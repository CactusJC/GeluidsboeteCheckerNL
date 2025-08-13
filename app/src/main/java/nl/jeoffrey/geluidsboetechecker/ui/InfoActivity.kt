package nl.jeoffrey.geluidsboetechecker.ui

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class InfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)

        val infoText: TextView = findViewById(R.id.infoText)
        infoText.setText(R.string.info_text_content)
    }
}
