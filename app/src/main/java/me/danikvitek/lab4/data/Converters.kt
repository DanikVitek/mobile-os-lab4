package me.danikvitek.lab4.data

import androidx.room.TypeConverter
import java.util.Date

class Converters {
    @TypeConverter
    fun dateToLong(date: Date): Long = date.time

    @TypeConverter
    fun longToDate(long: Long): Date = Date(long)
}