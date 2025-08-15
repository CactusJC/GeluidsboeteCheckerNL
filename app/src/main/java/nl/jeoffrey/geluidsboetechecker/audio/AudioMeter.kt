package nl.jeoffrey.geluidsboetechecker.audio

import android.media.MediaRecorder
import android.os.Build
import kotlin.math.log10

class AudioMeter {

    private var mediaRecorder: MediaRecorder? = null

    fun start() {
        if (mediaRecorder != null) return // Already running

        val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder()
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }

        mediaRecorder = recorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(NULL_OUTPUT)
            try {
                prepare()
                start()
            } catch (e: Exception) {
                // Clean up and re-throw as a custom exception
                release()
                this@AudioMeter.mediaRecorder = null
                when (e) {
                    is java.io.IOException, is IllegalStateException -> {
                        throw AudioMeterException("Failed to start MediaRecorder", e)
                    }
                    else -> throw e
                }
            }
        }
    }

    fun stop() {
        mediaRecorder?.let { recorder ->
            try {
                // stop() may throw IllegalStateException or RuntimeException on some devices
                // if the recorder wasn't properly started or was already stopped.
                recorder.stop()
            } catch (e: IllegalStateException) {
                // swallow IllegalStateException to avoid crashes; continue to release.
                // Optional: log using android.util.Log if available.
            } catch (e: RuntimeException) {
                // Some devices may throw other runtime exceptions - swallow to avoid crashes.
            } finally {
                try {
                    recorder.release()
                } catch (ignored: Exception) {
                    // Ignore release failures - resource cleanup best-effort
                }
            }
        }
        mediaRecorder = null
    }

    fun getDbLevel(): Double {
        val amplitude = mediaRecorder?.maxAmplitude ?: 0
        return if (amplitude > 0) {
            // Formula to convert amplitude to dB.
            // MAX_AMPLITUDE is the maximum value for a 16-bit signed integer.
            // The dB level is normalized and scaled to a more human-readable range.
            REFERENCE_DB * log10(amplitude.toDouble() / MAX_AMPLITUDE) + DB_OFFSET
        } else {
            0.0
        }
    }

    companion object {
        private const val NULL_OUTPUT = "/dev/null"
        private const val REFERENCE_DB = 20.0
        private const val MAX_AMPLITUDE = 32767.0
        private const val DB_OFFSET = 90.0
    }
}
