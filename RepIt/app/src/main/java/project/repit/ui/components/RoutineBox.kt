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
    val imageName = routine.category.lowercase().replace("é", "e").replace("è", "e").replace("ê", "e").replace("-","_")
    val imageRes = context.resources.getIdentifier(imageName, "drawable", context.packageName)
    
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    val now = Calendar.getInstance()
    val todayStart = now.clone() as Calendar
    todayStart.set(Calendar.HOUR_OF_DAY, 0); todayStart.set(Calendar.MINUTE, 0); todayStart.set(Calendar.SECOND, 0); todayStart.set(Calendar.MILLISECOND, 0)
    val todayTs = todayStart.timeInMillis

    // Date de l'occurrence affichée
    val displayDateTs = forcedDate ?: if (isUpcoming) {
        val tomorrowStart = todayTs + 24 * 3600 * 1000
        routine.getNextOccurrenceTimestamp(tomorrowStart)
    } else {
        routine.getNextOccurrenceTimestamp(todayTs)
    }
    
    val dateStr = sdf.format(Date(displayDateTs))
    
    // Un défi est considéré "terminé aujourd'hui" uniquement si on affiche la date d'aujourd'hui (ou avant) 
    // et qu'il a été complété aujourd'hui.
    val isCompletedToday = displayDateTs <= todayTs && routine.lastCompletedDate == todayTs
    val isFuture = displayDateTs > todayTs && !isCompletedToday
    val isStarted = (routine.remainingMillis ?: 0L) > 0

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isUpcoming -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                isCompletedToday -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isUpcoming || isCompletedToday) 0.dp else 2.dp)
    ) {
        val verticalPadding = if (isUpcoming) 12.dp else 16.dp
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = verticalPadding), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                    Box(modifier = Modifier.size(if (isUpcoming) 44.dp else 52.dp).clip(RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                        if (imageRes != 0) {
                            Image(
                                painter = painterResource(id = imageRes), contentDescription = routine.category,
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp)).border(1.dp, PureWhite.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                                contentScale = ContentScale.FillBounds,
                                alpha = if (isUpcoming) 0.7f else 1f
                            )
                        } else {
                            val defaultIcon = when(routine.category) {
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
                            Icon(defaultIcon, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = if (isUpcoming) 0.5f else 1f))
                        }
                    }

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
                                Icon(Icons.Default.CheckCircle, contentDescription = "Terminé", tint = Color(0xFF4CAF50), modifier = Modifier.size(18.dp))
                            }
                        }
                        val subtitle = if (isUpcoming) {
                            "Prochaine : $dateStr"
                        } else if (routine.isQuantifiable) {
                            "${routine.currentValue} / ${routine.targetValue} ${routine.unit} • $dateStr"
                        } else if (routine.isAllDay) {
                            "Toute la journée • $dateStr"
                        } else {
                            "${routine.startAt} - ${routine.endAt} • $dateStr"
                        }
                        Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // On affiche le bouton Modifier sauf si l'instance affichée est déjà terminée
                    if (!isCompletedToday) {
                        IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "Modifier", modifier = Modifier.size(20.dp)) }
                    }
                    IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Supprimer", modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)) }
                }
            }

            if (!isUpcoming) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val progressValue = routine.progress
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Progression", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
                        Text("$progressValue%", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                    LinearProgressIndicator(
                        progress = progressValue / 100f,
                        modifier = Modifier.fillMaxWidth().height(if (isCompletedToday) 6.dp else 8.dp).clip(RoundedCornerShape(4.dp)),
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
            } else {
                Text(
                    text = "Prévu pour le $dateStr",
                    style = MaterialTheme.typography.labelSmall,
                    color = SoftViolet.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
