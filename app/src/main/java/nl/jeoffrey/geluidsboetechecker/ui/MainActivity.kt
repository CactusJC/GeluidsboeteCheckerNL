package nl.jeoffrey.geluidsboetechecker.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import nl.jeoffrey.geluidsboetechecker.data.VehicleType
import nl.jeoffrey.geluidsboetechecker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSpinner()
        binding.infoButton.setOnClickListener {
            startActivity(Intent(this, InfoActivity::class.java))
        }

        collectUiState()
        requestAudioPermission()
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            VehicleType.values().map { it.displayName }
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.vehicleSpinner.adapter = adapter

        binding.vehicleSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedVehicle = VehicleType.values()[position]
                viewModel.onVehicleSelected(selectedVehicle)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun collectUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    // Update DB text
                    binding.dbValue.text = String.format("%.1f dB", state.dbLevel)

                    // Update color indicator
                    binding.colorIndicator.setBackgroundColor(ContextCompat.getColor(this@MainActivity, state.indicatorColor))

                    // Update spinner selection
                    val position = state.currentVehicle.ordinal
                    if (binding.vehicleSpinner.selectedItemPosition != position) {
                        binding.vehicleSpinner.setSelection(position)
                    }

                    // Show error toast
                    state.errorMessage?.let {
                        Toast.makeText(this@MainActivity, it, Toast.LENGTH_SHORT).show()
                        viewModel.clearErrorMessage()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (hasAudioPermission()) {
            viewModel.startMeasurement()
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopMeasurement()
    }

    private fun hasAudioPermission() = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

    private fun requestAudioPermission() {
        if (!hasAudioPermission()) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), AUDIO_PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == AUDIO_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                viewModel.startMeasurement()
            } else {
                Toast.makeText(this, "De microfoonpermissie is nodig om geluid te meten.", Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        private const val AUDIO_PERMISSION_REQUEST_CODE = 1
    }
}
