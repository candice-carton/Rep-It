package project.repit.data.repository

import project.repit.data.model.Routine
import project.repit.data.util.RoutineUtil

object RoutineRepository {
    fun getRoutines(): List<Routine> = RoutineUtil.defaultRoutines
}
