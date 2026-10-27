package com.iqra.chinese.data

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Prefs(ctx: Context) {
    private val appCtx = ctx.applicationContext
    private val p = appCtx.getSharedPreferences("iqra", Context.MODE_PRIVATE)
    private val g = Gson()

    /**
     * Returns the "study day" boundary (as a Calendar set to the day-start
     * moment), rolling over at the user's CONFIGURED day-start time (set in
     * Settings) rather than at real midnight. E.g. if day-start is 06:00, the
     * study day runs 06:00→05:59 the next calendar day — matching the alarm
     * system's own day boundary (see AlarmPrefs.todayStartMs, whose
     * day_hour/day_min this reads).
     *
     * Reads the same "iqra_alarm" SharedPreferences AlarmPrefs uses directly,
     * to avoid a circular module dependency on the alarm package.
     */
    fun studyDayKey(): java.util.Calendar {
        val alarmPrefs = appCtx.getSharedPreferences("iqra_alarm", Context.MODE_PRIVATE)
        val hour = alarmPrefs.getInt("day_hour", 0)
        val min  = alarmPrefs.getInt("day_min", 1)

        val now = java.util.Calendar.getInstance()
        val start = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, min)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        // Before today's configured start time → still "yesterday's" study day
        if (now.before(start)) start.add(java.util.Calendar.DAY_OF_YEAR, -1)
        return start
    }

    var showPinyin: Boolean get() = p.getBoolean("sp", true); set(v) = p.edit { putBoolean("sp", v) }
    var showMeaning: Boolean get() = p.getBoolean("sm", true); set(v) = p.edit { putBoolean("sm", v) }
    var xp: Int get() = p.getInt("xp", 0); set(v) = p.edit { putInt("xp", v) }
    var streak: Int get() = p.getInt("streak", 1); set(v) = p.edit { putInt("streak", v) }
    var dailySecs: Int get() = p.getInt("daily", 0); set(v) = p.edit { putInt("daily", v) }
    var lastDate: String get() = p.getString("ldate", "") ?: ""; set(v) = p.edit { putString("ldate", v) }
    var bestTest: Int get() = p.getInt("best", 0); set(v) = p.edit { putInt("best", v) }
    var snoozeUsed: Boolean get() = p.getBoolean("snooze", false); set(v) = p.edit { putBoolean("snooze", v) }

    /**
     * Daily study goal in minutes. User-configurable 5–60, default 30.
     * Stored in minutes (not seconds) for simpler UI binding with NumberPicker/SeekBar.
     */
    var dailyGoalMinutes: Int
        get()  = p.getInt("goal_min", 30).coerceIn(5, 60)
        set(v) = p.edit { putInt("goal_min", v.coerceIn(5, 60)) }

    /** Goal expressed in seconds — used everywhere internally for comparison against dailySecs */
    val dailyGoalSecs: Int get() = dailyGoalMinutes * 60

    /**
     * Per-day study time log: { "yyyy-MM-dd" : totalSecondsThatDay }
     * Updated once per day when the date rolls over (see Repository.touchStreak),
     * plus continuously for "today" via a live merge in getTimeHistory().
     */
    var timeHistoryJson: String
        get()  = p.getString("time_hist", "{}") ?: "{}"
        set(v) = p.edit { putString("time_hist", v) }

    fun getTimeHistory(): MutableMap<String, Int> {
        val type = object : TypeToken<MutableMap<String, Int>>(){}.type
        return runCatching { g.fromJson<MutableMap<String, Int>>(timeHistoryJson, type) }
            .getOrNull() ?: mutableMapOf()
    }

    /** Records [seconds] of study time for [date] (yyyy-MM-dd), replacing any prior value for that day. */
    fun saveTimeForDate(date: String, seconds: Int) {
        val hist = getTimeHistory()
        hist[date] = seconds
        timeHistoryJson = g.toJson(hist)
    }

    // Firebase backup — tracks last date we pushed stats to Realtime DB
    var lastBackupDate: String get() = p.getString("last_backup", "") ?: ""; set(v) = p.edit { putString("last_backup", v) }

    // Last viewed word — so user continues where they left off
    var lastLevel:   Int    get() = p.getInt("last_lvl", 1);   set(v) = p.edit { putInt("last_lvl", v) }
    var lastWordIdx: Int    get() = p.getInt("last_idx", 0);   set(v) = p.edit { putInt("last_idx", v) }

    /**
     * Per-level card position — each of the 6 HSK levels remembers its own
     * last-seen word/sentence card index independently, so switching between
     * levels (or leaving the app and coming back) resumes exactly where the
     * user left off in EACH level, not just the single most recent one.
     */
    /** Uid whose cloud data has already been merged into this device. Nothing is
     *  pushed to the cloud until this matches the signed-in uid — otherwise a
     *  fresh install could overwrite the cloud copy with near-empty local data. */
    var restoredUid: String
        get()  = p.getString("restored_uid", "") ?: ""
        set(v) = p.edit { putString("restored_uid", v) }

    /** Highest attempt lastTs already pushed — only newer records are re-sent. */
    var lastAttemptPushTs: Long
        get()  = p.getLong("att_push_ts", 0L)
        set(v) = p.edit { putLong("att_push_ts", v) }

    /** Current study day as yyyy-MM-dd (rolls over at the configured day-start). */
    fun todayKey(): String =
        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(studyDayKey().time)

    /**
     * Merges per-day study seconds from the cloud into local storage (the larger
     * value wins for each day, so nothing is ever double-counted) and applies
     * today's value to the live counter shown on Home.
     */
    fun mergeCloudTime(cloud: Map<String, Int>) {
        if (cloud.isEmpty()) return
        val today = todayKey()
        val hist  = getTimeHistory()

        // Park the local counter under its own day first
        if (lastDate.isNotEmpty()) hist[lastDate] = maxOf(hist[lastDate] ?: 0, dailySecs)
        for ((d, s) in cloud) if (s > (hist[d] ?: 0)) hist[d] = s

        // The live counter must belong to the current study day
        if (lastDate != today) { lastDate = today; dailySecs = 0 }
        val best = maxOf(dailySecs, hist[today] ?: 0)
        dailySecs  = best
        hist[today] = best
        timeHistoryJson = g.toJson(hist)
    }

    fun wordIdxFor(level: Int): Int = p.getInt("word_idx_$level", 0)
    fun setWordIdxFor(level: Int, idx: Int) = p.edit { putInt("word_idx_$level", idx) }

    fun sentIdxFor(level: Int): Int = p.getInt("sent_idx_$level", 0)
    fun setSentIdxFor(level: Int, idx: Int) = p.edit { putInt("sent_idx_$level", idx) }

    var unlocked: MutableList<Int>
        get() { val s = p.getString("ul", null); return if (s==null) mutableListOf(1) else g.fromJson(s, object: TypeToken<MutableList<Int>>(){}.type) }
        set(v) = p.edit { putString("ul", g.toJson(v)) }

    var earned: MutableList<String>
        get() { val s = p.getString("ach", null); return if (s==null) mutableListOf() else g.fromJson(s, object: TypeToken<MutableList<String>>(){}.type) }
        set(v) = p.edit { putString("ach", g.toJson(v)) }

    fun unlock(l: Int) { val u = unlocked; if (!u.contains(l)) { u.add(l); unlocked = u } }
    fun earn(id: String) { val e = earned; if (!e.contains(id)) { e.add(id); earned = e } }
    fun resetProgress() { p.edit { putInt("xp",0); putInt("streak",1); putInt("daily",0); putString("ldate",""); putString("ul",null); putString("ach",null); putInt("best",0) } }
    fun resetAll() = p.edit { clear() }

    /**
     * Turns off any currently-active study reminder/alarm for today the moment
     * the daily goal is reached — not just future scheduled ones (those already
     * self-check the goal when they fire and no-op correctly). This covers:
     *  - An already-shown ongoing Phase-0 notification that would otherwise
     *    stay stuck on screen until manually dismissed
     *  - A currently-ringing Phase-1 alarm (sound + overlay + full-screen GUI)
     * Safe to call repeatedly — cancelling an already-gone notification/service
     * is a harmless no-op.
     */
    fun turnOffAlarmsForTodayIfGoalMet() {
        if (dailySecs < dailyGoalSecs) return
        try {
            val nm = appCtx.getSystemService(Context.NOTIFICATION_SERVICE)
                as android.app.NotificationManager
            nm.cancel(com.iqra.chinese.alarm.NID)
        } catch (_: Exception) {}
        try {
            com.iqra.chinese.alarm.AlarmSoundService.stop(appCtx)
        } catch (_: Exception) {}
    }
}
