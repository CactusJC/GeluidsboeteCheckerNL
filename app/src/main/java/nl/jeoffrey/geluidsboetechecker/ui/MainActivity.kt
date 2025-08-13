package nl.jeoffrey.geluidsboetechecker.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import nl.jeoffrey.geluidsboetechecker.R
import nl.jeoffrey.geluidsboetechecker.audio.AudioMeter
import nl.jeoffrey.geluidsboetechecker.data.VehicleLimits

class MainActivity : AppCompatActivity() {

    private lateinit var dbValue: TextView
    private lateinit var colorIndicator: View
    private lateinit var vehicleSpinner: Spinner
    private lateinit var infoButton: Button
    private lateinit var audioMeter: AudioMeter

    private var updateJob: Job? = null
    private var currentVehicle = "Auto"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        audioMeter = AudioMeter(this)

        dbValue = findViewById(R.id.dbValue)
        colorIndicator = findViewById(R.id.colorIndicator)
        vehicleSpinner = findViewById(R.id.vehicleSpinner)
        infoButton = findViewById(R.id.infoButton)

        vehicleSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentVehicle = parent?.getItemAtPosition(position).toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        infoButton.setOnClickListener {
            startActivity(Intent(this, InfoActivity::class.java))
        }

        requestAudioPermission()
    }

    private fun requestAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
        } else {
            startAudioMeasurement()
        }
    }

    private fun startAudioMeasurement() {
        if (audioMeter.start()) {
            updateJob = lifecycleScope.launch {
                while (isActive) {
                    val dB = audioMeter.getDbLevel()
                    dbValue.text = String.format("%.1f dB", dB)

                    val (greenLimit, orangeLimit) = VehicleLimits.getLimitsFor(currentVehicle)
                    when {
                        dB < greenLimit -> colorIndicator.setBackgroundColor(
                            ContextCompat.getColor(this@MainActivity, android.R.color.holo_green_light)
                        )
                        dB < orangeLimit -> colorIndicator.setBackgroundColor(
                            ContextCompat.getColor(this@MainActivity, android.R.color.holo_orange_light)
                        )
                        else -> colorIndicator.setBackgroundColor(
                            ContextCompat.getColor(this@MainActivity, android.R.color.holo_red_light)
                        )
                    }
                    delay(500)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        updateJob?.cancel()
        audioMeter.stop()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                startAudioMeasurement()
            } else {
                Toast.makeText(this, "De microfoonpermissie is nodig om geluid te meten.", Toast.LENGTH_LONG).show()
            }
            return
        }
    }
}
