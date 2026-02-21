package project.repit.navigation

import androidx.compose.runtime.Composable
import project.repit.data.repository.RoutineRepository
import project.repit.ui.screens.HomeScreen

@Composable
fun RepitNavHost() {
    HomeScreen(routines = RoutineRepository.getRoutines())
}
