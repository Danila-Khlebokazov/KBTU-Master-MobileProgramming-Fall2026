package com.example.stylebook.data.local

import androidx.room.TypeConverter
import com.example.stylebook.data.model.Occasion
import java.time.LocalDate

class Converters {
    @TypeConverter
    fun occasionsToString(occasions: Set<Occasion>): String = occasions.sorted().joinToString(",") { it.name }

    @TypeConverter
    fun stringToOccasions(value: String): Set<Occasion> =
        value.split(',').filter { it.isNotBlank() }.mapTo(mutableSetOf()) { Occasion.valueOf(it) }

    @TypeConverter
    fun dateToEpochDay(date: LocalDate): Long = date.toEpochDay()

    @TypeConverter
    fun epochDayToDate(epochDay: Long): LocalDate = LocalDate.ofEpochDay(epochDay)
}
