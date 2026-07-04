package com.iqra.chinese.firebase;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0016\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u0004J\u0018\u0010\u0012\u001a\u0004\u0018\u00010\u00132\u0006\u0010\u000f\u001a\u00020\u0010H\u0086@\u00a2\u0006\u0002\u0010\u0014J\u0010\u0010\u0015\u001a\u00020\u000e2\u0006\u0010\u0011\u001a\u00020\u0004H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u001b\u0010\u0007\u001a\u00020\b8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u000b\u0010\f\u001a\u0004\b\t\u0010\n\u00a8\u0006\u0016"}, d2 = {"Lcom/iqra/chinese/firebase/BannerManager;", "", "()V", "KEY_CACHED", "", "KEY_DISMISSED", "PREFS", "db", "Lcom/google/firebase/database/DatabaseReference;", "getDb", "()Lcom/google/firebase/database/DatabaseReference;", "db$delegate", "Lkotlin/Lazy;", "dismiss", "", "ctx", "Landroid/content/Context;", "bannerId", "fetchBanner", "Lcom/iqra/chinese/firebase/BannerConfig;", "(Landroid/content/Context;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "incrementViewCount", "app_debug"})
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
     * Call when user taps the X / close on the banner
     */
    public final void dismiss(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx, @org.jetbrains.annotations.NotNull()
    java.lang.String bannerId) {
    }
    
    /**
     * Increments a global view/dismiss counter for this banner id in Firebase,
     * stored at /banners/stats/{bannerId}/dismissCount.
     * Fire-and-forget — failures are silently ignored (offline-safe).
     */
    private final void incrementViewCount(java.lang.String bannerId) {
    }
}