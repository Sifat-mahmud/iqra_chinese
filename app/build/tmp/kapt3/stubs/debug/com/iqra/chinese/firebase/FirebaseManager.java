package com.iqra.chinese.firebase;

/**
 * Central Firebase manager.
 *
 * Responsibilities:
 * - Email/password Auth (sign-up, sign-in, sign-out, password reset)
 * - Firebase Analytics event logging
 * - Once-a-day backup of user stats to Realtime Database
 *
 * Database structure (designed to stay within free Spark tier):
 * users/{uid}/stats/
 *     xp, streak, bestTest, dailySecs, lastBackupDate
 * users/{uid}/attempts/{itemId}/
 *     pass, fail, level, itemType, lastTs
 *
 * We intentionally omit per-attempt history arrays from the cloud backup
 * to keep payload small (free tier limit: 1 GB storage, 10 GB/month download).
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u009c\u0001\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0010$\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\u0011\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u001c\u0010\u001e\u001a\u000e\u0012\u0004\u0012\u00020\u0019\u0012\u0004\u0012\u00020\u00010\u001f2\u0006\u0010 \u001a\u00020!H\u0002J$\u0010\"\u001a\u00020\u00162\u0006\u0010#\u001a\u00020$2\f\u0010%\u001a\b\u0012\u0004\u0012\u00020!0&H\u0086@\u00a2\u0006\u0002\u0010\'J\u0018\u0010(\u001a\b\u0012\u0004\u0012\u00020)0&2\b\u0010*\u001a\u0004\u0018\u00010\u0019H\u0002J\u0016\u0010+\u001a\u00020\u00192\f\u0010,\u001a\b\u0012\u0004\u0012\u00020)0&H\u0002J\u000e\u0010-\u001a\u00020.2\u0006\u0010#\u001a\u00020$J\u001c\u0010/\u001a\u00020.2\u0006\u0010#\u001a\u00020$2\f\u0010%\u001a\b\u0012\u0004\u0012\u00020!0&J\u000e\u00100\u001a\u00020\u00162\u0006\u0010#\u001a\u00020$J\u0016\u00101\u001a\u0002022\u0006\u00103\u001a\u0002042\u0006\u00105\u001a\u00020\u0019J&\u00106\u001a\b\u0012\u0004\u0012\u00020\f072\b\u00108\u001a\u0004\u0018\u000109H\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b:\u0010;J\u000e\u0010<\u001a\u00020.2\u0006\u0010=\u001a\u00020\u0019J\u0006\u0010>\u001a\u00020.J\u001a\u0010?\u001a\u00020.2\u0006\u0010@\u001a\u00020\u00192\n\b\u0002\u0010A\u001a\u0004\u0018\u00010BJ\u000e\u0010C\u001a\u00020.2\u0006\u0010D\u001a\u00020EJ\u001e\u0010F\u001a\u00020.2\u0006\u0010D\u001a\u00020E2\u0006\u0010G\u001a\u00020E2\u0006\u0010H\u001a\u00020EJ\u001e\u0010I\u001a\u00020.2\u0006\u0010D\u001a\u00020E2\u0006\u0010J\u001a\u00020E2\u0006\u0010K\u001a\u00020\u0019J\u001e\u0010L\u001a\u00020\u00162\u0006\u0010#\u001a\u00020$2\u0006\u0010M\u001a\u00020NH\u0086@\u00a2\u0006\u0002\u0010OJ\u0010\u0010P\u001a\u00020\u00192\u0006\u0010=\u001a\u00020\u0019H\u0002J$\u0010Q\u001a\b\u0012\u0004\u0012\u00020.072\u0006\u0010R\u001a\u00020\u0019H\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\bS\u0010TJ,\u0010U\u001a\b\u0012\u0004\u0012\u00020\f072\u0006\u0010R\u001a\u00020\u00192\u0006\u0010V\u001a\u00020\u0019H\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\bW\u0010XJ\u0006\u0010Y\u001a\u00020.J,\u0010Z\u001a\b\u0012\u0004\u0012\u00020\f072\u0006\u0010R\u001a\u00020\u00192\u0006\u0010V\u001a\u00020\u0019H\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b[\u0010XJ$\u0010\\\u001a\u00020\u00162\u0006\u0010#\u001a\u00020$2\f\u0010%\u001a\b\u0012\u0004\u0012\u00020!0&H\u0086@\u00a2\u0006\u0002\u0010\'J\u0018\u0010]\u001a\u00020\u00162\u0006\u0010#\u001a\u00020$2\u0006\u0010\u0018\u001a\u00020\u0019H\u0002J\b\u0010^\u001a\u00020\u0019H\u0002R\u0014\u0010\u0003\u001a\u00020\u00048BX\u0082\u0004\u00a2\u0006\u0006\u001a\u0004\b\u0005\u0010\u0006R\u0014\u0010\u0007\u001a\u00020\b8BX\u0082\u0004\u00a2\u0006\u0006\u001a\u0004\b\t\u0010\nR\u0013\u0010\u000b\u001a\u0004\u0018\u00010\f8F\u00a2\u0006\u0006\u001a\u0004\b\r\u0010\u000eR\u001b\u0010\u000f\u001a\u00020\u00108BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0013\u0010\u0014\u001a\u0004\b\u0011\u0010\u0012R\u0011\u0010\u0015\u001a\u00020\u00168F\u00a2\u0006\u0006\u001a\u0004\b\u0015\u0010\u0017R\u0013\u0010\u0018\u001a\u0004\u0018\u00010\u00198F\u00a2\u0006\u0006\u001a\u0004\b\u001a\u0010\u001bR\u0013\u0010\u001c\u001a\u0004\u0018\u00010\u00198F\u00a2\u0006\u0006\u001a\u0004\b\u001d\u0010\u001b\u0082\u0002\u000b\n\u0002\b!\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006_"}, d2 = {"Lcom/iqra/chinese/firebase/FirebaseManager;", "", "()V", "analytics", "Lcom/google/firebase/analytics/FirebaseAnalytics;", "getAnalytics", "()Lcom/google/firebase/analytics/FirebaseAnalytics;", "auth", "Lcom/google/firebase/auth/FirebaseAuth;", "getAuth", "()Lcom/google/firebase/auth/FirebaseAuth;", "currentUser", "Lcom/google/firebase/auth/FirebaseUser;", "getCurrentUser", "()Lcom/google/firebase/auth/FirebaseUser;", "db", "Lcom/google/firebase/database/DatabaseReference;", "getDb", "()Lcom/google/firebase/database/DatabaseReference;", "db$delegate", "Lkotlin/Lazy;", "isLoggedIn", "", "()Z", "uid", "", "getUid", "()Ljava/lang/String;", "userEmail", "getUserEmail", "attemptMap", "", "rec", "Lcom/iqra/chinese/data/AttemptRecord;", "backupStatsIfDue", "prefs", "Lcom/iqra/chinese/data/Prefs;", "attempts", "", "(Lcom/iqra/chinese/data/Prefs;Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "decodeHistory", "Lcom/iqra/chinese/data/AttemptEntry;", "s", "encodeHistory", "h", "flushDailyTime", "", "flushProgress", "fullBackupDue", "getGoogleSignInClient", "Lcom/google/android/gms/auth/api/signin/GoogleSignInClient;", "context", "Landroid/content/Context;", "webClientId", "handleGoogleSignInResult", "Lkotlin/Result;", "data", "Landroid/content/Intent;", "handleGoogleSignInResult-gIAlu-s", "(Landroid/content/Intent;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "logAchievementEarned", "id", "logAppOpen", "logEvent", "name", "params", "Landroid/os/Bundle;", "logLevelUnlocked", "level", "", "logPracticeSession", "correct", "wrong", "logTestCompleted", "pct", "type", "restoreStats", "attemptDao", "Lcom/iqra/chinese/data/AttemptDao;", "(Lcom/iqra/chinese/data/Prefs;Lcom/iqra/chinese/data/AttemptDao;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "safeKey", "sendPasswordReset", "email", "sendPasswordReset-gIAlu-s", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "signIn", "password", "signIn-0E7RQCE", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "signOut", "signUp", "signUp-0E7RQCE", "syncNow", "syncReady", "todayString", "app_debug"})
public final class FirebaseManager {
    @org.jetbrains.annotations.NotNull()
    private static final kotlin.Lazy db$delegate = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.iqra.chinese.firebase.FirebaseManager INSTANCE = null;
    
    private FirebaseManager() {
        super();
    }
    
    private final com.google.firebase.auth.FirebaseAuth getAuth() {
        return null;
    }
    
    private final com.google.firebase.analytics.FirebaseAnalytics getAnalytics() {
        return null;
    }
    
    private final com.google.firebase.database.DatabaseReference getDb() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.google.firebase.auth.FirebaseUser getCurrentUser() {
        return null;
    }
    
    public final boolean isLoggedIn() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getUid() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getUserEmail() {
        return null;
    }
    
    public final void signOut() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.google.android.gms.auth.api.signin.GoogleSignInClient getGoogleSignInClient(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    java.lang.String webClientId) {
        return null;
    }
    
    public final void logEvent(@org.jetbrains.annotations.NotNull()
    java.lang.String name, @org.jetbrains.annotations.Nullable()
    android.os.Bundle params) {
    }
    
    public final void logLevelUnlocked(int level) {
    }
    
    public final void logPracticeSession(int level, int correct, int wrong) {
    }
    
    public final void logTestCompleted(int level, int pct, @org.jetbrains.annotations.NotNull()
    java.lang.String type) {
    }
    
    public final void logAchievementEarned(@org.jetbrains.annotations.NotNull()
    java.lang.String id) {
    }
    
    public final void logAppOpen() {
    }
    
    /**
     * Firebase keys can't contain . $ # [ ] /. Real ids (e.g. "2_帮助", "s1_9")
     * contain none of those, so this is the identity for them and keys stay
     * identical to earlier backups. The untouched id is also stored as a field.
     */
    private final java.lang.String safeKey(java.lang.String id) {
        return null;
    }
    
    private final java.lang.String encodeHistory(java.util.List<com.iqra.chinese.data.AttemptEntry> h) {
        return null;
    }
    
    private final java.util.List<com.iqra.chinese.data.AttemptEntry> decodeHistory(java.lang.String s) {
        return null;
    }
    
    private final java.util.Map<java.lang.String, java.lang.Object> attemptMap(com.iqra.chinese.data.AttemptRecord rec) {
        return null;
    }
    
    /**
     * True once this account's cloud data has been merged into this device.
     * Nothing is pushed before that, otherwise a fresh install would overwrite
     * the cloud copy with empty / near-empty local data.
     */
    private final boolean syncReady(com.iqra.chinese.data.Prefs prefs, java.lang.String uid) {
        return false;
    }
    
    /**
     * Cheap check used by the timer: is the once-a-day full backup still due?
     */
    public final boolean fullBackupDue(@org.jetbrains.annotations.NotNull()
    com.iqra.chinese.data.Prefs prefs) {
        return false;
    }
    
    /**
     * Once-a-day full snapshot. No-ops if not logged in, not yet restored, or already done today.
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object backupStatsIfDue(@org.jetbrains.annotations.NotNull()
    com.iqra.chinese.data.Prefs prefs, @org.jetbrains.annotations.NotNull()
    java.util.List<com.iqra.chinese.data.AttemptRecord> attempts, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    /**
     * Full push of stats + all attempts (with history) + study-time history.
     * Used by the daily backup, the Settings "Sync now" button and sign-out.
     * Non-destructive: attempts and time are merged into the cloud, never deleted.
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object syncNow(@org.jetbrains.annotations.NotNull()
    com.iqra.chinese.data.Prefs prefs, @org.jetbrains.annotations.NotNull()
    java.util.List<com.iqra.chinese.data.AttemptRecord> attempts, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    /**
     * Pushes today's study seconds (plus the last couple of days' final tallies).
     * Fire-and-forget and non-suspending, so it is safe to call from onStop():
     * the write is queued immediately and, with disk persistence on (see IqraApp),
     * survives the process being killed right afterwards.
     */
    public final void flushDailyTime(@org.jetbrains.annotations.NotNull()
    com.iqra.chinese.data.Prefs prefs) {
    }
    
    /**
     * Incremental sync, called when the user leaves the app and once a minute
     * while studying: stats summary, study time, and only the attempt records
     * changed since the last push. Never deletes anything in the cloud.
     */
    public final void flushProgress(@org.jetbrains.annotations.NotNull()
    com.iqra.chinese.data.Prefs prefs, @org.jetbrains.annotations.NotNull()
    java.util.List<com.iqra.chinese.data.AttemptRecord> attempts) {
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
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object restoreStats(@org.jetbrains.annotations.NotNull()
    com.iqra.chinese.data.Prefs prefs, @org.jetbrains.annotations.NotNull()
    com.iqra.chinese.data.AttemptDao attemptDao, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    private final java.lang.String todayString() {
        return null;
    }
}