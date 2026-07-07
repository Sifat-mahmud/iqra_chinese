package com.iqra.chinese.firebase;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000X\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0016\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u0004J\u0018\u0010\u0012\u001a\u0004\u0018\u00010\u00132\u0006\u0010\u000f\u001a\u00020\u0010H\u0086@\u00a2\u0006\u0002\u0010\u0014J\u0010\u0010\u0015\u001a\u00020\u000e2\u0006\u0010\u0011\u001a\u00020\u0004H\u0002J\u001a\u0010\u0016\u001a\u0004\u0018\u00010\u00132\u0006\u0010\u0017\u001a\u00020\u00182\u0006\u0010\u0019\u001a\u00020\u001aH\u0002J\u000e\u0010\u001b\u001a\u00020\u000e2\u0006\u0010\u001c\u001a\u00020\u001dJ\u0010\u0010\u001e\u001a\u00020\u00182\u0006\u0010\u001f\u001a\u00020 H\u0002J$\u0010!\u001a\u00020\u001d2\u0006\u0010\u000f\u001a\u00020\u00102\u0014\u0010\"\u001a\u0010\u0012\u0006\u0012\u0004\u0018\u00010\u0013\u0012\u0004\u0012\u00020\u000e0#J \u0010$\u001a\u00020\u000e2\u0006\u0010\u0011\u001a\u00020\u00042\u0006\u0010%\u001a\u00020\u00042\b\u0010&\u001a\u0004\u0018\u00010\u0004R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u001b\u0010\u0007\u001a\u00020\b8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u000b\u0010\f\u001a\u0004\b\t\u0010\n\u00a8\u0006\'"}, d2 = {"Lcom/iqra/chinese/firebase/BannerManager;", "", "()V", "KEY_CACHED", "", "KEY_DISMISSED", "PREFS", "db", "Lcom/google/firebase/database/DatabaseReference;", "getDb", "()Lcom/google/firebase/database/DatabaseReference;", "db$delegate", "Lkotlin/Lazy;", "dismiss", "", "ctx", "Landroid/content/Context;", "bannerId", "fetchBanner", "Lcom/iqra/chinese/firebase/BannerConfig;", "(Landroid/content/Context;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "incrementViewCount", "parseConfig", "json", "Lorg/json/JSONObject;", "prefs", "Landroid/content/SharedPreferences;", "removeLiveListener", "listener", "Lcom/google/firebase/database/ValueEventListener;", "snapshotToJson", "snapshot", "Lcom/google/firebase/database/DataSnapshot;", "startLiveListener", "onBanner", "Lkotlin/Function1;", "submitFeedback", "response", "uid", "app_debug"})
public final class BannerManager {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String PREFS = "iqra_banner";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_CACHED = "cached_json";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_DISMISSED = "dismissed_id";
    @org.jetbrains.annotations.NotNull()
    private static final kotlin.Lazy db$delegate = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.iqra.chinese.firebase.BannerManager INSTANCE = null;
    
    private BannerManager() {
        super();
    }
    
    private final com.google.firebase.database.DatabaseReference getDb() {
        return null;
    }
    
    /**
     * Fetches the latest banner from Firebase (if online) and caches it locally.
     * Returns the banner to show, or null if none/dismissed/disabled.
     * Safe to call on every app launch — does nothing harmful if offline.
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object fetchBanner(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.iqra.chinese.firebase.BannerConfig> $completion) {
        return null;
    }
    
    /**
     * Attaches a live listener to /banners/active so that if an admin pushes a
     * NEW or UPDATED banner while the app is open, it is delivered immediately —
     * no app restart needed.
     *
     * [onBanner] is called with the new banner config whenever it changes and
     * passes the dismissed/disabled checks, or null if it should be hidden.
     * Caller is responsible for removing the listener (see [removeLiveListener])
     * when the host is destroyed.
     *
     * Returns the listener handle so it can be detached later.
     */
    @org.jetbrains.annotations.NotNull()
    public final com.google.firebase.database.ValueEventListener startLiveListener(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.iqra.chinese.firebase.BannerConfig, kotlin.Unit> onBanner) {
        return null;
    }
    
    /**
     * Detach a listener started with [startLiveListener]. Call in onDestroy().
     */
    public final void removeLiveListener(@org.jetbrains.annotations.NotNull()
    com.google.firebase.database.ValueEventListener listener) {
    }
    
    private final org.json.JSONObject snapshotToJson(com.google.firebase.database.DataSnapshot snapshot) {
        return null;
    }
    
    private final com.iqra.chinese.firebase.BannerConfig parseConfig(org.json.JSONObject json, android.content.SharedPreferences prefs) {
        return null;
    }
    
    /**
     * Call when user taps the X / close on the banner
     */
    public final void dismiss(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx, @org.jetbrains.annotations.NotNull()
    java.lang.String bannerId) {
    }
    
    /**
     * Saves a user's feedback response to:
     *  /banners/feedback/{bannerId}/{uid_or_anon}/{timestamp}
     *    response: "user's answer text"
     *    uid: "user uid or anonymous"
     *    ts: epoch ms
     */
    public final void submitFeedback(@org.jetbrains.annotations.NotNull()
    java.lang.String bannerId, @org.jetbrains.annotations.NotNull()
    java.lang.String response, @org.jetbrains.annotations.Nullable()
    java.lang.String uid) {
    }
    
    /**
     * Increments a global view/dismiss counter for this banner id in Firebase,
     * stored at /banners/stats/{bannerId}/dismissCount.
     * Fire-and-forget — failures are silently ignored (offline-safe).
     */
    private final void incrementViewCount(java.lang.String bannerId) {
    }
}