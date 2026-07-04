package com.iqra.chinese.data

import androidx.room.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class Word(val id: String, val char: String, val pinyin: String, val meaning: String, val level: Int, val tone: Int = 1)
data class Sentence(val id: String, val char: String, val pinyin: String, val meaning: String, val level: Int)
data class AttemptEntry(val ts: Long, val passed: Boolean)
data class Achievement(val id: String, val emoji: String, val title: String, val desc: String, val xp: Int)

class Converters {
    @TypeConverter fun fromList(l: List<AttemptEntry>): String = Gson().toJson(l)
    @TypeConverter fun toList(s: String): List<AttemptEntry> = Gson().fromJson(s, object: TypeToken<List<AttemptEntry>>(){}.type) ?: emptyList()
    @TypeConverter fun fromStrList(l: List<String>): String = Gson().toJson(l)
    @TypeConverter fun toStrList(s: String): List<String> = Gson().fromJson(s, object: TypeToken<List<String>>(){}.type) ?: emptyList()
}

@Entity(tableName="attempts")
@TypeConverters(Converters::class)
data class AttemptRecord(
    @PrimaryKey val itemId: String,
    val itemType: String = "word",
    val level: Int = 1,
    val pass: Int = 0,
    val fail: Int = 0,
    val lastTs: Long = 0L,
    val history: List<AttemptEntry> = emptyList()
)

@Entity(tableName="groups")
@TypeConverters(Converters::class)
data class StudyGroup(
    @PrimaryKey val name: String,
    val wordIds: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)
