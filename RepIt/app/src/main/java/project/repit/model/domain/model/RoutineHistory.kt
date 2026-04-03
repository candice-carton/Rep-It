package project.repit.model.domain.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "routine_history",
    foreignKeys = [
        ForeignKey(
            entity = Routine::class,
            parentColumns = ["id"],
            childColumns = ["routineId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class RoutineHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val routineId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val durationMillis: Long,
    val isCompleted: Boolean,
    val progress: Int
)
