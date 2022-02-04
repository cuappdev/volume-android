package com.cornellappdev.volume

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cornellappdev.volume.databinding.ActivitySettingsBinding

/**
 * This activity is for the settings page.
 *
 * @see {@link com.cornellappdev.volume.R.layout#activity_settings}
 */
class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.clAboutUs.setOnClickListener {
            val intent = Intent(this, AboutUsActivity::class.java)
            this.startActivity(intent)
        }

        binding.clVisitOurWebsite.setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW, Uri.parse(BuildConfig.WEBSITE))
            startActivity(i)
        }

        binding.clSendFeedback.setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW, Uri.parse(BuildConfig.FEEDBACK_FORM))
            startActivity(i)
        }
    }
}