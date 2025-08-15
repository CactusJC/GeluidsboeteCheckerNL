package nl.jeoffrey.geluidsboetechecker.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import nl.jeoffrey.geluidsboetechecker.R
import nl.jeoffrey.geluidsboetechecker.databinding.ActivityInfoBinding

class InfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.infoText.text = getString(R.string.info_text_content)
    }
}
