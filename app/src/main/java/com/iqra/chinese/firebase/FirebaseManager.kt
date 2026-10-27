package com.iqra.chinese.firebase

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.iqra.chinese.data.AttemptEntry
import com.iqra.chinese.data.AttemptRecord
import com.iqra.chinese.data.Prefs
import kotlinx.coroutines.tasks.await

/**
 * Central Firebase manager.
 *
 * Responsibilities:
 *  - Email/password Auth (sign-up, sign-in, sign-out, password reset)
 *  - Firebase Analytics event logging
 *  - Once-a-day backup of user stats to Realtime Database
 *
 * Database structure (designed to stay within free Spark tier):
 *  users/{uid}/stats/
 *      xp, streak, bestTest, dailySecs, lastBackupDate
 *  users/{uid}/attempts/{itemId}/
 *      pass, fail, level, itemType, lastTs
 *
 * We intentionally omit per-attempt history arrays from the cloud backup
 * to keep payload small (free tier limit: 1 GB storage, 10 GB/month download).
 */
object FirebaseManager {

    private val auth: FirebaseAuth get() = Firebase.auth
    private val analytics: FirebaseAnalytics get() = Firebase.analytics
    // Keep DB reference lazy so it is only created after google-services.json is loaded
    private val db by lazy {
        Firebase.database("https://iqra-chinese-default-rtdb.firebaseio.com").reference
    }

    // ── Auth ──────────────────────────────────────────────────────────────────

    val currentUser: FirebaseUser? get() = auth.currentUser
    val isLoggedIn: Boolean get() = auth.currentUser != null
    val uid: String? get() = auth.currentUser?.uid
    val userEmail: String? get() = auth.currentUser?.email

    /**
     * Register a new account. Returns Result.success(user) or Result.failure(exception).
     */
    suspend fun signUp(email: String, password: String): Result<FirebaseUser> = runCatching {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val user = result.user ?: error("User creation returned null")
        logEvent("sign_up", Bundle().apply { putString("method", "email") })
        user
    }

    /**
     * Sign in an existing account.
     */
    suspend fun signIn(email: String, password: String): Result<FirebaseUser> = runCatching {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        val user = result.user ?: error("Sign-in returned null user")
        logEvent("login", Bundle().apply { putString("method", "email") })
        user
    }

    fun signOut() {
        logEvent("sign_out")
        auth.signOut()
    }

    // ── Google Sign-In (legacy GoogleSignInClient — works on Xiaomi/MIUI) ────

    fun getGoogleSignInClient(context: Context, webClientId: String): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

    /**
     * Call this after receiving the result from the Google Sign-In intent.
     * Pass the data Intent from onActivityResult / ActivityResultLauncher.
     */
    suspend fun handleGoogleSignInResult(data: Intent?): Result<FirebaseUser> = runCatching {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        val account = task.await()
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        val result = auth.signInWithCredential(credential).await()
        val user = result.user ?: error("Google sign-in returned null user")
        logEvent("login", Bundle().apply { putString("method", "google") })
        user
    }

    /**
     * Send a password-reset email.
     */
    suspend fun sendPasswordReset(email: String): Result<Unit> = runCatching {
        auth.sendPasswordResetEmail(email).await()
    }

    // ── Analytics ─────────────────────────────────────────────────────────────

    fun logEvent(name: String, params: Bundle? = null) {
        analytics.logEvent(name, params)
    }

    fun logLevelUnlocked(level: Int) = logEvent("level_unlocked",
        Bundle().apply { putInt("level", level) })

    fun logPracticeSession(level: Int, correct: Int, wrong: Int) = logEvent("practice_session",
        Bundle().apply {
            putInt("level", level)
            putInt("correct", correct)
            putInt("wrong", wrong)
        })

    fun logTestCompleted(level: Int, pct: Int, type: String) = logEvent("test_completed",
        Bundle().apply {
            putInt("level", level)
            putInt("score_pct", pct)
            putString("test_type", type)
        })

    fun logAchievementEarned(id: String) = logEvent("achievement_earned",
        Bundle().apply { putString("achievement_id", id) })

    fun logAppOpen() = logEvent(FirebaseAnalytics.Event.APP_OPEN)

    // ── Realtime Database — progress sync ─────────────────────────────────────
    //
    //   /users/{uid}/stats      xp, streak, bestTest, unlockedLevels, earnedAch, …
    //   /users/{uid}/attempts   per word/sentence: id, p, f, l, t, ts, h (history)
    //   /users/{uid}/time       { "yyyy-MM-dd": secondsStudied } per study day

    /** Firebase keys can't contain . $ # [ ] /. Real ids (e.g. "2_帮助", "s1_9")
     *  contain none of those, so this is the identity for them and keys stay
     *  identical to earlier backups. The untouched id is also stored as a field. */
    private fun safeKey(id: String): String {
        val bad = ".\$#[]/"
        return id.map { if (it in bad) '_' else it }.joinToString("")
    }

    private fun encodeHistory(h: List<AttemptEntry>): String =
        h.joinToString(",") { "${it.ts}:${if (it.passed) 1 else 0}" }

    private fun decodeHistory(s: String?): List<AttemptEntry> {
        if (s.isNullOrBlank()) return emptyList()
        return s.split(",").mapNotNull { part ->
            val bits = part.split(":")
            val ts = bits.getOrNull(0)?.toLongOrNull() ?: return@mapNotNull null
            AttemptEntry(ts, bits.getOrNull(1) == "1")
        }
    }

    private fun attemptMap(rec: AttemptRecord): Map<String, Any> = mapOf(
        "id" to rec.itemId,
        "p"  to rec.pass,
        "f"  to rec.fail,
        "l"  to rec.level,
        "t"  to rec.itemType,
        "ts" to rec.lastTs,
        "h"  to encodeHistory(rec.history)
    )

    /** True once this account's cloud data has been merged into this device.
     *  Nothing is pushed before that, otherwise a fresh install would overwrite
     *  the cloud copy with empty / near-empty local data. */
    private fun syncReady(prefs: Prefs, uid: String) = prefs.restoredUid == uid

    /** Cheap check used by the timer: is the once-a-day full backup still due? */
    fun fullBackupDue(prefs: Prefs): Boolean {
        val uid = uid ?: return false
        return syncReady(prefs, uid) && prefs.lastBackupDate != todayString()
    }

    /** Once-a-day full snapshot. No-ops if not logged in, not yet restored, or already done today. */
    suspend fun backupStatsIfDue(prefs: Prefs, attempts: List<AttemptRecord>): Boolean {
        if (!fullBackupDue(prefs)) return false
        return syncNow(prefs, attempts)
    }

    /**
     * Full push of stats + all attempts (with history) + study-time history.
     * Used by the daily backup, the Settings "Sync now" button and sign-out.
     * Non-destructive: attempts and time are merged into the cloud, never deleted.
     */
    suspend fun syncNow(prefs: Prefs, attempts: List<AttemptRecord>): Boolean {
        val uid = uid ?: return false
        if (!syncReady(prefs, uid)) return false
        val today = todayString()

        return runCatching {
            val base = db.child("users").child(uid)

            base.child("stats").setValue(mapOf(
                "xp"             to prefs.xp,
                "streak"         to prefs.streak,
                "bestTest"       to prefs.bestTest,
                "unlockedLevels" to prefs.unlocked,
                "earnedAch"      to prefs.earned,
                "lastBackupDate" to today,
                "email"          to (userEmail ?: "")
            )).await()

            if (attempts.isNotEmpty()) {
                base.child("attempts")
                    .updateChildren(attempts.associate { safeKey(it.itemId) to attemptMap(it) })
                    .await()
                prefs.lastAttemptPushTs = maxOf(prefs.lastAttemptPushTs, attempts.maxOf { it.lastTs })
            }

            val timeMap = HashMap<String, Any>(prefs.getTimeHistory())
            if (prefs.lastDate.isNotEmpty()) timeMap[prefs.lastDate] = prefs.dailySecs
            if (timeMap.isNotEmpty()) base.child("time").updateChildren(timeMap).await()

            prefs.lastBackupDate = today
            logEvent("stats_backup", Bundle().apply { putInt("attempt_count", attempts.size) })
            true
        }.getOrElse { e ->
            e.printStackTrace()
            false
        }
    }

    /**
     * Pushes today's study seconds (plus the last couple of days' final tallies).
     * Fire-and-forget and non-suspending, so it is safe to call from onStop():
     * the write is queued immediately and, with disk persistence on (see IqraApp),
     * survives the process being killed right afterwards.
     */
    fun flushDailyTime(prefs: Prefs) {
        val uid = uid ?: return
        if (!syncReady(prefs, uid)) return
        val updates = HashMap<String, Any>()
        // Recent days from local history (yesterday's final tally lives here) …
        prefs.getTimeHistory().entries.sortedByDescending { it.key }.take(3)
            .forEach { updates[it.key] = it.value }
        // … then the live counter, which belongs to the day it was last rolled for
        if (prefs.lastDate.isNotEmpty()) updates[prefs.lastDate] = prefs.dailySecs
        if (updates.isEmpty()) return
        db.child("users").child(uid).child("time").updateChildren(updates)
    }

    /**
     * Incremental sync, called when the user leaves the app and once a minute
     * while studying: stats summary, study time, and only the attempt records
     * changed since the last push. Never deletes anything in the cloud.
     */
    fun flushProgress(prefs: Prefs, attempts: List<AttemptRecord>) {
        val uid = uid ?: return
        if (!syncReady(prefs, uid)) return
        val base = db.child("users").child(uid)

        base.child("stats").updateChildren(mapOf(
            "xp"             to prefs.xp,
            "streak"         to prefs.streak,
            "bestTest"       to prefs.bestTest,
            "unlockedLevels" to prefs.unlocked,
            "earnedAch"      to prefs.earned
        ))

        val changed = attempts.filter { it.lastTs > prefs.lastAttemptPushTs }
        if (changed.isNotEmpty()) {
            base.child("attempts")
                .updateChildren(changed.associate { safeKey(it.itemId) to attemptMap(it) })
            prefs.lastAttemptPushTs = changed.maxOf { it.lastTs }
        }
        flushDailyTime(prefs)
    }

    /**
     * Restores and MERGES cloud data into local storage after sign-in (and once
     * per launch until it has succeeded).
     *
     * Stats:     xp, streak, bestTest → higher wins; unlockedLevels / earnedAch → union
     * Attempts:  pass/fail/level/lastTs → higher wins; history → union by timestamp
     * Time:      per-day study seconds → higher wins; today's value feeds the live counter
     *
     * Handles fresh install, sign-out → sign-in on the same device, and sign-in
     * on another device. Marks this uid as restored only when everything
     * succeeded, which is what unlocks pushing data back to the cloud.
     */
    suspend fun restoreStats(prefs: Prefs, attemptDao: com.iqra.chinese.data.AttemptDao): Boolean {
        val uid = uid ?: return false
        return runCatching {
            val userRef      = db.child("users").child(uid)
            val statsSnap    = userRef.child("stats").get().await()
            val attemptsSnap = userRef.child("attempts").get().await()
            val timeSnap     = userRef.child("time").get().await()

            // ── Stats ─────────────────────────────────────────────────────────
            if (statsSnap.exists()) {
                val cloudXp     = (statsSnap.child("xp").value       as? Long)?.toInt() ?: 0
                val cloudStreak = (statsSnap.child("streak").value   as? Long)?.toInt() ?: 0
                val cloudBest   = (statsSnap.child("bestTest").value as? Long)?.toInt() ?: 0
                if (cloudXp     > prefs.xp)       prefs.xp       = cloudXp
                if (cloudStreak > prefs.streak)   prefs.streak   = cloudStreak
                if (cloudBest   > prefs.bestTest) prefs.bestTest = cloudBest

                @Suppress("UNCHECKED_CAST")
                val cloudLevels = (statsSnap.child("unlockedLevels").value as? List<Long>)
                    ?.map { it.toInt() }?.toSet() ?: emptySet()
                if (cloudLevels.isNotEmpty())
                    prefs.unlocked = (prefs.unlocked.toSet() + cloudLevels).toMutableList()

                @Suppress("UNCHECKED_CAST")
                val cloudAch = (statsSnap.child("earnedAch").value as? List<String>)
                    ?.toSet() ?: emptySet()
                if (cloudAch.isNotEmpty())
                    prefs.earned = (prefs.earned.toSet() + cloudAch).toMutableList()
            }

            // ── Attempts (with history) ──────────────────────────────────────
            for (child in attemptsSnap.children) {
                // Use the stored id; the key is only a sanitised copy of it. (Never
                // convert "_" back to "." — real ids such as "2_帮助" contain underscores.)
                val itemId = (child.child("id").value as? String) ?: (child.key ?: continue)
                val cloudPass  = (child.child("p").value  as? Long)?.toInt() ?: 0
                val cloudFail  = (child.child("f").value  as? Long)?.toInt() ?: 0
                val cloudLevel = (child.child("l").value  as? Long)?.toInt() ?: 1
                val cloudType  = child.child("t").value as? String ?: "word"
                val cloudTs    = (child.child("ts").value as? Long) ?: 0L
                val cloudHist  = decodeHistory(child.child("h").value as? String)

                // Level always comes from HskData, never from stale stored values
                val level = if (cloudType == "word")
                    com.iqra.chinese.data.HskData.wordById(itemId)?.level ?: cloudLevel
                else cloudLevel

                val local = attemptDao.get(itemId)
                if (local == null) {
                    attemptDao.upsert(AttemptRecord(
                        itemId = itemId, itemType = cloudType, level = level,
                        pass = cloudPass, fail = cloudFail, lastTs = cloudTs,
                        history = cloudHist.sortedBy { it.ts }.takeLast(40)
                    ))
                } else {
                    val mergedHist = (local.history + cloudHist)
                        .distinctBy { it.ts }.sortedBy { it.ts }.takeLast(40)
                    val merged = local.copy(
                        pass    = maxOf(local.pass, cloudPass),
                        fail    = maxOf(local.fail, cloudFail),
                        lastTs  = maxOf(local.lastTs, cloudTs),
                        level   = level,
                        history = mergedHist
                    )
                    if (merged != local) attemptDao.upsert(merged)
                }
            }

            // ── Study time per day ───────────────────────────────────────────
            val cloudTime = HashMap<String, Int>()
            for (c in timeSnap.children) {
                val k = c.key ?: continue
                val v = (c.value as? Long)?.toInt() ?: (c.value as? Double)?.toInt() ?: continue
                cloudTime[k] = v
            }
            prefs.mergeCloudTime(cloudTime)

            // Everything merged — from now on this device may push to the cloud
            prefs.restoredUid = uid
            logEvent("stats_restored", Bundle().apply {
                putBoolean("had_local_data", prefs.xp > 0 || prefs.streak > 0)
            })
            true
        }.getOrElse { e ->
            e.printStackTrace()
            false
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun todayString(): String {
        val fmt = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return fmt.format(java.util.Date())
    }
}
