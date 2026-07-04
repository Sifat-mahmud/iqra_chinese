package com.iqra.chinese.alarm;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006J\u000e\u0010\u0007\u001a\u00020\b2\u0006\u0010\u0005\u001a\u00020\u0006J\u0018\u0010\t\u001a\n \u000b*\u0004\u0018\u00010\n0\n2\u0006\u0010\u0005\u001a\u00020\u0006H\u0002J\u000e\u0010\f\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006J\u0016\u0010\r\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u000e\u001a\u00020\b\u00a8\u0006\u000f"}, d2 = {"Lcom/iqra/chinese/alarm/AlarmScheduler;", "", "()V", "cancel", "", "ctx", "Landroid/content/Context;", "msUntilNext", "", "pendingIntent", "Landroid/app/PendingIntent;", "kotlin.jvm.PlatformType", "schedule", "scheduleDelay", "delayMs", "app_debug"})
public final class AlarmScheduler {
    @org.jetbrains.annotations.NotNull()
    public static final com.iqra.chinese.alarm.AlarmScheduler INSTANCE = null;
    
    private AlarmScheduler() {
        super();
    }
    
    public final void schedule(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx) {
    }
    
    public final void scheduleDelay(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx, long delayMs) {
    }
    
    public final long msUntilNext(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx) {
        return 0L;
    }
    
    public final void cancel(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx) {
    }
    
    private final android.app.PendingIntent pendingIntent(android.content.Context ctx) {
        return null;
    }
}