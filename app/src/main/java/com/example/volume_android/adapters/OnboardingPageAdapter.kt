package com.example.volume_android.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.volume_android.fragments.OnboardingFrag1
import com.example.volume_android.fragments.OnboardingFrag2

private const val COUNT = 3

class OnboardingPageAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

    override fun getCount(): Int {
        return COUNT
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> OnboardingFrag1()
            else -> OnboardingFrag2()
        }
    }


}