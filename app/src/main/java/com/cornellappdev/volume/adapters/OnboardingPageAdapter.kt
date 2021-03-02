package com.cornellappdev.volume.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.cornellappdev.volume.fragments.OnboardingFrag1
import com.cornellappdev.volume.fragments.OnboardingFrag2
import com.cornellappdev.volume.models.Publication

private const val COUNT = 3

class OnboardingPageAdapter(fragmentManager: FragmentManager, val publications : List<Publication>) : FragmentPagerAdapter(fragmentManager) {

    override fun getCount(): Int {
        return COUNT
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> OnboardingFrag1()
            else -> OnboardingFrag2(publications = publications)
        }
    }


}