package project.repit.data.model

data class Routine(
    val id: Int,
    val title: String,
    val description: String,
    val frequency: String,
    val targetMinutes: Int,
)
