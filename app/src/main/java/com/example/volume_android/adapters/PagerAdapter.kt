package com.example.volume_android.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.volume_android.fragments.HomeFragment
import com.example.volume_android.fragments.PublicationsFragment
import com.example.volume_android.fragments.SavedPublicationsFragment

private const val COUNT = 3

class PagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

    override fun getCount(): Int {
        return COUNT
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> HomeFragment()
            1 -> PublicationsFragment()
            2 -> SavedPublicationsFragment()
            else -> return HomeFragment()
        }
    }


}