package com.cornellappdev.volume

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.cornellappdev.volume.adapters.CustomPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class TabbedActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tabbed_activity)
        viewPager = findViewById(R.id.view_pager_fragments)
        tabLayout = findViewById(R.id.tab_layout)
        viewPager.adapter = CustomPagerAdapter(this, tabLayout.tabCount)
        TabLayoutMediator(tabLayout, viewPager) { _, _ ->
        }.attach()
        tabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        tabLayout.getTabAt(0)?.setIcon(R.drawable.ic_volumesvg_orange)
                        tabLayout.getTabAt(1)?.setIcon(R.drawable.ic_book_gray)
                        tabLayout.getTabAt(2)?.setIcon(R.drawable.ic_bookmark_gray)
                    }
                    1 -> {
                        tabLayout.getTabAt(0)?.setIcon(R.drawable.ic_volumesvg_gray)
                        tabLayout.getTabAt(1)?.setIcon(R.drawable.ic_book_orange)
                        tabLayout.getTabAt(2)?.setIcon(R.drawable.ic_bookmark_gray)
                    }
                    2 -> {
                        tabLayout.getTabAt(0)?.setIcon(R.drawable.ic_volumesvg_gray)
                        tabLayout.getTabAt(1)?.setIcon(R.drawable.ic_book_gray)
                        tabLayout.getTabAt(2)?.setIcon(R.drawable.ic_bookmark_orange)
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

        })
    }

    override fun onResume() {
        super.onResume()
        if (viewPager != null) {
            viewPager.adapter?.notifyDataSetChanged()
        }
    }
}