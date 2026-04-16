package project.repit.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import project.repit.ui.theme.PureWhite
import project.repit.ui.theme.SoftViolet
import project.repit.ui.viewModel.RoutineVM
import java.text.SimpleDateFormat
import java.util.*

/**
 * Composant de carte affichant les détails d'une routine.
 * Gère l'affichage des informations, de la progression et des actions (éditer, supprimer, démarrer).
 * S'adapte visuellement selon si la routine est pour aujourd'hui, terminée ou à venir.
 *
 * @param routine Les données de la routine à afficher.
 * @param onEdit Action déclenchée lors du clic sur le bouton d'édition.
 * @param onDelete Action déclenchée lors du clic sur le bouton de suppression.
 * @param onStart Action optionnelle déclenchée pour démarrer ou progresser dans la routine.
 * @param isUpcoming Indique si la routine est affichée dans une section "À venir".
 * @param forcedDate Timestamp optionnel pour forcer l'affichage d'une date spécifique.
 */
@Composable
fun RoutineBox(
    routine: RoutineVM,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onStart: (() -> Unit)? = null,
    isUpcoming: Boolean = false,
    forcedDate: Long? = null
) {
    val context = LocalContext.current
    
    // Résolution de l'image de catégorie
    val imageName = routine.category.lowercase()
        .replace("é", "e")
        .replace("è", "e")
        .replace("ê", "e")
        .replace("-", "_")
    val imageRes = context.resources.getIdentifier(imageName, "drawable", context.packageName)
    
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    val now = Calendar.getInstance()
    val todayStart = now.clone() as Calendar
    todayStart.set(Calendar.HOUR_OF_DAY, 0)
    todayStart.set(Calendar.MINUTE, 0)
    todayStart.set(Calendar.SECOND, 0)
    todayStart.set(Calendar.MILLISECOND, 0)
    val todayTs = todayStart.timeInMillis

    // Date de l'occurrence affichée
    val displayDateTs = forcedDate ?: if (isUpcoming) {
        val tomorrowStart = todayTs + 24 * 3600 * 1000
        routine.getNextOccurrenceTimestamp(tomorrowStart)
    } else {
        routine.getNextOccurrenceTimestamp(todayTs)
    }
    
    val dateStr = sdf.format(Date(displayDateTs))
    
    // États de la routine pour le style visuel
    val isCompletedToday = displayDateTs <= todayTs && routine.lastCompletedDate == todayTs
    val isFuture = displayDateTs > todayTs && !isCompletedToday
    val isStarted = (routine.remainingMillis ?: 0L) > 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isUpcoming -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                isCompletedToday -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isUpcoming || isCompletedToday) 0.dp else 2.dp
        )
    ) {
        val verticalPadding = if (isUpcoming) 12.dp else 16.dp
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = verticalPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Informations principales (Icône + Titre + Sous-titre)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    RoutineIcon(imageRes, routine.category, isUpcoming)

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = routine.name, 
                                style = if (isUpcoming) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium, 
                                fontWeight = FontWeight.Bold,
                                color = if (isUpcoming) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurface
                            )
                            if (isCompletedToday) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.CheckCircle, 
                                    contentDescription = "Terminé", 
                                    tint = Color(0xFF4CAF50), 
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        
                        val subtitle = getSubtitle(routine, isUpcoming, dateStr)
                        Text(
                            text = subtitle, 
                            style = MaterialTheme.typography.bodySmall, 
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }

                // Actions d'édition et suppression
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!isCompletedToday) {
                        IconButton(onClick = onEdit) { 
                            Icon(Icons.Default.Edit, "Modifier", modifier = Modifier.size(20.dp)) 
                        }
                    }
                    IconButton(onClick = onDelete) { 
                        Icon(
                            Icons.Default.Delete, 
                            "Supprimer", 
                            modifier = Modifier.size(20.dp), 
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                        ) 
                    }
                }
            }

            // Section Progression et bouton de démarrage (uniquement si pas une routine "À venir")
            if (!isUpcoming) {
                ProgressSection(routine, isCompletedToday, isStarted, isFuture, displayDateTs, todayTs, onStart)
            } else {
                UpcomingInfo(dateStr)
            }
        }
    }
}

/**
 * Affiche l'icône de la routine (image ou icône par défaut).
 */
@Composable
private fun RoutineIcon(imageRes: Int, category: String, isUpcoming: Boolean) {
    Box(
        modifier = Modifier
            .size(if (isUpcoming) 44.dp else 52.dp)
            .clip(RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (imageRes != 0) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = category,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, PureWhite.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                contentScale = ContentScale.FillBounds,
                alpha = if (isUpcoming) 0.7f else 1f
            )
        } else {
            val defaultIcon = when(category) {
                "Personnel" -> Icons.Default.Person
                "Alimentation" -> Icons.Default.Restaurant
                "Sport" -> Icons.Default.FitnessCenter
                "Travail" -> Icons.Default.Work
                "Santé" -> Icons.Default.MedicalServices
                "Maison" -> Icons.Default.Home
                "Études" -> Icons.Default.School
                "Loisirs" -> Icons.Default.Gamepad
                else -> Icons.Default.EmojiEvents
            }
            Icon(
                defaultIcon, 
                contentDescription = null, 
                tint = MaterialTheme.colorScheme.primary.copy(alpha = if (isUpcoming) 0.5f else 1f)
            )
        }
    }
}

/**
 * Génère le sous-titre descriptif de la routine.
 */
private fun getSubtitle(routine: RoutineVM, isUpcoming: Boolean, dateStr: String): String {
    return if (isUpcoming) {
        "Prochaine : $dateStr"
    } else if (routine.isQuantifiable) {
        "${routine.currentValue} / ${routine.targetValue} ${routine.unit} • $dateStr"
    } else if (routine.isAllDay) {
        "Toute la journée • $dateStr"
    } else {
        "${routine.startAt} - ${routine.endAt} • $dateStr"
    }
}

/**
 * Affiche la barre de progression et le bouton d'action.
 */
@Composable
private fun ProgressSection(
    routine: RoutineVM,
    isCompletedToday: Boolean,
    isStarted: Boolean,
    isFuture: Boolean,
    displayDateTs: Long,
    todayTs: Long,
    onStart: (() -> Unit)?
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val progressValue = routine.progress
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Progression", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
            Text("$progressValue%", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        }
        LinearProgressIndicator(
            progress = progressValue / 100f,
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isCompletedToday) 6.dp else 8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = if (isCompletedToday) Color(0xFF4CAF50) else SoftViolet,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        
        if (!isCompletedToday && onStart != null) {
            Button(
                onClick = onStart,
                modifier = Modifier.align(Alignment.End).padding(top = 4.dp),
                shape = RoundedCornerShape(10.dp),
                enabled = !isFuture || (displayDateTs > todayTs && routine.isRepetitive)
            ) {
                val buttonText = when {
                    routine.isQuantifiable -> "Ajouter"
                    routine.isAllDay -> "Terminer"
                    isStarted -> "Reprendre"
                    else -> "Démarrer"
                }
                Text(buttonText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

/**
 * Affiche l'information de date pour les routines à venir.
 */
@Composable
private fun UpcomingInfo(dateStr: String) {
    Text(
        text = "Prévu pour le $dateStr",
        style = MaterialTheme.typography.labelSmall,
        color = SoftViolet.copy(alpha = 0.8f),
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(top = 4.dp)
    )
}
