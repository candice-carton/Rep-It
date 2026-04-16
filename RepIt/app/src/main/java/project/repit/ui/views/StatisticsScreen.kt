package project.repit.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import project.repit.model.data.WeightLog
import project.repit.ui.viewModel.StatisticsViewModel

@Composable
fun StatisticsScreen(
    navController: NavController,
    viewModel: StatisticsViewModel = viewModel()
) {
    val uiState by viewModel.uiState

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Mes statistiques", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }

        item { CardStat("Défis totaux", uiState.totalChallenges.toString()) }
        item { CardStat("Défis complétés", uiState.completedChallenges.toString()) }
        item { CardStat("Taux de complétion", "${uiState.completionRate}%") }
        item { CardStat("Progression moyenne", "${uiState.averageProgress}%") }
        item { CardStat("Hydratation du jour", "${uiState.waterTodayLiters} L") }
        item { CardStat("Hydratation moyenne (7j)", String.format("%.2f L", uiState.waterWeeklyAverage)) }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Par catégorie", fontWeight = FontWeight.SemiBold)
                    if (uiState.byCategory.isEmpty()) {
                        Text("Aucune donnée pour l'instant")
                    } else {
                        uiState.byCategory.forEach { (category, count) ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(category)
                                Text(count.toString(), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        item {
            WeightSection(
                currentWeight = uiState.currentWeightKg,
                targetWeight = uiState.targetWeightKg,
                logs = uiState.weightLogs,
                onAddWeight = viewModel::addWeightEntry
            )
        }

        item { WaterSection(onAddWater = viewModel::addWaterEntry) }
        item { Box(modifier = Modifier.height(8.dp)) }
    }
}

@Composable
private fun CardStat(label: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label)
            Text(value, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun WaterSection(onAddWater: (Float) -> Unit) {
    var input by remember { mutableStateOf("") }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Ajouter une donnée d'eau (L)", fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text("Ex: 2.3") },
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = {
                    input.toFloatOrNull()?.let(onAddWater)
                    input = ""
                }) { Text("Ajouter") }
            }
        }
    }
}

@Composable
private fun WeightSection(
    currentWeight: Float,
    targetWeight: Float,
    logs: List<WeightLog>,
    onAddWeight: (Float) -> Unit
) {
    var weightInput by remember { mutableStateOf("") }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Suivi du poids", fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Actuel: ${currentWeight} kg")
                Text("Cible: ${targetWeight} kg")
            }
            SimpleWeightChart(logs = logs, targetWeight = targetWeight)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = weightInput,
                    onValueChange = { weightInput = it },
                    label = { Text("Nouveau poids") },
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = {
                    weightInput.toFloatOrNull()?.let(onAddWeight)
                    weightInput = ""
                }) { Text("Ajouter") }
            }
        }
    }
}

@Composable
private fun SimpleWeightChart(logs: List<WeightLog>, targetWeight: Float) {
    val chartValues = if (logs.isEmpty()) {
        listOf(targetWeight.takeIf { it > 0f } ?: 0f)
    } else {
        logs.map { it.weightKg }
    }

    val minValue = (minOf(chartValues.minOrNull() ?: 0f, targetWeight) - 1f).coerceAtLeast(0f)
    val maxValue = maxOf(chartValues.maxOrNull() ?: 1f, targetWeight) + 1f

    Box(modifier = Modifier.fillMaxWidth().height(140.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val stepX = if (chartValues.size <= 1) width else width / (chartValues.size - 1)

            val toY: (Float) -> Float = { value ->
                val normalized = (value - minValue) / (maxValue - minValue).coerceAtLeast(0.001f)
                height - (normalized * height)
            }

            val targetY = toY(targetWeight)
            drawLine(
                color = Color(0xFFB39DDB),
                start = androidx.compose.ui.geometry.Offset(0f, targetY),
                end = androidx.compose.ui.geometry.Offset(width, targetY),
                strokeWidth = 3f
            )

            val path = Path()
            chartValues.forEachIndexed { index, weight ->
                val x = index * stepX
                val y = toY(weight)
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path = path, color = Color(0xFF6C63FF))
        }

        if (logs.isEmpty()) {
            Text(
                text = "Graphique prêt : ajoute une valeur pour démarrer ta courbe.",
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
