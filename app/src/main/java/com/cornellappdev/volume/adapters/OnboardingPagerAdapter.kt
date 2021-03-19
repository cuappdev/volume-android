package com.cornellappdev.volume.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.cornellappdev.volume.fragments.OnboardingFragOne
import com.cornellappdev.volume.fragments.OnboardingFragTwo

class OnboardingPagerAdapter(fa: FragmentActivity, private val numOfFragments: Int) :
        FragmentStateAdapter(fa) {

    override fun getItemCount(): Int {
        return numOfFragments
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> OnboardingFragOne()
            else -> OnboardingFragTwo()
        }
    }
}