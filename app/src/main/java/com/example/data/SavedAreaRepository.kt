package com.example.data

import kotlinx.coroutines.flow.Flow

class SavedAreaRepository(private val savedAreaDao: SavedAreaDao) {
    val allSavedAreas: Flow<List<SavedArea>> = savedAreaDao.getAllSavedAreas()

    suspend fun insert(area: SavedArea): Long {
        return savedAreaDao.insertArea(area)
    }

    suspend fun deleteById(id: Int) {
        savedAreaDao.deleteAreaById(id)
    }

    suspend fun deleteAll() {
        savedAreaDao.deleteAllAreas()
    }
}
