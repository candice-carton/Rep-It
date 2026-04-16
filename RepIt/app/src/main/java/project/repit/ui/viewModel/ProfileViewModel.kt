package project.repit.ui.viewModel

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import project.repit.model.data.UserPreferences

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val userPreferences = UserPreferences(application)

    private val _name = mutableStateOf(userPreferences.getProfileName())
    val name: State<String> = _name

    private val _avatarUri = mutableStateOf(userPreferences.getProfileAvatarUri())
    val avatarUri: State<String?> = _avatarUri

    private val _age = mutableStateOf(userPreferences.getAge())
    val age: State<Int> = _age

    private val _heightCm = mutableStateOf(userPreferences.getHeightCm())
    val heightCm: State<Int> = _heightCm

    private val _weightKg = mutableStateOf(userPreferences.getCurrentWeightKg())
    val weightKg: State<Float> = _weightKg

    private val _targetWeightKg = mutableStateOf(userPreferences.getTargetWeightKg())
    val targetWeightKg: State<Float> = _targetWeightKg

    fun updateName(value: String) {
        _name.value = value
        userPreferences.setProfileName(value.trim().ifBlank { "Rép-it User" })
    }

    fun updateAvatarUri(value: String?) {
        _avatarUri.value = value
        userPreferences.setProfileAvatarUri(value)
    }

    fun updateAge(value: Int) {
        _age.value = value
        userPreferences.setAge(value)
    }

    fun updateHeightCm(value: Int) {
        _heightCm.value = value
        userPreferences.setHeightCm(value)
    }

    fun updateWeightKg(value: Float) {
        _weightKg.value = value
        userPreferences.setCurrentWeightKg(value)
        userPreferences.addWeightLog(value)
    }

    fun updateTargetWeightKg(value: Float) {
        _targetWeightKg.value = value
        userPreferences.setTargetWeightKg(value)
    }
}
