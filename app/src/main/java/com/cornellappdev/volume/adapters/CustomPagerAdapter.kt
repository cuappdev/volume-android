package com.cornellappdev.volume.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.cornellappdev.volume.fragments.HomeFragment
import com.cornellappdev.volume.fragments.PublicationsFragment
import com.cornellappdev.volume.fragments.SavedArticlesFragment

/**
 * Used by the ViewPager in TabActivity. Holds all three of our pages.
 */
class CustomPagerAdapter(fa: FragmentActivity, private val numOfTabs: Int) : FragmentStateAdapter(fa) {

    override fun getItemCount(): Int {
        return numOfTabs
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HomeFragment()
            1 -> PublicationsFragment()
            else -> SavedArticlesFragment()
        }
    }
}
