package nl.jeoffrey.geluidsboetechecker.audio

import android.content.Context
import android.media.MediaRecorder
import android.widget.Toast
import kotlin.math.log10

class AudioMeter(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null

    fun start(): Boolean {
        if (mediaRecorder != null) return true // Already running

        mediaRecorder = MediaRecorder(context).apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile("/dev/null")
            try {
                prepare()
                start()
                return true
            } catch (e: java.io.IOException) {
                Toast.makeText(context, "Kon microfoon niet starten. Is deze in gebruik?", Toast.LENGTH_LONG).show()
                e.printStackTrace()
                // Clean up
                release()
                this@AudioMeter.mediaRecorder = null
                return false
            } catch (e: IllegalStateException) {
                Toast.makeText(context, "Kon microfoon niet starten. Herstart de app.", Toast.LENGTH_LONG).show()
                e.printStackTrace()
                // Clean up
                release()
                this@AudioMeter.mediaRecorder = null
                return false
            }
        }
        return false // Should not be reached
    }

    fun stop() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
    }

    fun getDbLevel(): Double {
        val amplitude = mediaRecorder?.maxAmplitude ?: 0
        return if (amplitude > 0) {
            // Formula to convert amplitude to dB. The constants can be calibrated.
            20 * log10(amplitude.toDouble() / 32767.0) + 90
        } else {
            0.0
        }
    }
}
