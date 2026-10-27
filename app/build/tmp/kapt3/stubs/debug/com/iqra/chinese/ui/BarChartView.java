package com.iqra.chinese.ui;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000>\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\u0007\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\u0018\u00002\u00020\u0001B\u001b\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\u0002\u0010\u0006J\u0010\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u0013H\u0014J\"\u0010\u0014\u001a\u00020\u00112\f\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u000f0\n2\f\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u000b0\nR\u000e\u0010\u0007\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u000b0\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u000f0\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0017"}, d2 = {"Lcom/iqra/chinese/ui/BarChartView;", "Landroid/view/View;", "ctx", "Landroid/content/Context;", "attrs", "Landroid/util/AttributeSet;", "(Landroid/content/Context;Landroid/util/AttributeSet;)V", "barPaint", "Landroid/graphics/Paint;", "labels", "", "", "lblPaint", "valPaint", "vals", "", "onDraw", "", "c", "Landroid/graphics/Canvas;", "setData", "v", "l", "app_debug"})
public final class BarChartView extends android.view.View {
    @org.jetbrains.annotations.NotNull()
    private java.util.List<java.lang.Float> vals;
    @org.jetbrains.annotations.NotNull()
    private java.util.List<java.lang.String> labels;
    @org.jetbrains.annotations.NotNull()
    private final android.graphics.Paint barPaint = null;
    @org.jetbrains.annotations.NotNull()
    private final android.graphics.Paint lblPaint = null;
    @org.jetbrains.annotations.NotNull()
    private final android.graphics.Paint valPaint = null;
    
    @kotlin.jvm.JvmOverloads()
    public BarChartView(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx, @org.jetbrains.annotations.Nullable()
    android.util.AttributeSet attrs) {
        super(null);
    }
    
    public final void setData(@org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.Float> v, @org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.String> l) {
    }
    
    @java.lang.Override()
    protected void onDraw(@org.jetbrains.annotations.NotNull()
    android.graphics.Canvas c) {
    }
    
    @kotlin.jvm.JvmOverloads()
    public BarChartView(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx) {
        super(null);
    }
}