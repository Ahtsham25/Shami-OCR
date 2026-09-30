package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PaperDao {

    @Query("SELECT * FROM saved_papers ORDER BY timestamp DESC")
    fun getAllPapers(): Flow<List<PaperEntity>>

    @Query("SELECT * FROM saved_papers WHERE id = :id")
    suspend fun getPaperById(id: String): PaperEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaper(paper: PaperEntity)

    @Query("DELETE FROM saved_papers WHERE id = :id")
    suspend fun deletePaperById(id: String)

    @Query("DELETE FROM saved_papers")
    suspend fun clearAll()
}
