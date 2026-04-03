package project.repit.ui.viewModel

import project.repit.model.domain.model.Routine
import java.util.UUID
import java.util.Calendar

/**
 * Objet d'affichage (équivalent de RoutineVM).
 */
data class RoutineVM(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val category: String,
    val startAt: String = "",
    val endAt: String = "",
    val isAllDay: Boolean = false,
    val periodicity: String,
    val priority: String,
    val progress: Int = 0,
    val isRepetitive: Boolean = false,
    val repeatDays: List<Int> = emptyList(),
    val remainingMillis: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastCompletedDate: Long? = null,
    val isQuantifiable: Boolean = false,
    val targetValue: Float = 0f,
    val currentValue: Float = 0f,
    val unit: String = "",
    val scheduledDate: Long? = null
) {
    /**
     * Calcule le timestamp (à minuit) de la prochaine occurrence de ce défi.
     */
    fun getNextOccurrenceTimestamp(fromTimeMillis: Long = System.currentTimeMillis()): Long {
        val now = Calendar.getInstance()
        now.timeInMillis = fromTimeMillis

        val todayStart = now.clone() as Calendar
        todayStart.set(Calendar.HOUR_OF_DAY, 0)
        todayStart.set(Calendar.MINUTE, 0)
        todayStart.set(Calendar.SECOND, 0)
        todayStart.set(Calendar.MILLISECOND, 0)
        val todayTs = todayStart.timeInMillis

        // Pour un défi ponctuel
        if (!isRepetitive) {
            val date = scheduledDate ?: createdAt
            val cal = Calendar.getInstance()
            cal.timeInMillis = date
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal.timeInMillis
        }

        // Pour un défi répétitif
        if (repeatDays.isEmpty()) return todayTs

        // Jour de la semaine (1=Lundi, 7=Dimanche)
        val currentDayOfWeek = if (now.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) 7 else now.get(Calendar.DAY_OF_WEEK) - 1
        
        // Si prévu aujourd'hui ET pas encore fini aujourd'hui -> c'est aujourd'hui
        if (repeatDays.contains(currentDayOfWeek) && lastCompletedDate != todayTs) {
            return todayTs
        }

        // Sinon, on cherche le prochain jour dans la liste
        val sortedDays = repeatDays.sorted()
        val nextDayThisWeek = sortedDays.firstOrNull { it > currentDayOfWeek }
        
        val daysToAdd = if (nextDayThisWeek != null) {
            nextDayThisWeek - currentDayOfWeek
        } else {
            (7 - currentDayOfWeek) + sortedDays.first()
        }

        val nextDate = todayStart.clone() as Calendar
        nextDate.add(Calendar.DAY_OF_YEAR, daysToAdd)
        return nextDate.timeInMillis
    }

    companion object {
        fun fromEntity(routine: Routine): RoutineVM {
            return RoutineVM(
                id = routine.id,
                name = routine.name,
                description = routine.description,
                category = routine.category,
                startAt = routine.startAt,
                endAt = routine.endAt,
                isAllDay = routine.isAllDay,
                periodicity = routine.periodicity,
                priority = routine.priority,
                progress = routine.progress,
                isRepetitive = routine.isRepetitive,
                repeatDays = routine.repeatDays,
                remainingMillis = routine.remainingMillis,
                createdAt = routine.createdAt,
                lastCompletedDate = routine.lastCompletedDate,
                isQuantifiable = routine.isQuantifiable,
                targetValue = routine.targetValue,
                currentValue = routine.currentValue,
                unit = routine.unit,
                scheduledDate = routine.scheduledDate
            )
        }
    }
}

fun RoutineVM.toEntity(): Routine {
    return Routine(
        id = this.id,
        name = this.name,
        description = this.description,
        category = this.category,
        startAt = this.startAt,
        endAt = this.endAt,
        isAllDay = this.isAllDay,
        periodicity = this.periodicity,
        priority = this.priority,
        progress = this.progress,
        isRepetitive = this.isRepetitive,
        repeatDays = this.repeatDays,
        remainingMillis = this.remainingMillis,
        createdAt = this.createdAt,
        lastCompletedDate = this.lastCompletedDate,
        isQuantifiable = this.isQuantifiable,
        targetValue = this.targetValue,
        currentValue = this.currentValue,
        unit = this.unit,
        scheduledDate = this.scheduledDate
    )
}
