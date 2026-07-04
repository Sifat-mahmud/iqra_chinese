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
