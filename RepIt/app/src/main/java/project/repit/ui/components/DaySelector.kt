package project.repit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import project.repit.ui.theme.SoftViolet

/**
 * Composant permettant de sélectionner les jours de la semaine pour une routine répétitive.
 * Affiche les initiales des jours dans des cercles cliquables.
 *
 * @param selectedDays Liste des entiers représentant les jours sélectionnés (1 pour Lundi, 7 pour Dimanche).
 * @param onDaysChanged Callback appelé lorsqu'un jour est ajouté ou retiré de la sélection.
 */
@Composable
fun DaySelector(selectedDays: List<Int>, onDaysChanged: (List<Int>) -> Unit) {
    val days = listOf("L", "M", "M", "J", "V", "S", "D")
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Répétition", 
            style = MaterialTheme.typography.labelMedium, 
            fontWeight = FontWeight.SemiBold
        )
        Row(
            modifier = Modifier.fillMaxWidth(), 
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            days.forEachIndexed { index, day ->
                val dayNum = index + 1
                val isSelected = selectedDays.contains(dayNum)
                DayCircle(
                    day = day,
                    isSelected = isSelected,
                    onClick = {
                        val newList = if (isSelected) {
                            selectedDays - dayNum
                        } else {
                            selectedDays + dayNum
                        }
                        onDaysChanged(newList.sorted())
                    }
                )
            }
        }
    }
}

/**
 * Un cercle individuel représentant un jour de la semaine.
 */
@Composable
private fun DayCircle(
    day: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(if (isSelected) SoftViolet else MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() }
            .border(1.dp, if (isSelected) SoftViolet else Color.Transparent, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
