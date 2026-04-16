package project.repit.model.data.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Calendar
import kotlin.math.max

/**
 * Source locale pour récupérer le nombre de pas via TYPE_STEP_COUNTER.
 */
class PedometerDataSource(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepCounter: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    val hasStepCounter: Boolean get() = stepCounter != null

    private val _stepsToday = MutableStateFlow(0)
    val stepsToday: StateFlow<Int> = _stepsToday

    private var dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
    private var initialCount: Float? = null

    fun start() {
        stepCounter?.let { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val sensorValue = event?.values?.firstOrNull() ?: return
        val today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)

        if (today != dayOfYear) {
            dayOfYear = today
            initialCount = sensorValue
            _stepsToday.value = 0
            return
        }

        if (initialCount == null) {
            initialCount = sensorValue
        }

        val steps = max(0f, sensorValue - (initialCount ?: sensorValue)).toInt()
        _stepsToday.value = steps
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}
