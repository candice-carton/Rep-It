package project.repit.util

import android.content.Context
import project.repit.model.Routine
import java.time.LocalDateTime

object RoutineFileUtil {
    private const val FILE_NAME = "routines.txt"
    private const val FIELD_SEPARATOR = "\t"

    fun readRoutines(context: Context): List<Routine> {
        val file = context.getFileStreamPath(FILE_NAME)
        if (!file.exists()) return emptyList()

        return file.readLines()
            .mapNotNull { parseRoutine(it) }
    }

    fun saveRoutines(context: Context, routines: List<Routine>) {
        context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE).bufferedWriter().use { writer ->
            routines.forEach { routine ->
                writer.appendLine(serializeRoutine(routine))
            }
        }
    }

    private fun serializeRoutine(routine: Routine): String = listOf(
        routine.name.escape(),
        routine.description.escape(),
        routine.category.escape(),
        routine.startAt.toString(),
        routine.endAt.toString(),
        routine.periodicity.escape(),
        routine.priority.escape()
    ).joinToString(FIELD_SEPARATOR)

    private fun parseRoutine(line: String): Routine? {
        val parts = line.split(FIELD_SEPARATOR)
        if (parts.size != 7) return null

        return runCatching {
            Routine(
                name = parts[0].unescape(),
                description = parts[1].unescape(),
                category = parts[2].unescape(),
                startAt = LocalDateTime.parse(parts[3]),
                endAt = LocalDateTime.parse(parts[4]),
                periodicity = parts[5].unescape(),
                priority = parts[6].unescape()
            )
        }.getOrNull()
    }

    private fun String.escape(): String = replace("\\", "\\\\").replace("\t", "\\t")

    private fun String.unescape(): String = buildString {
        var index = 0
        while (index < this@unescape.length) {
            val current = this@unescape[index]
            if (current == '\\' && index + 1 < this@unescape.length) {
                val next = this@unescape[index + 1]
                append(if (next == 't') '\t' else next)
                index += 2
            } else {
                append(current)
                index++
            }
        }
    }
}
