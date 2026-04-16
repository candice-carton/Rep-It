package project.repit.ui.views

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import project.repit.ui.viewModel.ProfileViewModel

@Composable
fun ProfileScreen(navController: NavController, viewModel: ProfileViewModel = viewModel()) {
    val context = LocalContext.current
    val name by viewModel.name
    val avatarUri by viewModel.avatarUri
    val age by viewModel.age
    val heightCm by viewModel.heightCm
    val weightKg by viewModel.weightKg
    val targetWeightKg by viewModel.targetWeightKg
    val activeSeries by viewModel.activeSeries

    var isEditing by rememberSaveable { mutableStateOf(false) }
    var nameInput by remember(name) { mutableStateOf(name) }
    var ageInput by remember(age) { mutableStateOf(age.toString()) }
    var heightInput by remember(heightCm) { mutableStateOf(heightCm.toString()) }
    var weightInput by remember(weightKg) { mutableStateOf(weightKg.toString()) }
    var targetWeightInput by remember(targetWeightKg) { mutableStateOf(targetWeightKg.toString()) }

    val avatarBitmap = remember(avatarUri) {
        avatarUri?.let { uri ->
            runCatching {
                context.contentResolver.openInputStream(Uri.parse(uri))?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            }.getOrNull()
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            viewModel.updateAvatarUri(uri?.toString())
        }
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Mon profil", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                FilledIconButton(
                    onClick = {
                        if (isEditing) {
                            viewModel.updateName(nameInput)
                            ageInput.toIntOrNull()?.let(viewModel::updateAge)
                            heightInput.toIntOrNull()?.let(viewModel::updateHeightCm)
                            weightInput.toFloatOrNull()?.let(viewModel::updateWeightKg)
                            targetWeightInput.toFloatOrNull()?.let(viewModel::updateTargetWeightKg)
                        }
                        isEditing = !isEditing
                    }
                ) {
                    Icon(
                        imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                        contentDescription = if (isEditing) "Valider la modification" else "Modifier mes informations"
                    )
                }
            }
        }

        item {
            Surface(modifier = Modifier.size(96.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)) {
                if (avatarBitmap != null) {
                    Image(
                        bitmap = avatarBitmap.asImageBitmap(),
                        contentDescription = "Photo de profil",
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                    )
                } else {
                    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = (name.firstOrNull()?.uppercase() ?: "R"),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        if (isEditing) {
            item {
                Text(
                    text = "Photo de profil",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { photoPickerLauncher.launch("image/*") }
                )
            }
        }

        item {
            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                label = { Text("Nom affiché") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = isEditing
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = ageInput,
                    onValueChange = { ageInput = it },
                    label = { Text("Âge") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    enabled = isEditing
                )
                OutlinedTextField(
                    value = heightInput,
                    onValueChange = { heightInput = it },
                    label = { Text("Taille (cm)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    enabled = isEditing
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = weightInput,
                    onValueChange = { weightInput = it },
                    label = { Text("Poids (kg)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    enabled = isEditing
                )
                OutlinedTextField(
                    value = targetWeightInput,
                    onValueChange = { targetWeightInput = it },
                    label = { Text("Poids cible") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    enabled = isEditing
                )
            }
        }

        item {
            Text(
                text = "Mes séries commencées",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (activeSeries.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Aucune série commencée pour le moment.",
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        } else {
            items(activeSeries, key = { it.id }) { series ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(series.name, fontWeight = FontWeight.SemiBold)
                        Text("${series.completedSessions}/${series.targetSessions} sessions • ${series.completionRate}% complété")
                    }
                }
            }
        }

        item {
            Text(
                text = if (isEditing) "Appuie sur ✓ pour valider." else "Appuie sur l'icône ✏️ pour modifier tes informations.",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
