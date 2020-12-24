package com.example.volume_android

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.example.volume_android.adapters.PagerAdapter
import com.example.volume_android.models.Article
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
        onboardingdata.add(Publication("We are Creme de Cornell", "", "url", "Creme de Cornell", "rssName", "rssUrl", 10.0, "website"))
        onboardingdata.add(Publication("We are Creme de Cornell", "2", "url", "Creme de Cornell", "rssName", "rssUrl", 10.0, "website"))
        onboardingdata.add(Publication("We are Creme de Cornell", "3", "url", "Creme de Cornell", "rssName", "rssUrl", 10.0, "website"))
        onboardingdata.add(Publication("We are Creme de Cornell", "4", "url", "Creme de Cornell", "rssName", "rssUrl", 10.0, "website"))
        onboardingdata.add(Publication("We are Creme de Cornell", "5", "url", "Creme de Cornell", "rssName", "rssUrl", 10.0, "website"))
        onboardingdata.add(Publication("We are Creme de Cornell", "6", "url", "Creme de Cornell", "rssName", "rssUrl", 10.0, "website"))
        onboardingdata.add(Publication("We are Creme de Cornell", "7", "url", "Creme de Cornell", "rssName", "rssUrl", 10.0, "website"))
        onboardingdata.add(Publication("We are Creme de Cornell", "8", "url", "Creme de Cornell", "rssName", "rssUrl", 10.0, "website"))
        onboardingdata.add(Publication("We are Creme de Cornell", "9", "url", "Creme de Cornell", "rssName", "rssUrl", 10.0, "website"))

        val articledata: ArrayList<Article> = ArrayList()
        articledata.add(Article("https://www.cremedecornell.net/blog/2020/7/26/sangkhaya-thai-pandan-custard-dip/", "fakedate", "1", "url", "1A", 10.0, "Sangkhaya: Thai Pandan Custard Dip"))
        articledata.add(Article("https://www.cremedecornell.net/blog/2020/7/26/sangkhaya-thai-pandan-custard-dip/", "fakedate", "2", "url", "1A", 10.0, "Sangkhaya: Thai Pandan Custard Dip"))
        articledata.add(Article("https://www.cremedecornell.net/blog/2020/7/26/sangkhaya-thai-pandan-custard-dip/", "fakedate", "3", "url", "1A", 10.0, "Sangkhaya: Thai Pandan Custard Dip"))
        articledata.add(Article("https://www.cremedecornell.net/blog/2020/7/26/sangkhaya-thai-pandan-custard-dip/", "fakedate", "4", "url", "1A", 10.0, "Sangkhaya: Thai Pandan Custard Dip"))
        articledata.add(Article("https://www.cremedecornell.net/blog/2020/7/26/sangkhaya-thai-pandan-custard-dip/", "fakedate", "5", "url", "1A", 10.0, "Sangkhaya: Thai Pandan Custard Dip"))




        val fragmentAdapter = PagerAdapter(supportFragmentManager, onboardingdata, articledata)
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