package com.cornellappdev.volume.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.cornellappdev.volume.fragments.WeeklyDebriefIntroFragment
import com.cornellappdev.volume.fragments.WeeklyDebriefSeeYouNextFragment
import com.cornellappdev.volume.fragments.WeeklyDebriefShareFragment
import com.cornellappdev.volume.models.WeeklyDebrief

class WeeklyDebriefPagerAdapter(
    fa: FragmentActivity,
    private val numOfFragments: Int,
    private val weeklyDebrief: WeeklyDebrief,
    callback: () -> Unit
) :
    FragmentStateAdapter(fa) {

    val fragments: HashMap<Int, () -> Fragment> = hashMapOf()

    init {
        var positionCounter = 0
        fragments[positionCounter] = { WeeklyDebriefIntroFragment(weeklyDebrief) }
        positionCounter++
        for (article in weeklyDebrief.readArticles) {
            fragments[positionCounter] = { WeeklyDebriefShareFragment(article) }
            positionCounter++
        }
        for (article in weeklyDebrief.randomArticles) {
            fragments[positionCounter] = { WeeklyDebriefShareFragment(article) }
            positionCounter++
        }
        fragments[positionCounter] = { WeeklyDebriefSeeYouNextFragment(callback) }
    }

    override fun getItemCount(): Int {
        return numOfFragments
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]!!.invoke()
    }
}