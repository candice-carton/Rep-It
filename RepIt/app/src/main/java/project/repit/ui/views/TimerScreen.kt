package project.repit.ui.views

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import project.repit.model.data.AppDatabase
import project.repit.model.domain.useCase.GetRoutineUseCase
import project.repit.models.RoutineRepository
import project.repit.ui.theme.SoftViolet
import project.repit.ui.viewModel.TimerViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    navController: NavController,
    routineId: String
) {
    val context = LocalContext.current
    val repository = remember { RoutineRepository(AppDatabase.getDatabase(context).routineDao()) }
    val getRoutineUseCase = remember { GetRoutineUseCase(repository) }
    
    val viewModel: TimerViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TimerViewModel(routineId, repository, getRoutineUseCase) as T
            }
        }
    )

    val routineName by viewModel.routineName
    val timeLeft by viewModel.timeLeft
    val isRunning by viewModel.isRunning

    val minutes = (timeLeft / 1000) / 60
    val seconds = (timeLeft / 1000) % 60
    val timeString = String.format("%02d:%02d", minutes, seconds)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Session en cours", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = routineName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = SoftViolet
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Text(
                text = timeString,
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 80.sp),
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(64.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally)
            ) {
                LargeFloatingActionButton(
                    onClick = { viewModel.toggleTimer() },
                    containerColor = SoftViolet,
                    contentColor = Color.White,
                    shape = androidx.compose.foundation.shape.CircleShape
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isRunning) "Pause" else "Reprendre",
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(64.dp))
            
            Button(
                onClick = { 
                    viewModel.finishRoutine {
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SoftViolet)
            ) {
                Text("Terminer le défi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "Appuie sur terminer une fois que tu as fini !",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}
