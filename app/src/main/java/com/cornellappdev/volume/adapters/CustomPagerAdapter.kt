package com.cornellappdev.volume.adapters

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.cornellappdev.volume.fragments.HomeFragment
import com.cornellappdev.volume.fragments.PublicationsFragment
import com.cornellappdev.volume.fragments.SavedArticlesFragment

/**
 * Used by the ViewPager in TabActivity. Holds all three of our pages.
 */
class CustomPagerAdapter(fa: FragmentActivity, private val numOfTabs: Int, private val bundle: Bundle?) : FragmentStateAdapter(fa) {

    private var firstTime = true

    override fun getItemCount(): Int {
        return numOfTabs
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                val homeFragment = HomeFragment()
                if (firstTime) {
                    homeFragment.arguments = bundle
                    firstTime = false
                }
                homeFragment
            }
            1 -> PublicationsFragment()
            else -> SavedArticlesFragment()
        }
    }
}
