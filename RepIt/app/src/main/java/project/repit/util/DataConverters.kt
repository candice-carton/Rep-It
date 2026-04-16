package project.repit.util

import androidx.room.TypeConverter

/**
 * Convertit les types de données complexes pour permettre leur stockage dans la base de données Room.
 * Room ne supportant pas nativement les listes, cette classe transforme les listes d'entiers
 * en chaînes de caractères (CSV) et vice-versa.
 */
class DataConverters {
    /**
     * Convertit une liste d'entiers en une chaîne de caractères séparée par des virgules.
     * Utilisé pour stocker [project.repit.model.domain.model.Routine.repeatDays] en base de données.
     *
     * @param value La liste d'entiers à convertir.
     * @return Une chaîne de caractères représentant la liste, ou une chaîne vide.
     */
    @TypeConverter
    fun fromIntList(value: List<Int>): String {
        return value.joinToString(",")
    }

    /**
     * Convertit une chaîne de caractères séparée par des virgules en une liste d'entiers.
     * Recrée la liste d'origine à partir des données stockées.
     *
     * @param value La chaîne de caractères lue depuis la base de données.
     * @return Une liste d'entiers, ou une liste vide si la chaîne est vide.
     */
    @TypeConverter
    fun toIntList(value: String): List<Int> {
        if (value.isEmpty()) return emptyList()
        return value.split(",").map { it.toInt() }
    }
}
