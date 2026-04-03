package project.repit.model.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import project.repit.model.domain.model.Routine
import project.repit.model.domain.model.RoutineHistory

@Dao
interface RoutinesDao {
    @Query("SELECT * FROM routines")
    fun getRoutines(): Flow<List<Routine>>

    @Query("SELECT * FROM routines WHERE id = :id")
    suspend fun getRoutineById(id: String): Routine?

    @Upsert
    suspend fun upsertRoutine(routine: Routine)

    @Delete
    suspend fun deleteRoutine(routine: Routine)

    // Gestion de l'historique
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: RoutineHistory)

    @Query("SELECT * FROM routine_history WHERE routineId = :routineId ORDER BY timestamp DESC")
    fun getHistoryForRoutine(routineId: String): Flow<List<RoutineHistory>>

    @Query("SELECT * FROM routine_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<RoutineHistory>>
}
