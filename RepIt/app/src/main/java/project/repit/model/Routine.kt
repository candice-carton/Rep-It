package project.repit.model

import java.time.LocalDateTime

data class Routine(
    val name: String,
    val description: String,
    val category: String,
    val startAt: LocalDateTime,
    val endAt: LocalDateTime,
    val periodicity: String,
    val priority: String
)
