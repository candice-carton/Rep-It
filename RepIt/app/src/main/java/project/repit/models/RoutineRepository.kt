package project.repit.models

import kotlinx.coroutines.flow.Flow
import project.repit.model.data.RoutinesDao
import project.repit.model.domain.model.Routine
import project.repit.model.domain.model.RoutineHistory

/**
 * Repository qui fait le lien entre la source de données (Room) et le ViewModel.
 */
class RoutineRepository(private val routineDao: RoutinesDao) {

    /**
     * Récupère toutes les routines sous forme de Flow pour une mise à jour en temps réel de l'UI.
     */
    val allRoutines: Flow<List<Routine>> = routineDao.getRoutines()

    /**
     * Récupère tout l'historique des routines.
     */
    val allHistory: Flow<List<RoutineHistory>> = routineDao.getAllHistory()

    /**
     * Récupère une routine spécifique par son ID.
     */
    suspend fun getRoutineById(id: String): Routine? {
        return routineDao.getRoutineById(id)
    }

    /**
     * Insère ou met à jour une routine dans la base de données.
     */
    suspend fun upsert(routine: Routine) {
        routineDao.upsertRoutine(routine)
    }

    /**
     * Supprime une routine de la base de données.
     */
    suspend fun delete(routine: Routine) {
        routineDao.deleteRoutine(routine)
    }

    /**
     * Insère une nouvelle entrée d'historique.
     */
    suspend fun insertHistory(history: RoutineHistory) {
        routineDao.insertHistory(history)
    }

    /**
     * Récupère l'historique pour une routine spécifique.
     */
    fun getHistoryForRoutine(routineId: String): Flow<List<RoutineHistory>> {
        return routineDao.getHistoryForRoutine(routineId)
    }
}
