package com.example.volume_android.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.volume_android.fragments.HomeFragment
import com.example.volume_android.fragments.PublicationsFragment
import com.example.volume_android.fragments.SavedPublicationsFragment
import com.example.volume_android.models.Article
import com.example.volume_android.models.Publication

private const val COUNT = 3

class PagerAdapter(fragmentManager: FragmentManager, val publications: List<Publication>, val articles: List<Article>) : FragmentPagerAdapter(fragmentManager) {

    override fun getCount(): Int {
        return COUNT
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> HomeFragment(articles)
            1 -> PublicationsFragment(publications)
            else -> SavedPublicationsFragment(articles)
        }
    }


}