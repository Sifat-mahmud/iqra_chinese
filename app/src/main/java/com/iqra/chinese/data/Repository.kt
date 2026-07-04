package com.iqra.chinese.data

import com.iqra.chinese.firebase.FirebaseManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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
    fun masteredCount(): Flow<Int> = attemptDao.masteredCount()

    fun masteryForLevel(l: Int): Flow<Pair<Int,Int>> = attemptDao.forLevel(l).map { recs ->
        Pair(recs.count { it.pass > 0 }, HskData.wordsFor(l).size)
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
        val today = fmt.format(Date())
        if (prefs.lastDate != today) {
            val yesterday = fmt.format(Date(System.currentTimeMillis() - 86_400_000L))
            prefs.streak = if (prefs.lastDate == yesterday) prefs.streak + 1 else 1
            prefs.lastDate = today
            prefs.dailySecs = 0
        }
    }

    fun tickDaily() { prefs.dailySecs += 1; touchStreak() }

    /**
     * Call this from a coroutine scope after tickDaily — triggers the once-a-day
     * Firebase backup if the user is logged in and hasn't backed up today yet.
     */
    suspend fun maybeBackupToFirebase() {
        if (!FirebaseManager.isLoggedIn) return
        val attempts = attemptDao.allOnce()
        FirebaseManager.backupStatsIfDue(prefs, attempts)
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
        chk("daily",   prefs.dailySecs >= 1800)
        return newList
    }

    // ── Reset ─────────────────────────────────────────────────────────────────
    suspend fun resetProgress() { attemptDao.clear(); prefs.resetProgress() }
    suspend fun resetAll() { attemptDao.clear(); groupDao.clear(); prefs.resetAll() }
}
