package me.danikvitek.lab4.data.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import me.danikvitek.lab4.data.entity.HistoryRecord
import java.util.Date

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history ORDER BY timestamp DESC")
    fun getHistory(): Flow<List<HistoryRecord>>

    @Query("INSERT INTO history (title, artist, timestamp) VALUES (:title, :artist, :timestamp)")
    suspend fun addRecord(title: String, artist: String, timestamp: Date = Date())

    @Query("SELECT * FROM history ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastRecord(): HistoryRecord
}