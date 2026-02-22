package project.repit.model

data class Routine(
    val name: String,
    val description: String,
    val category: String,
    val startAt: String,
    val endAt: String,
    val periodicity: String,
    val priority: String
)
