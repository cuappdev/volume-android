package com.example.volume_android

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.example.volume_android.adapters.CustomPagerAdapter
import com.example.volume_android.models.Article
import com.example.volume_android.models.Publication
import com.google.android.material.tabs.TabLayout

class TabbedActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager
    private lateinit var tabbedLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tabbed_activity)
        viewPager = findViewById(R.id.view_pager_fragments)
        tabbedLayout = findViewById(R.id.tab_layout)
        viewPager.adapter = CustomPagerAdapter(supportFragmentManager)
        tabbedLayout.setupWithViewPager(viewPager)

        for (i in 0 until tabbedLayout.tabCount) {
            if (i == 0){
                tabbedLayout.getTabAt(i)?.setIcon(R.drawable.ic_volumesvg_orange)
            }
            if (i == 1){
                tabbedLayout.getTabAt(i)?.setIcon(R.drawable.ic_book_gray)
            }
            if(i==2) {
                tabbedLayout.getTabAt(i)?.setIcon(R.drawable.ic_bookmark_gray)
            }


        }

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        tabbedLayout.getTabAt(0)?.setIcon(R.drawable.ic_volumesvg_orange)
                        tabbedLayout.getTabAt(1)?.setIcon(R.drawable.ic_book_gray)
                        tabbedLayout.getTabAt(2)?.setIcon(R.drawable.ic_bookmark_gray)
                    }
                    1 -> {
                        tabbedLayout.getTabAt(0)?.setIcon(R.drawable.ic_volumesvg_gray)
                        tabbedLayout.getTabAt(1)?.setIcon(R.drawable.ic_book_orange)
                        tabbedLayout.getTabAt(2)?.setIcon(R.drawable.ic_bookmark_gray)
                    }
                    2 ->{
                        tabbedLayout.getTabAt(0)?.setIcon(R.drawable.ic_volumesvg_gray)
                        tabbedLayout.getTabAt(1)?.setIcon(R.drawable.ic_book_gray)
                        tabbedLayout.getTabAt(2)?.setIcon(R.drawable.ic_bookmark_orange)}
                }
            }

        })
    }
}