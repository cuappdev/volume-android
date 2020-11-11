package com.example.volume_android

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.ViewUtils
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabItem
import com.google.android.material.tabs.TabLayout

class TabbedActivity: AppCompatActivity() {

    private lateinit var viewPager: ViewPager
    private lateinit var tabbedLayout: TabLayout
    private lateinit var homeTab: TabItem
    private lateinit var publicationTab: TabItem
    private lateinit var savedPublicationTab: TabItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tabbed_activity)
        viewPager = findViewById(R.id.view_pager_fragments)
        tabbedLayout = findViewById(R.id.tab_layout)

        val fragmentAdapter = PagerAdapter(supportFragmentManager)
        viewPager.adapter = fragmentAdapter
        tabbedLayout.setupWithViewPager(viewPager)

    }
}