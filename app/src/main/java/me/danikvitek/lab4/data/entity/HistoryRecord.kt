package me.danikvitek.lab4.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "history",
    indices = [Index("title", "artist", unique = true)],
)
data class HistoryRecord(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val title: String,
    val artist: String,
    @ColumnInfo(defaultValue = "CURRENT_TIMESTAMP") val timestamp: Date = Date(),
)
