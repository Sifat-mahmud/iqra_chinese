package com.iqra.chinese.alarm;

/**
 * Stores all alarm-related preferences separately from the main Prefs class.
 *
 * Key concept — "day start":
 *  The user can configure what time their study day begins (default 00:01).
 *  [todayStartMs] is the epoch-ms of today's configured day-start time.
 *  This anchors the 0–12 h (notification) and 12–24 h (alarm) windows.
 *
 * Snooze keys are per-phase and reset each new day so the user always gets
 * one snooze per phase per day.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000>\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010\u0016\u001a\u00020\u00172\u0006\u0010\u0018\u001a\u00020\u0019J\u0016\u0010\u001a\u001a\u00020\u001b2\u0006\u0010\u0018\u001a\u00020\u00192\u0006\u0010\u001c\u001a\u00020\u0017J\b\u0010\u001d\u001a\u00020\u0019H\u0002R$\u0010\u0007\u001a\u00020\u00062\u0006\u0010\u0005\u001a\u00020\u00068F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\b\b\u0010\t\"\u0004\b\n\u0010\u000bR$\u0010\f\u001a\u00020\u00062\u0006\u0010\u0005\u001a\u00020\u00068F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\b\r\u0010\t\"\u0004\b\u000e\u0010\u000bR\u0016\u0010\u000f\u001a\n \u0011*\u0004\u0018\u00010\u00100\u0010X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0011\u0010\u0012\u001a\u00020\u00138F\u00a2\u0006\u0006\u001a\u0004\b\u0014\u0010\u0015\u00a8\u0006\u001e"}, d2 = {"Lcom/iqra/chinese/alarm/AlarmPrefs;", "", "ctx", "Landroid/content/Context;", "(Landroid/content/Context;)V", "v", "", "dayStartHour", "getDayStartHour", "()I", "setDayStartHour", "(I)V", "dayStartMinute", "getDayStartMinute", "setDayStartMinute", "p", "Landroid/content/SharedPreferences;", "kotlin.jvm.PlatformType", "todayStartMs", "", "getTodayStartMs", "()J", "getSnoozeUsed", "", "key", "", "setSnoozeUsed", "", "used", "todayKey", "app_debug"})
public final class AlarmPrefs {
    private final android.content.SharedPreferences p = null;
    
    public AlarmPrefs(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx) {
        super();
    }
    
    public final int getDayStartHour() {
        return 0;
    }
    
    public final void setDayStartHour(int v) {
    }
    
    public final int getDayStartMinute() {
        return 0;
    }
    
    public final void setDayStartMinute(int v) {
    }
    
    public final long getTodayStartMs() {
        return 0L;
    }
    
    /**
     * Whether the user has snoozed this phase today
     */
    public final boolean getSnoozeUsed(@org.jetbrains.annotations.NotNull()
    java.lang.String key) {
        return false;
    }
    
    /**
     * Mark snooze as used for this phase today
     */
    public final void setSnoozeUsed(@org.jetbrains.annotations.NotNull()
    java.lang.String key, boolean used) {
    }
    
    private final java.lang.String todayKey() {
        return null;
    }
}