package project.repit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import project.repit.ui.theme.RepitTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RepitTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SimpleRoutineScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

data class SimpleRoutine(
    val title: String,
    val description: String,
    val priority: String
)

@Composable
fun SimpleRoutineScreen(modifier: Modifier = Modifier) {
    val routines = listOf(
        SimpleRoutine("Boire 2L d'eau", "Boire de l'eau régulièrement toute la journée.", "Élevée"),
        SimpleRoutine("Marcher 20 minutes", "Faire une marche rapide chaque jour.", "Moyenne"),
        SimpleRoutine("Étirements 10 minutes", "Étirements simples pour le dos et les jambes.", "Moyenne"),
        SimpleRoutine("Dormir avant 23h", "Couper les écrans et se coucher plus tôt.", "Faible")
    ).sortedBy { priorityWeight(it.priority) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Routines débutant",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        items(routines) { routine ->
            RoutineBox(routine)
        }
    }
}

@Composable
private fun RoutineBox(routine: SimpleRoutine) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = routine.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = routine.description, style = MaterialTheme.typography.bodyMedium)
            Text(text = "Priorité : ${routine.priority}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun priorityWeight(priority: String): Int = when (priority) {
    "Élevée" -> 0
    "Moyenne" -> 1
    "Faible" -> 2
    else -> 3
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RoutinePreview() {
    RepitTheme {
        SimpleRoutineScreen()
    }
}
