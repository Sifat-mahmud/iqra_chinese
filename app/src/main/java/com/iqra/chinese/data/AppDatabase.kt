package com.iqra.chinese.data

import android.content.Context
import androidx.room.*

@Database(entities=[AttemptRecord::class, StudyGroup::class], version=1, exportSchema=false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun attempts(): AttemptDao
    abstract fun groups(): GroupDao
    companion object {
        @Volatile private var I: AppDatabase? = null
        fun get(ctx: Context) = I ?: synchronized(this) {
            Room.databaseBuilder(ctx.applicationContext, AppDatabase::class.java, "iqra.db").build().also { I = it }
        }
    }
}
