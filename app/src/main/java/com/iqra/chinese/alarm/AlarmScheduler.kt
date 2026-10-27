package com.iqra.chinese.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

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
object AlarmScheduler {

    private const val SLOTS      = 168                    // 24h × 7 days
    private const val HOUR_BASE  = 5000                    // requestCode range 5000-5167
    private const val EXTRA_CODE = "slot_code"
    private val HOUR_MS          = 3_600_000L

    // ── Public API ────────────────────────────────────────────────────────────

    /** Call on first install, sign-in, day-start change, and BOOT_COMPLETED */
    fun scheduleWeek(ctx: Context) {
        cancelWeek(ctx)
        val base = AlarmPrefs(ctx).todayStartMs
        val now  = System.currentTimeMillis()
        for (k in 0 until SLOTS) {
            var triggerMs = base + k * HOUR_MS
            // Skip already-passed slots in the very first batch — push them
            // a full week ahead so we never schedule something in the past
            if (triggerMs <= now) triggerMs += SLOTS * HOUR_MS
            set(ctx, HOUR_BASE + k, triggerMs)
        }
        saveNextTrigger(ctx, base + HOUR_MS)
    }

    /**
     * Called every time an hourly alarm fires — reschedules that exact slot
     * 168 hours (7 days) later, keeping the rolling window topped up forever.
     */
    fun extendSlot(ctx: Context, requestCode: Int, firedAtMs: Long) {
        val k = requestCode - HOUR_BASE
        if (k !in 0 until SLOTS) return
        set(ctx, requestCode, firedAtMs + SLOTS * HOUR_MS)
    }

    /** Call when user changes day-start time in Settings */
    fun rescheduleWeek(ctx: Context) = scheduleWeek(ctx)

    /** Cancel everything (e.g. before reschedule or on sign-out) */
    fun cancelWeek(ctx: Context) {
        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        for (k in 0 until SLOTS) am.cancel(pi(ctx, HOUR_BASE + k))
    }

    /** Snooze — delay next alarm popup by 1 hour (used by overlay / AlarmActivity) */
    fun scheduleDelay(ctx: Context, delayMs: Long) {
        val triggerMs = System.currentTimeMillis() + delayMs
        set(ctx, 42, triggerMs)
        saveNextTrigger(ctx, triggerMs)
    }

    fun msUntilNext(ctx: Context): Long {
        val saved = ctx.getSharedPreferences("iqra_alarm", Context.MODE_PRIVATE)
            .getLong("next_trigger_ms", -1L)
        if (saved == -1L) return -1L
        return (saved - System.currentTimeMillis()).coerceAtLeast(0L)
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private fun set(ctx: Context, code: Int, triggerMs: Long) {
        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val showPi = PendingIntent.getActivity(
            ctx, 4100 + code,
            Intent(ctx, AlarmActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        am.setAlarmClock(
            AlarmManager.AlarmClockInfo(triggerMs, showPi),
            pi(ctx, code)
        )
    }

    private fun pi(ctx: Context, code: Int) = PendingIntent.getBroadcast(
        ctx, code,
        Intent(ctx, AlarmReceiver::class.java).apply {
            action = ACTION_ALARM
            putExtra(EXTRA_CODE, code)
        },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    private fun saveNextTrigger(ctx: Context, ms: Long) {
        ctx.getSharedPreferences("iqra_alarm", Context.MODE_PRIVATE)
            .edit().putLong("next_trigger_ms", ms).apply()
    }
}
