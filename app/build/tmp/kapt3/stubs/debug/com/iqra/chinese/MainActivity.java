package com.iqra.chinese;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\b\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0012\u0010\u0015\u001a\u00020\u00162\b\u0010\u0017\u001a\u0004\u0018\u00010\u0018H\u0014J\b\u0010\u0019\u001a\u00020\u0016H\u0014J\b\u0010\u001a\u001a\u00020\u0016H\u0014J\b\u0010\u001b\u001a\u00020\u0016H\u0014J\b\u0010\u001c\u001a\u00020\u0016H\u0002J\b\u0010\u001d\u001a\u00020\u0016H\u0002J\b\u0010\u001e\u001a\u00020\u0016H\u0002J\b\u0010\u001f\u001a\u00020\u0016H\u0002R\u001a\u0010\u0003\u001a\u00020\u0004X\u0086.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0005\u0010\u0006\"\u0004\b\u0007\u0010\bR\u000e\u0010\t\u001a\u00020\nX\u0082.\u00a2\u0006\u0002\n\u0000R\u001c\u0010\u000b\u001a\u0010\u0012\f\u0012\n \u000e*\u0004\u0018\u00010\r0\r0\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u000f\u001a\u00020\u0010X\u0086.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0011\u0010\u0012\"\u0004\b\u0013\u0010\u0014\u00a8\u0006 "}, d2 = {"Lcom/iqra/chinese/MainActivity;", "Landroidx/appcompat/app/AppCompatActivity;", "()V", "binding", "Lcom/iqra/chinese/databinding/ActivityMainBinding;", "getBinding", "()Lcom/iqra/chinese/databinding/ActivityMainBinding;", "setBinding", "(Lcom/iqra/chinese/databinding/ActivityMainBinding;)V", "nav", "Landroidx/navigation/NavController;", "notifLauncher", "Landroidx/activity/result/ActivityResultLauncher;", "", "kotlin.jvm.PlatformType", "vm", "Lcom/iqra/chinese/ui/MainViewModel;", "getVm", "()Lcom/iqra/chinese/ui/MainViewModel;", "setVm", "(Lcom/iqra/chinese/ui/MainViewModel;)V", "onCreate", "", "s", "Landroid/os/Bundle;", "onDestroy", "onPause", "onResume", "openMiuiAutoStart", "requestNotif", "showBannerIfAvailable", "showMiuiAlarmPromptIfNeeded", "app_debug"})
public final class MainActivity extends androidx.appcompat.app.AppCompatActivity {
    public com.iqra.chinese.databinding.ActivityMainBinding binding;
    public com.iqra.chinese.ui.MainViewModel vm;
    private androidx.navigation.NavController nav;
    @org.jetbrains.annotations.NotNull()
    private final androidx.activity.result.ActivityResultLauncher<java.lang.String> notifLauncher = null;
    
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
    
    /**
     * Fetches a remote banner (if any) and shows it as a dismissible overlay.
     * Runs on every app open / resume from background. Cached locally so it
     * also works offline using the last-fetched banner.
     */
    private final void showBannerIfAvailable() {
    }
}