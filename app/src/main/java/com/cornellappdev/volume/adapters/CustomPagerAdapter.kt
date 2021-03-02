package com.cornellappdev.volume.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.cornellappdev.volume.fragments.HomeFragment
import com.cornellappdev.volume.fragments.PublicationsFragment
import com.cornellappdev.volume.fragments.SavedPublicationsFragment

class CustomPagerAdapter(fragmentManager: FragmentManager, private val numOfTabs: Int)
    : FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getCount(): Int {
        return numOfTabs
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> HomeFragment()
            1 -> PublicationsFragment()
            else -> SavedPublicationsFragment()
        }
    }
}
