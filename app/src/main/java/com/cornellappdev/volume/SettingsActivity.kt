package com.cornellappdev.volume

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cornellappdev.volume.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val website = "https://www.cornellappdev.com/"
    private val feedbackForm = "https://forms.gle/ZLqCFZ259EPky4Zm7"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.clAboutUs.setOnClickListener {
            val intent = Intent(this, AboutUsActivity::class.java)
            this.startActivity(intent)
        }

        binding.clVisitOurWebsite.setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW, Uri.parse(website))
            startActivity(i)
        }

        binding.clSendFeedback.setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW, Uri.parse(feedbackForm))
            startActivity(i)
        }
    }
}