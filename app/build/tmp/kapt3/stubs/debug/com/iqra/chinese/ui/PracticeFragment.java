package com.iqra.chinese.ui;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010#\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010!\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\f\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\t\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0018\u001a\u00020\u0019H\u0002J\b\u0010\u001a\u001a\u00020\u0019H\u0002J\b\u0010\u001b\u001a\u00020\u0017H\u0002J\b\u0010\u001c\u001a\u00020\u0019H\u0002J\u001a\u0010\u001d\u001a\u00020\u00192\u0006\u0010\u001e\u001a\u00020\f2\b\b\u0002\u0010\u001f\u001a\u00020\tH\u0002J\u0010\u0010 \u001a\u00020\u00192\u0006\u0010!\u001a\u00020\u0010H\u0002J\u0018\u0010\"\u001a\u00020\u00192\u0006\u0010#\u001a\u00020\t2\u0006\u0010$\u001a\u00020\tH\u0002J$\u0010%\u001a\u00020&2\u0006\u0010\'\u001a\u00020(2\b\u0010)\u001a\u0004\u0018\u00010*2\b\u0010+\u001a\u0004\u0018\u00010,H\u0016J\b\u0010-\u001a\u00020\u0019H\u0016J\u001a\u0010.\u001a\u00020\u00192\u0006\u0010/\u001a\u00020&2\b\u0010+\u001a\u0004\u0018\u00010,H\u0016J\b\u00100\u001a\u00020\u0019H\u0002J\b\u00101\u001a\u00020\u0019H\u0002J\b\u00102\u001a\u00020\u0019H\u0002J\b\u00103\u001a\u00020\u0019H\u0002J\b\u00104\u001a\u00020\u0019H\u0002R\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0005\u001a\u00020\u00048BX\u0082\u0004\u00a2\u0006\u0006\u001a\u0004\b\u0006\u0010\u0007R\u000e\u0010\b\u001a\u00020\tX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\tX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00100\u000fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\f0\u0012X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0013\u001a\u0004\u0018\u00010\u0014X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u00170\u0016X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u00065"}, d2 = {"Lcom/iqra/chinese/ui/PracticeFragment;", "Lcom/iqra/chinese/ui/BaseFragment;", "()V", "_b", "Lcom/iqra/chinese/databinding/FragmentPracticeBinding;", "b", "getB", "()Lcom/iqra/chinese/databinding/FragmentPracticeBinding;", "busy", "", "hint", "idx", "", "level", "masteredOnce", "", "", "queue", "", "ttsJob", "Lkotlinx/coroutines/Job;", "words", "", "Lcom/iqra/chinese/data/Word;", "buildPills", "", "check", "current", "flashCard", "load", "l", "restorePosition", "loadDots", "wordId", "next", "passed", "skip", "onCreateView", "Landroid/view/View;", "i", "Landroid/view/LayoutInflater;", "c", "Landroid/view/ViewGroup;", "s", "Landroid/os/Bundle;", "onDestroyView", "onViewCreated", "view", "show", "showGroupPicker", "showTtsSetupDialog", "speakCurrent", "speakCurrentSlow", "app_debug"})
public final class PracticeFragment extends com.iqra.chinese.ui.BaseFragment {
    @org.jetbrains.annotations.Nullable()
    private com.iqra.chinese.databinding.FragmentPracticeBinding _b;
    private int level = 1;
    @org.jetbrains.annotations.NotNull()
    private java.util.List<com.iqra.chinese.data.Word> words;
    private int idx = 0;
    @org.jetbrains.annotations.NotNull()
    private java.util.List<java.lang.Integer> queue;
    private boolean hint = false;
    private boolean busy = false;
    @org.jetbrains.annotations.Nullable()
    private kotlinx.coroutines.Job ttsJob;
    @org.jetbrains.annotations.NotNull()
    private final java.util.Set<java.lang.String> masteredOnce = null;
    
    public PracticeFragment() {
        super();
    }
    
    private final com.iqra.chinese.databinding.FragmentPracticeBinding getB() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public android.view.View onCreateView(@org.jetbrains.annotations.NotNull()
    android.view.LayoutInflater i, @org.jetbrains.annotations.Nullable()
    android.view.ViewGroup c, @org.jetbrains.annotations.Nullable()
    android.os.Bundle s) {
        return null;
    }
    
    @java.lang.Override()
    public void onViewCreated(@org.jetbrains.annotations.NotNull()
    android.view.View view, @org.jetbrains.annotations.Nullable()
    android.os.Bundle s) {
    }
    
    private final void speakCurrent() {
    }
    
    private final void speakCurrentSlow() {
    }
    
    private final void showTtsSetupDialog() {
    }
    
    private final void buildPills() {
    }
    
    private final void load(int l, boolean restorePosition) {
    }
    
    private final com.iqra.chinese.data.Word current() {
        return null;
    }
    
    private final void show() {
    }
    
    private final void loadDots(java.lang.String wordId) {
    }
    
    private final void check() {
    }
    
    private final void next(boolean passed, boolean skip) {
    }
    
    private final void flashCard() {
    }
    
    private final void showGroupPicker() {
    }
    
    @java.lang.Override()
    public void onDestroyView() {
    }
}