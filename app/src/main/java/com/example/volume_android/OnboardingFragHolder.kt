package com.example.volume_android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.example.volume_android.adapters.OnboardingPageAdapter
import com.example.volume_android.adapters.PagerAdapter
import com.google.android.material.tabs.TabItem
import com.google.android.material.tabs.TabLayout

class OnboardingFragHolder : AppCompatActivity() {

    private lateinit var viewPager: ViewPager
    private lateinit var tabbedLayout: TabLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.onboarding_holder)
        viewPager = findViewById(R.id.onboarding_pageviewer)

        val fragmentAdapter = OnboardingPageAdapter(supportFragmentManager)
        viewPager.adapter = fragmentAdapter

    }
}