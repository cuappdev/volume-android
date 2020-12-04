package com.example.volume_android

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.example.volume_android.adapters.PagerAdapter
import com.google.android.material.tabs.TabItem
import com.google.android.material.tabs.TabLayout

class TabbedActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager
    private lateinit var tabbedLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tabbed_activity)
        viewPager = findViewById(R.id.view_pager_fragments)
        tabbedLayout = findViewById(R.id.tab_layout)

        val fragmentAdapter = PagerAdapter(supportFragmentManager)
        viewPager.adapter = fragmentAdapter
        tabbedLayout.setupWithViewPager(viewPager)

        for (i in 0 until tabbedLayout.tabCount) {
            Log.d("Int", i.toString())
            if (i == 0){
                tabbedLayout.getTabAt(i)?.setIcon(com.example.volume_android.R.drawable.ic_volumesvg)
            }

            if (i == 1){
                tabbedLayout.getTabAt(i)?.setIcon(com.example.volume_android.R.drawable.ic_book_svg)
            }
            if(i==2) {
                tabbedLayout.getTabAt(i)?.setIcon(com.example.volume_android.R.drawable.ic_bookmarksss)
            }


        }
    }
}