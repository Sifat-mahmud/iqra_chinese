package com.iqra.chinese.ui;

/**
 * Single linear first-login tour shown once from MainActivity.
 * All targets are views visible on the Home screen + bottom nav tabs.
 *
 * Flow:
 * 1. 🔥 Streak — explain daily streak
 * 2. 📊 Daily bar — explain study goal
 * 3. 📚 Level cards — tap to start word cards
 * 4. 📖 Sentence tab — bottom nav highlight
 * 5. ⚙️ Settings tab — toggle pinyin/meaning
 * 6. 📈 Stats tab — see progress
 * 7. 🏠 Home tab — welcome, let's go!
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000>\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0005\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\tJ\u0010\u0010\n\u001a\u00020\u000b2\u0006\u0010\b\u001a\u00020\tH\u0002J\u000e\u0010\f\u001a\u00020\u000b2\u0006\u0010\b\u001a\u00020\tJF\u0010\r\u001a\u00020\u000b2\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u00112\u0006\u0010\u0013\u001a\u00020\u00112\u0006\u0010\u0014\u001a\u00020\u00112\u0006\u0010\u0015\u001a\u00020\u00112\u0006\u0010\u0016\u001a\u00020\u00112\u0006\u0010\u0017\u001a\u00020\u0011J0\u0010\u0018\u001a\n \u001a*\u0004\u0018\u00010\u00190\u00192\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u001b\u001a\u00020\u00112\u0006\u0010\u001c\u001a\u00020\u00042\u0006\u0010\u001d\u001a\u00020\u0004H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001e"}, d2 = {"Lcom/iqra/chinese/ui/AppTour;", "", "()V", "KEY", "", "PREFS", "isDone", "", "ctx", "Landroid/content/Context;", "markDone", "", "resetAll", "startTour", "activity", "Landroid/app/Activity;", "wordCardArea", "Landroid/view/View;", "dailyBar", "streakView", "sentenceTab", "settingsTab", "statsTab", "homeTab", "t", "Lcom/getkeepsafe/taptargetview/TapTarget;", "kotlin.jvm.PlatformType", "view", "title", "desc", "app_debug"})
public final class AppTour {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String PREFS = "iqra_meta";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY = "tour_done_v2";
    @org.jetbrains.annotations.NotNull()
    public static final com.iqra.chinese.ui.AppTour INSTANCE = null;
    
    private AppTour() {
        super();
    }
    
    public final boolean isDone(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx) {
        return false;
    }
    
    private final void markDone(android.content.Context ctx) {
    }
    
    public final void resetAll(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx) {
    }
    
    public final void startTour(@org.jetbrains.annotations.NotNull()
    android.app.Activity activity, @org.jetbrains.annotations.NotNull()
    android.view.View wordCardArea, @org.jetbrains.annotations.NotNull()
    android.view.View dailyBar, @org.jetbrains.annotations.NotNull()
    android.view.View streakView, @org.jetbrains.annotations.NotNull()
    android.view.View sentenceTab, @org.jetbrains.annotations.NotNull()
    android.view.View settingsTab, @org.jetbrains.annotations.NotNull()
    android.view.View statsTab, @org.jetbrains.annotations.NotNull()
    android.view.View homeTab) {
    }
    
    private final com.getkeepsafe.taptargetview.TapTarget t(android.app.Activity activity, android.view.View view, java.lang.String title, java.lang.String desc) {
        return null;
    }
}