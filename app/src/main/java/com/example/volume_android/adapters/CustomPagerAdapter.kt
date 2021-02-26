package com.example.volume_android.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.volume_android.fragments.HomeFragment
import com.example.volume_android.fragments.PublicationsFragment
import com.example.volume_android.fragments.SavedPublicationsFragment

class CustomPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    companion object {
        private const val COUNT = 3
    }

    override fun getCount(): Int {
        return COUNT
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> HomeFragment()
            1 -> PublicationsFragment()
            else -> SavedPublicationsFragment()
        }
    }
}