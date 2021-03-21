package com.cornellappdev.volume

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.cornellappdev.volume.adapters.CustomPagerAdapter
import com.cornellappdev.volume.databinding.ActivityTabBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class TabActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTabBinding

    companion object {
        private const val VIEWPAGER_SENSITIVITY = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTabBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.vpFragments.reduceDragSensitivity()
        binding.vpFragments.isUserInputEnabled = true
        binding.vpFragments.adapter = CustomPagerAdapter(this, binding.tlTabs.tabCount)
        TabLayoutMediator(binding.tlTabs, binding.vpFragments) { _, _ ->
        }.attach()
        binding.tlTabs.getTabAt(0)?.setIcon(R.drawable.ic_volumesvg_orange)
        binding.tlTabs.getTabAt(1)?.setIcon(R.drawable.ic_book_gray)
        binding.tlTabs.getTabAt(2)?.setIcon(R.drawable.ic_bookmark_gray)
        binding.tlTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        binding.tlTabs.getTabAt(0)?.setIcon(R.drawable.ic_volumesvg_orange)
                        binding.tlTabs.getTabAt(1)?.setIcon(R.drawable.ic_book_gray)
                        binding.tlTabs.getTabAt(2)?.setIcon(R.drawable.ic_bookmark_gray)
                    }
                    1 -> {
                        binding.tlTabs.getTabAt(0)?.setIcon(R.drawable.ic_volumesvg_gray)
                        binding.tlTabs.getTabAt(1)?.setIcon(R.drawable.ic_book_orange)
                        binding.tlTabs.getTabAt(2)?.setIcon(R.drawable.ic_bookmark_gray)
                    }
                    2 -> {
                        binding.tlTabs.getTabAt(0)?.setIcon(R.drawable.ic_volumesvg_gray)
                        binding.tlTabs.getTabAt(1)?.setIcon(R.drawable.ic_book_gray)
                        binding.tlTabs.getTabAt(2)?.setIcon(R.drawable.ic_bookmark_orange)
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