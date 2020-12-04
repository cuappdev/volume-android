package com.example.volume_android

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.example.volume_android.adapters.PagerAdapter
import com.example.volume_android.models.Publication
import com.google.android.material.tabs.TabItem
import com.google.android.material.tabs.TabLayout

class TabbedActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager
    private lateinit var tabbedLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tabbed_activity)
        viewPager = findViewById(R.id.view_pager_fragments)
        tabbedLayout = findViewById(R.id.tab_layout)

        //TODO: Fake data
        val onboardingdata : ArrayList<Publication>  = ArrayList()
        onboardingdata.add(Publication("We are Creme de Cornell", 1, "url", "Creme de Cornell", "rssName", "rssUrl", 10.0f, "website"))
        onboardingdata.add(Publication("We are Creme de Cornell", 2, "url", "Creme de Cornell", "rssName", "rssUrl", 10.0f, "website"))
        onboardingdata.add(Publication("We are Creme de Cornell", 3, "url", "Creme de Cornell", "rssName", "rssUrl", 10.0f, "website"))
        onboardingdata.add(Publication("We are Creme de Cornell", 4, "url", "Creme de Cornell", "rssName", "rssUrl", 10.0f, "website"))
        onboardingdata.add(Publication("We are Creme de Cornell", 5, "url", "Creme de Cornell", "rssName", "rssUrl", 10.0f, "website"))
        onboardingdata.add(Publication("We are Creme de Cornell", 6, "url", "Creme de Cornell", "rssName", "rssUrl", 10.0f, "website"))
        onboardingdata.add(Publication("We are Creme de Cornell", 7, "url", "Creme de Cornell", "rssName", "rssUrl", 10.0f, "website"))
        onboardingdata.add(Publication("We are Creme de Cornell", 8, "url", "Creme de Cornell", "rssName", "rssUrl", 10.0f, "website"))
        onboardingdata.add(Publication("We are Creme de Cornell", 9, "url", "Creme de Cornell", "rssName", "rssUrl", 10.0f, "website"))



        val fragmentAdapter = PagerAdapter(supportFragmentManager, onboardingdata)
        viewPager.adapter = fragmentAdapter
        tabbedLayout.setupWithViewPager(viewPager)

        for (i in 0 until tabbedLayout.tabCount) {
            Log.d("Int", i.toString())
            if (i == 0){
                tabbedLayout.getTabAt(i)?.setIcon(com.example.volume_android.R.drawable.ic_volumesvg)
            }

            if (i == 1){
                tabbedLayout.getTabAt(i)?.setIcon(com.example.volume_android.R.drawable.ic_book_svg)
            }
            if(i==2) {
                tabbedLayout.getTabAt(i)?.setIcon(com.example.volume_android.R.drawable.ic_bookmarksss)
            }


        }
    }
}