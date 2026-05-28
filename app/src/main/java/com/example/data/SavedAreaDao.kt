package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedAreaDao {
    @Query("SELECT * FROM saved_areas ORDER BY timestamp DESC")
    fun getAllSavedAreas(): Flow<List<SavedArea>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArea(area: SavedArea): Long

    @Query("DELETE FROM saved_areas WHERE id = :id")
    suspend fun deleteAreaById(id: Int)

    @Query("DELETE FROM saved_areas")
    suspend fun deleteAllAreas()
}
