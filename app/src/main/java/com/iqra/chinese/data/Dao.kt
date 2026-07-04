package com.iqra.chinese.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao interface AttemptDao {
    @Query("SELECT * FROM attempts WHERE itemId=:id") suspend fun get(id: String): AttemptRecord?
    @Query("SELECT * FROM attempts") fun all(): Flow<List<AttemptRecord>>
    @Query("SELECT * FROM attempts") suspend fun allOnce(): List<AttemptRecord>
    @Query("SELECT * FROM attempts WHERE level=:l AND itemType='word'") fun forLevel(l: Int): Flow<List<AttemptRecord>>
    @Query("SELECT COUNT(*) FROM attempts WHERE itemType='word' AND pass>0") fun masteredCount(): Flow<Int>
    @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun upsert(r: AttemptRecord)
    @Query("DELETE FROM attempts") suspend fun clear()
}

@Dao interface GroupDao {
    @Query("SELECT * FROM groups ORDER BY createdAt DESC") fun all(): Flow<List<StudyGroup>>
    @Query("SELECT * FROM groups WHERE name=:n") suspend fun get(n: String): StudyGroup?
    @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun upsert(g: StudyGroup)
    @Delete suspend fun delete(g: StudyGroup)
    @Query("DELETE FROM groups") suspend fun clear()
}
