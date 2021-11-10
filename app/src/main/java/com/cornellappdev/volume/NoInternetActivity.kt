package com.cornellappdev.volume

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.cornellappdev.volume.databinding.ActivityMainBinding
import com.cornellappdev.volume.databinding.ActivityNoInternetBinding

class NoInternetActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNoInternetBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoInternetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.clBack.setOnClickListener {
            finish()
        }
    }
}