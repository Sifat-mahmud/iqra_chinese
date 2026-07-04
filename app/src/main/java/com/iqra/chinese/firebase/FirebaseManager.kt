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

    // ── Realtime Database — once-a-day backup ─────────────────────────────────

    /**
     * Called from Repository.tickDaily() once the date rolls over, or from
     * SettingsFragment "Sync now" button. Silently no-ops if not logged in.
     *
     * Only pushes a lightweight summary + attempt pass/fail counts to stay
     * well within the free tier (no full history arrays, no MP3 data).
     */
    suspend fun backupStatsIfDue(prefs: Prefs, attempts: List<AttemptRecord>): Boolean {
        val uid = uid ?: return false                 // not logged in
        val today = todayString()
        if (prefs.lastBackupDate == today) return false  // already backed up today

        return runCatching {
            val statsRef = db.child("users").child(uid).child("stats")
            val statsMap = mapOf(
                "xp"             to prefs.xp,
                "streak"         to prefs.streak,
                "bestTest"       to prefs.bestTest,
                "unlockedLevels" to prefs.unlocked,
                "earnedAch"      to prefs.earned,
                "lastBackupDate" to today,
                "email"          to (userEmail ?: "")
            )
            statsRef.setValue(statsMap).await()

            // Attempt summary (pass/fail only, no full history)
            val attRef = db.child("users").child(uid).child("attempts")
            val attMap = attempts.associate { rec ->
                rec.itemId.replace(".", "_") to mapOf(
                    "p" to rec.pass,
                    "f" to rec.fail,
                    "l" to rec.level,
                    "t" to rec.itemType,
                    "ts" to rec.lastTs
                )
            }
            attRef.setValue(attMap).await()

            prefs.lastBackupDate = today
            logEvent("stats_backup", Bundle().apply {
                putInt("attempt_count", attempts.size)
            })
            true
        }.getOrElse { e ->
            e.printStackTrace()
            false
        }
    }

    /**
     * Full restore from Firebase after sign-in on a (possibly new/reinstalled) device.
     *
     * Restores:
     *  - User stats (xp, streak, bestTest, unlocked levels, earned achievements)
     *  - Per-word/sentence attempt records (pass/fail counts, mastery)
     *
     * Strategy: if local XP is 0 (fresh install) OR cloud XP is higher than local,
     * cloud data wins. This handles the "deleted app, reinstalled, signed back in"
     * case where local prefs are empty defaults.
     *
     * Returns true if any data was restored.
     */
    suspend fun restoreStats(prefs: Prefs, attemptDao: com.iqra.chinese.data.AttemptDao): Boolean {
        val uid = uid ?: return false
        return runCatching {
            val statsSnapshot = db.child("users").child(uid).child("stats").get().await()
            if (!statsSnapshot.exists()) return@runCatching false

            val cloudXp = (statsSnapshot.child("xp").value as? Long)?.toInt() ?: 0
            val isFreshInstall = prefs.xp == 0 && prefs.streak == 0
            val cloudIsNewer   = cloudXp > prefs.xp

            if (isFreshInstall || cloudIsNewer) {
                prefs.xp       = cloudXp
                prefs.streak   = (statsSnapshot.child("streak").value as? Long)?.toInt() ?: prefs.streak
                prefs.bestTest = (statsSnapshot.child("bestTest").value as? Long)?.toInt() ?: prefs.bestTest

                @Suppress("UNCHECKED_CAST")
                val levels = (statsSnapshot.child("unlockedLevels").value as? List<Long>)
                    ?.map { it.toInt() }?.toMutableList()
                if (levels != null) prefs.unlocked = levels

                @Suppress("UNCHECKED_CAST")
                val ach = (statsSnapshot.child("earnedAch").value as? List<String>)?.toMutableList()
                if (ach != null) prefs.earned = ach
            }

            // Restore attempt records (per-word/sentence mastery) on fresh install.
            // Always restore these on fresh install since local DB is empty either way.
            if (isFreshInstall) {
                val attSnapshot = db.child("users").child(uid).child("attempts").get().await()
                if (attSnapshot.exists()) {
                    for (child in attSnapshot.children) {
                        val itemId = (child.key ?: continue).replace("_", ".")
                        val pass   = (child.child("p").value as? Long)?.toInt() ?: 0
                        val fail   = (child.child("f").value as? Long)?.toInt() ?: 0
                        val level  = (child.child("l").value as? Long)?.toInt() ?: 1
                        val type   = child.child("t").value as? String ?: "word"
                        val ts     = (child.child("ts").value as? Long) ?: 0L

                        attemptDao.upsert(
                            com.iqra.chinese.data.AttemptRecord(
                                itemId  = itemId,
                                itemType = type,
                                level   = level,
                                pass    = pass,
                                fail    = fail,
                                lastTs  = ts
                            )
                        )
                    }
                }
            }

            // After restoring, set lastBackupDate to today so we don't immediately
            // re-backup and overwrite cloud data with the (now-synced) local copy
            prefs.lastBackupDate = todayString()

            logEvent("stats_restored", Bundle().apply {
                putBoolean("fresh_install", isFreshInstall)
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
