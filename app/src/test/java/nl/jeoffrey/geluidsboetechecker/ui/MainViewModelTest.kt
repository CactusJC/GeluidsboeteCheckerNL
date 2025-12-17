package nl.jeoffrey.geluidsboetechecker.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import nl.jeoffrey.geluidsboetechecker.audio.AudioMeter
import nl.jeoffrey.geluidsboetechecker.audio.AudioMeterException
import nl.jeoffrey.geluidsboetechecker.data.VehicleType
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class MainViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: MainViewModel
    private lateinit var audioMeter: AudioMeter

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        audioMeter = mock()
        viewModel = MainViewModel(audioMeter)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onVehicleSelected updates uiState`() = runTest {
        viewModel.onVehicleSelected(VehicleType.MOTORCYCLE)
        assertEquals(VehicleType.MOTORCYCLE, viewModel.uiState.value.currentVehicle)
    }

    @Test
    fun `startMeasurement success updates uiState`() = runTest {
        whenever(audioMeter.getDbLevel()).thenReturn(50.0)

        viewModel.startMeasurement()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(true, viewModel.uiState.value.isMeasuring)
        assertEquals(50.0, viewModel.uiState.value.dbLevel, 0.0)
    }

    @Test
    fun `startMeasurement failure updates uiState with error`() = runTest {
        doThrow(AudioMeterException("Test Exception")).whenever(audioMeter).start()

        viewModel.startMeasurement()

        assertEquals(false, viewModel.uiState.value.isMeasuring)
        assertEquals("Kon de microfoon niet starten. Is deze misschien in gebruik door een andere app?", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `stopMeasurement updates uiState`() = runTest {
        viewModel.startMeasurement()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.stopMeasurement()

        assertEquals(false, viewModel.uiState.value.isMeasuring)
    }
}
