package com.iqra.chinese.data

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Prefs(ctx: Context) {
    private val p = ctx.getSharedPreferences("iqra", Context.MODE_PRIVATE)
    private val g = Gson()

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
}
