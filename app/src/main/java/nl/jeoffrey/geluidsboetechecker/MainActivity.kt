package YOUR.PACKAGE.NAME   // <-- zet hier jouw package

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlin.math.log10

class MainActivity : AppCompatActivity() {

    private lateinit var dbValue: TextView
    private lateinit var colorIndicator: android.view.View
    private lateinit var vehicleSpinner: Spinner
    private lateinit var infoButton: Button
    private var mediaRecorder: MediaRecorder? = null
    private val handler = Handler(Looper.getMainLooper())
    private var currentVehicle = "Auto"

    private val updateTask = object : Runnable {
        override fun run() {
            val amplitude = mediaRecorder?.maxAmplitude ?: 0
            val dB = if (amplitude > 0) 20 * log10(amplitude.toDouble() / 32767.0) + 90 else 0.0
            dbValue.text = String.format("%.1f dB", dB)

            val (greenLimit, orangeLimit) = getLimitsForVehicle(currentVehicle)
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
            handler.postDelayed(this, 500)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbValue = findViewById(R.id.dbValue)
        colorIndicator = findViewById(R.id.colorIndicator)
        vehicleSpinner = findViewById(R.id.vehicleSpinner)
        infoButton = findViewById(R.id.infoButton)

        vehicleSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                currentVehicle = parent?.getItemAtPosition(position).toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        infoButton.setOnClickListener {
            startActivity(Intent(this, InfoActivity::class.java))
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
        } else {
            startRecording()
        }
    }

    private fun startRecording() {
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile("/dev/null")
            prepare()
            start()
        }
        handler.post(updateTask)
    }

    private fun getLimitsForVehicle(vehicle: String): Pair<Double, Double> {
        return when (vehicle) {
            "Auto" -> 70.0 to 90.0
            "Motor" -> 75.0 to 95.0
            "Brommer" -> 72.0 to 92.0
            else -> 70.0 to 90.0
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateTask)
        mediaRecorder?.release()
        mediaRecorder = null
    }
}
