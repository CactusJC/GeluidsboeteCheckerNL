package nl.jeoffrey.geluidsboetechecker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import nl.jeoffrey.geluidsboetechecker.R
import nl.jeoffrey.geluidsboetechecker.audio.AudioMeter
import nl.jeoffrey.geluidsboetechecker.audio.AudioMeterException
import nl.jeoffrey.geluidsboetechecker.data.VehicleLimits
import nl.jeoffrey.geluidsboetechecker.data.VehicleType

data class UiState(
    val dbLevel: Double = 0.0,
    val indicatorColor: Int = R.color.indicator_green,
    val currentVehicle: VehicleType = VehicleType.CAR,
    val isMeasuring: Boolean = false,
    val errorMessage: String? = null
)

class MainViewModel : ViewModel() {

    private val audioMeter = AudioMeter()
    private var measurementJob: Job? = null

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    fun onVehicleSelected(vehicleType: VehicleType) {
        _uiState.value = _uiState.value.copy(currentVehicle = vehicleType)
        updateColorIndicator()
    }

    fun startMeasurement() {
        if (measurementJob?.isActive == true) return

        try {
            audioMeter.start()
            _uiState.value = _uiState.value.copy(isMeasuring = true, errorMessage = null)
            measurementJob = viewModelScope.launch {
                while (isActive) {
                    val dB = audioMeter.getDbLevel()
                    _uiState.value = _uiState.value.copy(dbLevel = dB)
                    updateColorIndicator()
                    delay(MEASUREMENT_INTERVAL_MS)
                }
            }
        } catch (e: AudioMeterException) {
            _uiState.value = _uiState.value.copy(
                isMeasuring = false,
                errorMessage = "Kon de microfoon niet starten. Is deze misschien in gebruik door een andere app?"
            )
        }
    }

    fun stopMeasurement() {
        measurementJob?.cancel()
        audioMeter.stop()
        _uiState.value = _uiState.value.copy(isMeasuring = false)
    }

    private fun updateColorIndicator() {
        val currentState = _uiState.value
        val (greenLimit, orangeLimit) = VehicleLimits.getLimitsFor(currentState.currentVehicle)
        val newColor = when {
            currentState.dbLevel < greenLimit -> R.color.indicator_green
            currentState.dbLevel < orangeLimit -> R.color.indicator_orange
            else -> R.color.indicator_red
        }
        _uiState.value = _uiState.value.copy(indicatorColor = newColor)
    }

    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    override fun onCleared() {
        super.onCleared()
        stopMeasurement()
    }

    companion object {
        private const val MEASUREMENT_INTERVAL_MS = 500L
    }
}
