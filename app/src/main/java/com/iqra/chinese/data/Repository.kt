package com.iqra.chinese.data

import com.iqra.chinese.firebase.FirebaseManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeoutOrNull
import java.text.SimpleDateFormat
import java.util.*

class Repository(
    internal val attemptDao: AttemptDao,
    private val groupDao: GroupDao,
    val prefs: Prefs
) {
    // ── Words / Sentences ────────────────────────────────────────────────────
    fun wordsFor(level: Int) = HskData.wordsFor(level)
    fun sentencesFor(level: Int) = HskData.sentencesFor(level)
    fun wordById(id: String) = HskData.wordById(id)

    // ── Attempts ─────────────────────────────────────────────────────────────
    suspend fun record(itemId: String, type: String, level: Int, passed: Boolean) {
        val existing = attemptDao.get(itemId) ?: AttemptRecord(itemId, type, level)
        val hist = (existing.history + AttemptEntry(System.currentTimeMillis(), passed)).takeLast(40)
        attemptDao.upsert(existing.copy(
            pass  = if (passed) existing.pass + 1 else existing.pass,
            fail  = if (!passed) existing.fail + 1 else existing.fail,
            lastTs = System.currentTimeMillis(),
            history = hist
        ))
        if (passed) prefs.xp += 10
        touchStreak()
    }

    suspend fun getAttempt(id: String) = attemptDao.get(id)
    fun allAttempts(): Flow<List<AttemptRecord>> = attemptDao.all()
    fun attemptsForLevel(l: Int): Flow<List<AttemptRecord>> = attemptDao.forLevel(l)
    fun masteredCount(): Flow<Int> = attemptDao.all().map { recs ->
        // Cross-check against the full set of canonical word IDs currently in
        // HskData — a stale/orphaned attempt record (e.g. from an old app
        // version's word list, or a Firebase-synced record for an item that
        // no longer exists) should never count toward "mastered", even if it
        // has itemType='word' and pass>0 in the local/cloud-merged table.
        val allValidIds = HskData.words.values.flatten().map { it.id }.toSet()
        recs.count { it.itemType == "word" && it.pass > 0 && it.itemId in allValidIds }
    }

    /**
     * Mastery ratio for a level — deliberately does NOT trust the stored
     * `level` field on AttemptRecord, since it can drift out of sync with the
     * current HskData word list (word-list edits across app versions, or the
     * Firebase restore merge taking maxOf(local.level, cloud.level) which can
     * pull in a stale/mismatched value from an old sync). That drift is what
     * previously caused mastery percentages to exceed 100% — the numerator
     * could include items no longer canonically part of that level while the
     * denominator (HskData.wordsFor(l).size) stayed fixed.
     *
     * Fix: always resolve "does this item belong to level l" by checking
     * canonical HskData word-ID membership, not the possibly-stale stored
     * level. This makes the count self-healing — it corrects itself the
     * moment HskData is the source of truth, with no migration needed.
     */
    fun masteryForLevel(l: Int): Flow<Pair<Int,Int>> {
        val idsForLevel = HskData.wordsFor(l).map { it.id }.toSet()
        return attemptDao.all().map { recs ->
            val mastered = recs.count {
                it.itemType == "word" && it.pass > 0 && it.itemId in idsForLevel
            }
            Pair(mastered, idsForLevel.size)
        }
    }

    // ── Groups ────────────────────────────────────────────────────────────────
    fun allGroups(): Flow<List<StudyGroup>> = groupDao.all()
    suspend fun getGroup(name: String) = groupDao.get(name)
    suspend fun createGroup(name: String) { groupDao.upsert(StudyGroup(name)) }
    suspend fun addWordToGroup(groupName: String, wordId: String) {
        val g = groupDao.get(groupName) ?: StudyGroup(groupName)
        if (!g.wordIds.contains(wordId))
            groupDao.upsert(g.copy(wordIds = g.wordIds + wordId))
        prefs.xp += 2
    }
    suspend fun deleteGroup(g: StudyGroup) = groupDao.delete(g)
    fun wordsForGroup(g: StudyGroup) = g.wordIds.mapNotNull { HskData.wordById(it) }

    // ── Streak / Daily ────────────────────────────────────────────────────────
    private fun touchStreak() {
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        // Roll over at the user's CONFIGURED day-start time, not real midnight —
        // e.g. day-start 06:00 means the study day ends at 05:59 the next
        // calendar day, matching the alarm system's own day boundary.
        val studyDayStart = prefs.studyDayKey()
        val today = fmt.format(studyDayStart.time)
        if (prefs.lastDate != today) {
            // Save yesterday's final tally into the time-history log before resetting
            if (prefs.lastDate.isNotEmpty()) {
                prefs.saveTimeForDate(prefs.lastDate, prefs.dailySecs)
            }
            val yesterdayCal = (studyDayStart.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
            val yesterday = fmt.format(yesterdayCal.time)
            prefs.streak = if (prefs.lastDate == yesterday) prefs.streak + 1 else 1
            prefs.lastDate = today
            prefs.dailySecs = 0
        }
    }

    fun tickDaily() {
        prefs.dailySecs += 1
        touchStreak()
        // Keep today's entry continuously up to date in the history log too,
        // so the Stats time-chart reflects live progress. Throttled to every
        // 10s to avoid re-serializing the JSON map on every single tick.
        if (prefs.dailySecs % 10 == 0) {
            prefs.saveTimeForDate(prefs.lastDate, prefs.dailySecs)
        }
        // The instant the goal is reached, silence any notification/alarm that
        // is already showing/ringing for today — future scheduled alarms
        // already self-check the goal when they fire, but an already-active
        // one needs to be turned off live.
        if (prefs.dailySecs == prefs.dailyGoalSecs) {
            prefs.turnOffAlarmsForTodayIfGoalMet()
        }
    }

    /**
     * Call this from a coroutine scope after tickDaily — triggers the once-a-day
     * Firebase backup if the user is logged in and hasn't backed up today yet.
     */
    private var backupInFlight = false
    private var lastBackupTryMs = 0L

    /** Cheap, non-suspending check — safe to call every second from the timer. */
    fun backupWanted(): Boolean =
        !backupInFlight &&
        FirebaseManager.fullBackupDue(prefs) &&
        System.currentTimeMillis() - lastBackupTryMs >= 60_000L

    /**
     * Runs the once-a-day full backup. Call it in its OWN coroutine (never inline
     * in the timer loop): the network write can wait indefinitely while offline,
     * so it is time-limited, runs one at a time, and retries at most once a minute.
     */
    suspend fun maybeBackupToFirebase() {
        if (!backupWanted()) return
        lastBackupTryMs = System.currentTimeMillis()
        backupInFlight = true
        try {
            withTimeoutOrNull(20_000L) {
                FirebaseManager.backupStatsIfDue(prefs, attemptDao.allOnce())
            }
        } finally {
            backupInFlight = false
        }
    }

    /**
     * Light incremental push (stats, study time, changed attempts). Called when
     * the user leaves the app and once a minute while studying. Fire-and-forget:
     * the writes are only queued, so this returns immediately.
     */
    suspend fun flushProgressToCloud() {
        if (!FirebaseManager.isLoggedIn) return
        FirebaseManager.flushProgress(prefs, attemptDao.allOnce())
    }

    /** Forced full sync that waits for the server — used by "Sync now" and sign-out. */
    suspend fun syncNowToCloud(): Boolean {
        if (!FirebaseManager.isLoggedIn) return false
        return withTimeoutOrNull(15_000L) {
            FirebaseManager.syncNow(prefs, attemptDao.allOnce())
        } ?: false
    }

    // ── Achievements ─────────────────────────────────────────────────────────
    suspend fun checkAchievements(mastered: Int): List<Achievement> {
        val earned = prefs.earned
        val newList = mutableListOf<Achievement>()
        fun chk(id: String, cond: Boolean) {
            if (cond && !earned.contains(id)) {
                val a = HskData.ACHIEVEMENTS.find { it.id == id } ?: return
                prefs.earn(id); prefs.xp += a.xp; newList.add(a)
                FirebaseManager.logAchievementEarned(id)
            }
        }
        chk("first",   mastered >= 1)
        chk("ten",     mastered >= 10)
        chk("fifty",   mastered >= 50)
        chk("streak3", prefs.streak >= 3)
        chk("streak7", prefs.streak >= 7)
        chk("xp500",   prefs.xp >= 500)
        chk("xp2000",  prefs.xp >= 2000)
        chk("unlock2", prefs.unlocked.contains(2))
        chk("unlock3", prefs.unlocked.contains(3))
        chk("daily",   prefs.dailySecs >= prefs.dailyGoalSecs)
        return newList
    }

    // ── Reset ─────────────────────────────────────────────────────────────────
    suspend fun resetProgress() { attemptDao.clear(); prefs.resetProgress() }
    suspend fun resetAll() { attemptDao.clear(); groupDao.clear(); prefs.resetAll() }
}
