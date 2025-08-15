package nl.jeoffrey.geluidsboetechecker.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import nl.jeoffrey.geluidsboetechecker.R

class MainActivity : AppCompatActivity() {

    private lateinit var dbValue: TextView
    private lateinit var colorIndicator: View
    private lateinit var vehicleSpinner: Spinner
    private lateinit var infoButton: Button

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbValue = findViewById(R.id.dbValue)
        colorIndicator = findViewById(R.id.colorIndicator)
        vehicleSpinner = findViewById(R.id.vehicleSpinner)
        infoButton = findViewById(R.id.infoButton)

        setupSpinner()
        infoButton.setOnClickListener {
            startActivity(Intent(this, InfoActivity::class.java))
        }

        collectUiState()
        requestAudioPermission()
    }

    private fun setupSpinner() {
        // This assumes vehicle_types is a string-array resource.
        // The adapter is already created by the XML `android:entries` attribute.
        vehicleSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                parent?.getItemAtPosition(position)?.toString()?.let {
                    viewModel.onVehicleSelected(it)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun collectUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    // Update DB text
                    dbValue.text = String.format("%.1f dB", state.dbLevel)

                    // Update color indicator
                    colorIndicator.setBackgroundColor(ContextCompat.getColor(this@MainActivity, state.indicatorColor))

                    // Update spinner selection
                    val adapter = vehicleSpinner.adapter as? ArrayAdapter<String>
                    if (adapter != null) {
                        val position = adapter.getPosition(state.currentVehicle)
                        if (position != -1 && vehicleSpinner.selectedItemPosition != position) {
                            vehicleSpinner.setSelection(position)
                        }
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
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                viewModel.startMeasurement()
            } else {
                Toast.makeText(this, "De microfoonpermissie is nodig om geluid te meten.", Toast.LENGTH_LONG).show()
            }
        }
    }
}
