package project.repit.model.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Représente une routine ou un défi utilisateur.
 */
@Entity(tableName = "routines")
data class Routine(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
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
    val repeatDays: List<Int> = emptyList(), // 1 = Lundi, 7 = Dimanche
    val remainingMillis: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastCompletedDate: Long? = null, // Timestamp du jour de dernière complétion (minuit)
    
    val isQuantifiable: Boolean = false,
    val targetValue: Float = 0f,
    val currentValue: Float = 0f,
    val unit: String = "",
    
    val scheduledDate: Long? = null, // Pour les défis non-répétitifs à une date précise
    val isDailySuggestion: Boolean = false // Défi proposé automatiquement par l'application
)
