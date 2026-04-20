package project.repit.model.domain.useCase

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import project.repit.model.data.RoutineRepository
import project.repit.model.domain.model.Routine

class UpsertRoutineUseCaseTest {

    private lateinit var repository: RoutineRepository
    private lateinit var upsertRoutineUseCase: UpsertRoutineUseCase

    @Before
    fun setUp() {
        repository = mock(RoutineRepository::class.java)
        upsertRoutineUseCase = UpsertRoutineUseCase(repository)
    }

    @Test
    fun `when name is blank should throw exception`() {
        val routine = Routine(
            name = "",
            description = "",
            category = "Sport",
            priority = "Haute",
            periodicity = "Quotidien"
        )
        
        assertThrows(RoutineException::class.java) {
            runBlocking { upsertRoutineUseCase(routine) }
        }
    }

    @Test
    fun `when isRepetitive is true and no days selected should throw exception`() {
        val routine = Routine(
            name = "Test", 
            description = "",
            category = "Sport", 
            priority = "Haute", 
            periodicity = "Série",
            isRepetitive = true, 
            repeatDays = emptyList()
        )
        
        assertThrows(RoutineException::class.java) {
            runBlocking { upsertRoutineUseCase(routine) }
        }
    }

    @Test
    fun `when isQuantifiable is true and targetValue is zero should throw exception`() {
        val routine = Routine(
            name = "Boire de l'eau",
            description = "",
            category = "Santé",
            priority = "Moyenne",
            periodicity = "Quotidien",
            isQuantifiable = true,
            targetValue = 0f,
            unit = "L"
        )
        
        assertThrows(RoutineException::class.java) {
            runBlocking { upsertRoutineUseCase(routine) }
        }
    }
}
