package com.iqra.chinese.alarm;

/**
 * Pre-schedules HOURLY alarms for the next 7 days (168 slots) so the
 * notification/alarm cycle never breaks even if the user never opens the app.
 *
 * Each slot fires once per hour. At fire time, AlarmReceiver decides whether
 * to show a notification (hours 0–11 of the configured "day") or the full
 * alarm popup (hours 12–23), based on elapsed time since day-start — and
 * skips entirely if today's study goal is already met (see AlarmReceiver's
 * date-aware goal check, which works correctly even without the app open).
 *
 * Ring-buffer design: slot k (0..167) represents "k hours after today's
 * day-start". When slot k fires, it is immediately rescheduled +168 hours
 * (exactly 7 days) later, reusing the SAME request code — since 168 = 24×7,
 * this keeps the same hour-of-day alignment forever, and guarantees at least
 * ~7 days of alarms are always pending, regenerating indefinitely purely from
 * system alarm broadcasts (no app process required).
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u000b\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\rJ\u001e\u0010\u000e\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\r2\u0006\u0010\u000f\u001a\u00020\u00062\u0006\u0010\u0010\u001a\u00020\bJ\u000e\u0010\u0011\u001a\u00020\b2\u0006\u0010\f\u001a\u00020\rJ \u0010\u0012\u001a\n \u0014*\u0004\u0018\u00010\u00130\u00132\u0006\u0010\f\u001a\u00020\r2\u0006\u0010\u0015\u001a\u00020\u0006H\u0002J\u000e\u0010\u0016\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\rJ\u0018\u0010\u0017\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\r2\u0006\u0010\u0018\u001a\u00020\bH\u0002J\u0016\u0010\u0019\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\r2\u0006\u0010\u001a\u001a\u00020\bJ\u000e\u0010\u001b\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\rJ \u0010\u001c\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\r2\u0006\u0010\u0015\u001a\u00020\u00062\u0006\u0010\u001d\u001a\u00020\bH\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0006X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001e"}, d2 = {"Lcom/iqra/chinese/alarm/AlarmScheduler;", "", "()V", "EXTRA_CODE", "", "HOUR_BASE", "", "HOUR_MS", "", "SLOTS", "cancelWeek", "", "ctx", "Landroid/content/Context;", "extendSlot", "requestCode", "firedAtMs", "msUntilNext", "pi", "Landroid/app/PendingIntent;", "kotlin.jvm.PlatformType", "code", "rescheduleWeek", "saveNextTrigger", "ms", "scheduleDelay", "delayMs", "scheduleWeek", "set", "triggerMs", "app_debug"})
public final class AlarmScheduler {
    private static final int SLOTS = 168;
    private static final int HOUR_BASE = 5000;
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String EXTRA_CODE = "slot_code";
    private static final long HOUR_MS = 3600000L;
    @org.jetbrains.annotations.NotNull()
    public static final com.iqra.chinese.alarm.AlarmScheduler INSTANCE = null;
    
    private AlarmScheduler() {
        super();
    }
    
    /**
     * Call on first install, sign-in, day-start change, and BOOT_COMPLETED
     */
    public final void scheduleWeek(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx) {
    }
    
    /**
     * Called every time an hourly alarm fires — reschedules that exact slot
     * 168 hours (7 days) later, keeping the rolling window topped up forever.
     */
    public final void extendSlot(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx, int requestCode, long firedAtMs) {
    }
    
    /**
     * Call when user changes day-start time in Settings
     */
    public final void rescheduleWeek(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx) {
    }
    
    /**
     * Cancel everything (e.g. before reschedule or on sign-out)
     */
    public final void cancelWeek(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx) {
    }
    
    /**
     * Snooze — delay next alarm popup by 1 hour (used by overlay / AlarmActivity)
     */
    public final void scheduleDelay(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx, long delayMs) {
    }
    
    public final long msUntilNext(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx) {
        return 0L;
    }
    
    private final void set(android.content.Context ctx, int code, long triggerMs) {
    }
    
    private final android.app.PendingIntent pi(android.content.Context ctx, int code) {
        return null;
    }
    
    private final void saveNextTrigger(android.content.Context ctx, long ms) {
    }
}