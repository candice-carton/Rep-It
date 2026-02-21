package project.repit.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// CORRECTION : Le chemin d'importation est ajusté pour correspondre à la structure de votre projet.
import project.repit.data.model.Routine

@Composable
fun RoutineCard(routine: Routine) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // CORRECTION : Utilise 'routine.title' qui existe dans votre data class.
            Text(
                text = routine.title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(text = routine.description, fontSize = 14.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Fréquence: ${routine.frequency}")

            Text(text = "Objectif: ${routine.targetMinutes} minutes")

            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(progress = 0.5f, modifier = Modifier.fillMaxWidth())
        }
    }
}
