package com.iqra.chinese.alarm

import android.content.Context
import androidx.core.content.edit
import java.util.Calendar

/**
 * Stores all alarm-related preferences separately from the main Prefs class.
 *
 * Key concept — "day start":
 *   The user can configure what time their study day begins (default 00:01).
 *   [todayStartMs] is the epoch-ms of today's configured day-start time.
 *   This anchors the 0–12 h (notification) and 12–24 h (alarm) windows.
 *
 * Snooze keys are per-phase and reset each new day so the user always gets
 * one snooze per phase per day.
 */
class AlarmPrefs(ctx: Context) {
    private val p = ctx.getSharedPreferences("iqra_alarm", Context.MODE_PRIVATE)

    /** Hour portion of day-start time (0-23, default 0 = midnight) */
    var dayStartHour: Int
        get()  = p.getInt("day_hour", 0)
        set(v) = p.edit { putInt("day_hour", v) }

    /** Minute portion of day-start time (0-59, default 1 = 00:01) */
    var dayStartMinute: Int
        get()  = p.getInt("day_min", 1)
        set(v) = p.edit { putInt("day_min", v) }

    /**
     * Returns the epoch-ms of today's configured day-start.
     * If current time is before today's start time, returns yesterday's start.
     * This ensures the "day" always starts at the user's preferred time.
     */
    val todayStartMs: Long
        get() {
            val now = Calendar.getInstance()
            val start = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, dayStartHour)
                set(Calendar.MINUTE,      dayStartMinute)
                set(Calendar.SECOND,      0)
                set(Calendar.MILLISECOND, 0)
            }
            // If now is before today's configured start, use yesterday's start
            if (now.before(start)) start.add(Calendar.DAY_OF_YEAR, -1)
            return start.timeInMillis
        }

    /** Whether the user has snoozed this phase today */
    fun getSnoozeUsed(key: String): Boolean {
        val today = todayKey()
        return p.getBoolean("${key}_${today}", false)
    }

    /** Mark snooze as used for this phase today */
    fun setSnoozeUsed(key: String, used: Boolean) {
        val today = todayKey()
        p.edit { putBoolean("${key}_${today}", used) }
    }

    private fun todayKey(): String {
        val c = Calendar.getInstance()
        return "${c.get(Calendar.YEAR)}_${c.get(Calendar.DAY_OF_YEAR)}"
    }
}
