package project.repit.model.data

import android.content.Context
import java.time.LocalDate

class UserPreferences(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getProfileName(): String = prefs.getString(KEY_PROFILE_NAME, "Rép-it User") ?: "Rép-it User"

    fun setProfileName(name: String) {
        prefs.edit().putString(KEY_PROFILE_NAME, name).apply()
    }

    fun getProfileAvatarUri(): String? = prefs.getString(KEY_PROFILE_AVATAR_URI, null)

    fun setProfileAvatarUri(uri: String?) {
        prefs.edit().putString(KEY_PROFILE_AVATAR_URI, uri).apply()
    }

    fun getLastDailyChallengeEpochDay(): Long = prefs.getLong(KEY_LAST_DAILY_EPOCH_DAY, Long.MIN_VALUE)

    fun setLastDailyChallengeEpochDay(epochDay: Long) {
        prefs.edit().putLong(KEY_LAST_DAILY_EPOCH_DAY, epochDay).apply()
    }

    fun getAge(): Int = prefs.getInt(KEY_AGE, 25)
    fun setAge(value: Int) { prefs.edit().putInt(KEY_AGE, value).apply() }

    fun getHeightCm(): Int = prefs.getInt(KEY_HEIGHT_CM, 170)
    fun setHeightCm(value: Int) { prefs.edit().putInt(KEY_HEIGHT_CM, value).apply() }

    fun getCurrentWeightKg(): Float = prefs.getFloat(KEY_WEIGHT_KG, 70f)
    fun setCurrentWeightKg(value: Float) { prefs.edit().putFloat(KEY_WEIGHT_KG, value).apply() }

    fun getTargetWeightKg(): Float = prefs.getFloat(KEY_TARGET_WEIGHT_KG, 68f)
    fun setTargetWeightKg(value: Float) { prefs.edit().putFloat(KEY_TARGET_WEIGHT_KG, value).apply() }

    fun getWeightLogs(): List<WeightLog> {
        val raw = prefs.getString(KEY_WEIGHT_LOGS, "") ?: ""
        if (raw.isBlank()) return emptyList()
        return raw.split(";").mapNotNull { token ->
            val parts = token.split(":")
            if (parts.size != 2) return@mapNotNull null
            val epoch = parts[0].toLongOrNull() ?: return@mapNotNull null
            val value = parts[1].toFloatOrNull() ?: return@mapNotNull null
            WeightLog(epochDay = epoch, weightKg = value)
        }.sortedBy { it.epochDay }
    }

    fun addWeightLog(value: Float, date: LocalDate = LocalDate.now()) {
        val logs = getWeightLogs().toMutableList()
        logs.removeAll { it.epochDay == date.toEpochDay() }
        logs.add(WeightLog(date.toEpochDay(), value))
        saveWeightLogs(logs.sortedBy { it.epochDay })
        setCurrentWeightKg(value)
    }

    fun getWaterLogs(): List<WaterLog> {
        val raw = prefs.getString(KEY_WATER_LOGS, "") ?: ""
        if (raw.isBlank()) return emptyList()
        return raw.split(";").mapNotNull { token ->
            val parts = token.split(":")
            if (parts.size != 2) return@mapNotNull null
            val epoch = parts[0].toLongOrNull() ?: return@mapNotNull null
            val value = parts[1].toFloatOrNull() ?: return@mapNotNull null
            WaterLog(epochDay = epoch, liters = value)
        }.sortedBy { it.epochDay }
    }

    fun addWaterLog(value: Float, date: LocalDate = LocalDate.now()) {
        val logs = getWaterLogs().toMutableList()
        logs.removeAll { it.epochDay == date.toEpochDay() }
        logs.add(WaterLog(date.toEpochDay(), value))
        saveWaterLogs(logs.sortedBy { it.epochDay })
    }

    private fun saveWeightLogs(entries: List<WeightLog>) {
        val serialized = entries.joinToString(";") { "${it.epochDay}:${it.weightKg}" }
        prefs.edit().putString(KEY_WEIGHT_LOGS, serialized).apply()
    }

    private fun saveWaterLogs(entries: List<WaterLog>) {
        val serialized = entries.joinToString(";") { "${it.epochDay}:${it.liters}" }
        prefs.edit().putString(KEY_WATER_LOGS, serialized).apply()
    }

    companion object {
        private const val PREFS_NAME = "repit_user_prefs"
        private const val KEY_PROFILE_NAME = "profile_name"
        private const val KEY_PROFILE_AVATAR_URI = "profile_avatar_uri"
        private const val KEY_LAST_DAILY_EPOCH_DAY = "last_daily_epoch_day"
        private const val KEY_AGE = "age"
        private const val KEY_HEIGHT_CM = "height_cm"
        private const val KEY_WEIGHT_KG = "weight_kg"
        private const val KEY_TARGET_WEIGHT_KG = "target_weight_kg"
        private const val KEY_WEIGHT_LOGS = "weight_logs"
        private const val KEY_WATER_LOGS = "water_logs"
    }
}

data class WeightLog(val epochDay: Long, val weightKg: Float)
data class WaterLog(val epochDay: Long, val liters: Float)
