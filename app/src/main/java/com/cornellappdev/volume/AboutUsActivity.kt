package com.cornellappdev.volume

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cornellappdev.volume.databinding.ActivityAboutUsBinding

/**
 * This article is used for displaying some about us information of the Volume app.
 *
 * @see {@link com.cornellappdev.volume.R.layout#activity_about_us}
 */
class AboutUsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutUsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutUsBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
