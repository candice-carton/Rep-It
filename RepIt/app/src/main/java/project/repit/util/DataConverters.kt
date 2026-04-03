package project.repit.util

import androidx.room.TypeConverter

/**
 * Convertit les types complexes (comme les listes) pour qu'ils soient stockables dans Room.
 */
class DataConverters {
    @TypeConverter
    fun fromIntList(value: List<Int>): String {
        return value.joinToString(",")
    }

    @TypeConverter
    fun toIntList(value: String): List<Int> {
        if (value.isEmpty()) return emptyList()
        return value.split(",").map { it.toInt() }
    }
}
