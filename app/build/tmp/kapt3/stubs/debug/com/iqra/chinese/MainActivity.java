package com.iqra.chinese;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000R\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\t\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u001a\u001a\u00020\u001b2\u0006\u0010\u001c\u001a\u00020\u001dH\u0002J\u0012\u0010\u001e\u001a\u0004\u0018\u00010\u000e2\u0006\u0010\u001f\u001a\u00020\u000eH\u0002J\u0012\u0010 \u001a\u00020\u001b2\b\u0010!\u001a\u0004\u0018\u00010\"H\u0014J\b\u0010#\u001a\u00020\u001bH\u0014J\b\u0010$\u001a\u00020\u001bH\u0014J\b\u0010%\u001a\u00020\u001bH\u0014J\b\u0010&\u001a\u00020\u001bH\u0002J\b\u0010\'\u001a\u00020\u001bH\u0002J\b\u0010(\u001a\u00020\u001bH\u0002J\b\u0010)\u001a\u00020\u001bH\u0002J\b\u0010*\u001a\u00020\u001bH\u0002R\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0005\u001a\u00020\u0006X\u0086.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0007\u0010\b\"\u0004\b\t\u0010\nR\u0010\u0010\u000b\u001a\u0004\u0018\u00010\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\r\u001a\u0004\u0018\u00010\u000eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u0010X\u0082.\u00a2\u0006\u0002\n\u0000R\u001c\u0010\u0011\u001a\u0010\u0012\f\u0012\n \u0013*\u0004\u0018\u00010\u000e0\u000e0\u0012X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0014\u001a\u00020\u0015X\u0086.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0016\u0010\u0017\"\u0004\b\u0018\u0010\u0019\u00a8\u0006+"}, d2 = {"Lcom/iqra/chinese/MainActivity;", "Landroidx/appcompat/app/AppCompatActivity;", "()V", "bannerListener", "Lcom/google/firebase/database/ValueEventListener;", "binding", "Lcom/iqra/chinese/databinding/ActivityMainBinding;", "getBinding", "()Lcom/iqra/chinese/databinding/ActivityMainBinding;", "setBinding", "(Lcom/iqra/chinese/databinding/ActivityMainBinding;)V", "exoPlayer", "Landroidx/media3/exoplayer/ExoPlayer;", "lastShownBannerId", "", "nav", "Landroidx/navigation/NavController;", "notifLauncher", "Landroidx/activity/result/ActivityResultLauncher;", "kotlin.jvm.PlatformType", "vm", "Lcom/iqra/chinese/ui/MainViewModel;", "getVm", "()Lcom/iqra/chinese/ui/MainViewModel;", "setVm", "(Lcom/iqra/chinese/ui/MainViewModel;)V", "displayBanner", "", "banner", "Lcom/iqra/chinese/firebase/BannerConfig;", "extractYoutubeId", "url", "onCreate", "s", "Landroid/os/Bundle;", "onDestroy", "onPause", "onResume", "openMiuiAutoStart", "releasePlayer", "requestNotif", "showBannerIfAvailable", "showMiuiAlarmPromptIfNeeded", "app_debug"})
public final class MainActivity extends androidx.appcompat.app.AppCompatActivity {
    public com.iqra.chinese.databinding.ActivityMainBinding binding;
    public com.iqra.chinese.ui.MainViewModel vm;
    private androidx.navigation.NavController nav;
    @org.jetbrains.annotations.NotNull()
    private final androidx.activity.result.ActivityResultLauncher<java.lang.String> notifLauncher = null;
    
    /**
     * Fetches a remote banner (if any) and shows it as a dismissible overlay.
     * Runs on every app open / resume from background. Cached locally so it
     * also works offline using the last-fetched banner.
     */
    @org.jetbrains.annotations.Nullable()
    private com.google.firebase.database.ValueEventListener bannerListener;
    @org.jetbrains.annotations.Nullable()
    private java.lang.String lastShownBannerId;
    @org.jetbrains.annotations.Nullable()
    private androidx.media3.exoplayer.ExoPlayer exoPlayer;
    
    public MainActivity() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.iqra.chinese.databinding.ActivityMainBinding getBinding() {
        return null;
    }
    
    public final void setBinding(@org.jetbrains.annotations.NotNull()
    com.iqra.chinese.databinding.ActivityMainBinding p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.iqra.chinese.ui.MainViewModel getVm() {
        return null;
    }
    
    public final void setVm(@org.jetbrains.annotations.NotNull()
    com.iqra.chinese.ui.MainViewModel p0) {
    }
    
    @java.lang.Override()
    protected void onCreate(@org.jetbrains.annotations.Nullable()
    android.os.Bundle s) {
    }
    
    @java.lang.Override()
    protected void onResume() {
    }
    
    @java.lang.Override()
    protected void onPause() {
    }
    
    @java.lang.Override()
    protected void onDestroy() {
    }
    
    private final void requestNotif() {
    }
    
    private final void showMiuiAlarmPromptIfNeeded() {
    }
    
    private final void openMiuiAutoStart() {
    }
    
    private final void showBannerIfAvailable() {
    }
    
    private final void displayBanner(com.iqra.chinese.firebase.BannerConfig banner) {
    }
    
    private final void releasePlayer() {
    }
    
    private final java.lang.String extractYoutubeId(java.lang.String url) {
        return null;
    }
}