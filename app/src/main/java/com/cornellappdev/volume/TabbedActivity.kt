package com.cornellappdev.volume

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.cornellappdev.volume.adapters.CustomPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class TabbedActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    companion object {
        private const val VIEWPAGER_SENSITIVITY = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tabbed_activity)
        viewPager = findViewById(R.id.view_pager_fragments)
        viewPager.reduceDragSensitivity()
        viewPager.isUserInputEnabled = true
        tabLayout = findViewById(R.id.tab_layout)
        viewPager.adapter = CustomPagerAdapter(this, tabLayout.tabCount)
        TabLayoutMediator(tabLayout, viewPager) { _, _ ->
        }.attach()
        tabLayout.getTabAt(0)?.setIcon(R.drawable.ic_volumesvg_orange)
        tabLayout.getTabAt(1)?.setIcon(R.drawable.ic_book_gray)
        tabLayout.getTabAt(2)?.setIcon(R.drawable.ic_bookmark_gray)
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

    private fun ViewPager2.reduceDragSensitivity() {
        val recyclerViewField = ViewPager2::class.java.getDeclaredField("mRecyclerView")
        recyclerViewField.isAccessible = true
        val recyclerView = recyclerViewField.get(this) as RecyclerView
        val touchSlopField = RecyclerView::class.java.getDeclaredField("mTouchSlop")
        touchSlopField.isAccessible = true
        val touchSlop = touchSlopField.get(recyclerView) as Int
        touchSlopField.set(recyclerView, touchSlop * VIEWPAGER_SENSITIVITY)  // multiplier effects sensitivity of scroll
    }
}