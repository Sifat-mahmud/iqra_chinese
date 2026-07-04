package com.iqra.chinese.ui;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010!\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\n\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u001d\u001a\u00020\u001eH\u0002J\b\u0010\u001f\u001a\u00020\u001eH\u0002J\b\u0010 \u001a\u00020\u001eH\u0002J$\u0010!\u001a\u00020\"2\u0006\u0010#\u001a\u00020$2\b\u0010%\u001a\u0004\u0018\u00010&2\b\u0010\'\u001a\u0004\u0018\u00010(H\u0016J\b\u0010)\u001a\u00020\u001eH\u0016J\u001a\u0010*\u001a\u00020\u001e2\u0006\u0010+\u001a\u00020\"2\b\u0010\'\u001a\u0004\u0018\u00010(H\u0016J\b\u0010,\u001a\u00020\u001eH\u0002J\b\u0010-\u001a\u00020\u001eH\u0002J\b\u0010.\u001a\u00020\u001eH\u0002J\b\u0010/\u001a\u00020\u001eH\u0002J\b\u00100\u001a\u00020\u001eH\u0002J\b\u00101\u001a\u00020\u001eH\u0002R\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0005\u001a\u00020\u00048BX\u0082\u0004\u00a2\u0006\u0006\u001a\u0004\b\u0006\u0010\u0007R\u000e\u0010\b\u001a\u00020\tX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\u000bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u000bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u000fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u000bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\tX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00140\u0013X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0015\u001a\u00020\u000fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u00170\u0013X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0018\u001a\u00020\u000b8BX\u0082\u0004\u00a2\u0006\u0006\u001a\u0004\b\u0019\u0010\u001aR\u0014\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u00170\u001cX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u00062"}, d2 = {"Lcom/iqra/chinese/ui/TestFragment;", "Lcom/iqra/chinese/ui/BaseFragment;", "()V", "_b", "Lcom/iqra/chinese/databinding/FragmentTestBinding;", "b", "getB", "()Lcom/iqra/chinese/databinding/FragmentTestBinding;", "busy", "", "fail", "", "idx", "level", "mode", "", "pass", "started", "testSents", "", "Lcom/iqra/chinese/data/Sentence;", "testType", "testWords", "Lcom/iqra/chinese/data/Word;", "totalQ", "getTotalQ", "()I", "wrongWords", "", "advanceQ", "", "buildPills", "checkAnswer", "onCreateView", "Landroid/view/View;", "i", "Landroid/view/LayoutInflater;", "c", "Landroid/view/ViewGroup;", "s", "Landroid/os/Bundle;", "onDestroyView", "onViewCreated", "view", "showQ", "showResult", "showSetup", "startTest", "updateModes", "updateTabs", "app_debug"})
public final class TestFragment extends com.iqra.chinese.ui.BaseFragment {
    @org.jetbrains.annotations.Nullable()
    private com.iqra.chinese.databinding.FragmentTestBinding _b;
    private int level = 1;
    @org.jetbrains.annotations.NotNull()
    private java.lang.String testType = "words";
    @org.jetbrains.annotations.NotNull()
    private java.lang.String mode = "meaning";
    @org.jetbrains.annotations.NotNull()
    private java.util.List<com.iqra.chinese.data.Word> testWords;
    @org.jetbrains.annotations.NotNull()
    private java.util.List<com.iqra.chinese.data.Sentence> testSents;
    private int idx = 0;
    private int pass = 0;
    private int fail = 0;
    @org.jetbrains.annotations.NotNull()
    private java.util.List<com.iqra.chinese.data.Word> wrongWords;
    private boolean busy = false;
    private boolean started = false;
    
    public TestFragment() {
        super();
    }
    
    private final com.iqra.chinese.databinding.FragmentTestBinding getB() {
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
    
    private final void showSetup() {
    }
    
    private final void buildPills() {
    }
    
    private final void updateTabs() {
    }
    
    private final void updateModes() {
    }
    
    private final void startTest() {
    }
    
    private final int getTotalQ() {
        return 0;
    }
    
    private final void showQ() {
    }
    
    private final void checkAnswer() {
    }
    
    private final void advanceQ() {
    }
    
    private final void showResult() {
    }
    
    @java.lang.Override()
    public void onDestroyView() {
    }
}