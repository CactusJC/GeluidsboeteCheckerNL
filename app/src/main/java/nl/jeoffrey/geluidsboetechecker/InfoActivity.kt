package nl.jeoffrey.geluidsboetechecker

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class InfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)

        val infoText: TextView = findViewById(R.id.infoText)
        infoText.text = """
            Geluidsboetes in Nederland (voorbeeld):
            
            Auto: max 70 dB (stilstaan), 90 dB (rijdend)
            Motor: max 75 dB (stilstaan), 95 dB (rijdend)
            Brommer: max 72 dB (stilstaan), 92 dB (rijdend)
            
            Let op: dit zijn voorbeeldwaarden. Check altijd actuele regels.
        """.trimIndent()
    }
}
