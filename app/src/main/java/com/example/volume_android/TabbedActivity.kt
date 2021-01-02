package com.example.volume_android

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.example.volume_android.adapters.PagerAdapter
import com.example.volume_android.models.Article
import com.example.volume_android.models.Publication
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
        onboardingdata.add(Publication("We are Creme de Cornell", "1", "bio", "Creme de Cornell", "image", "rssName", "rssURL", "website"))
        onboardingdata.add(Publication("We are Creme de Cornell", "1", "bio", "Creme de Cornell", "image", "rssName", "rssURL", "website"))
        onboardingdata.add(Publication("We are Creme de Cornell", "1", "bio", "Creme de Cornell", "image", "rssName", "rssURL", "website"))
        onboardingdata.add(Publication("We are Creme de Cornell", "1", "bio", "Creme de Cornell", "image", "rssName", "rssURL", "website"))
        onboardingdata.add(Publication("We are Creme de Cornell", "1", "bio", "Creme de Cornell", "image", "rssName", "rssURL", "website"))
        onboardingdata.add(Publication("We are Creme de Cornell", "1", "bio", "Creme de Cornell", "image", "rssName", "rssURL", "website"))
        onboardingdata.add(Publication("We are Creme de Cornell", "1", "bio", "Creme de Cornell", "image", "rssName", "rssURL", "website"))
        onboardingdata.add(Publication("We are Creme de Cornell", "1", "bio", "Creme de Cornell", "image", "rssName", "rssURL", "website"))

        val articledata: ArrayList<Article> = ArrayList()
//        articledata.add(Article("", "Sangkhaya: Thai Pandan Custard Dip", "https://www.cremedecornell.net/blog/2020/7/26/sangkhaya-thai-pandan-custard-dip/", "imageURL", "pubID", "pubName", "date", 10.0))
//        articledata.add(Article("", "Sangkhaya: Thai Pandan Custard Dip", "https://www.cremedecornell.net/blog/2020/7/26/sangkhaya-thai-pandan-custard-dip/", "imageURL", "pubID", "pubName", "date", 10.0))
//        articledata.add(Article("", "Sangkhaya: Thai Pandan Custard Dip", "https://www.cremedecornell.net/blog/2020/7/26/sangkhaya-thai-pandan-custard-dip/", "imageURL", "pubID", "pubName", "date", 10.0))
//        articledata.add(Article("", "Sangkhaya: Thai Pandan Custard Dip", "https://www.cremedecornell.net/blog/2020/7/26/sangkhaya-thai-pandan-custard-dip/", "imageURL", "pubID", "pubName", "date", 10.0))
//        articledata.add(Article("", "Sangkhaya: Thai Pandan Custard Dip", "https://www.cremedecornell.net/blog/2020/7/26/sangkhaya-thai-pandan-custard-dip/", "imageURL", "pubID", "pubName", "date", 10.0))

        val fragmentAdapter = PagerAdapter(supportFragmentManager, onboardingdata, articledata)
        viewPager.adapter = fragmentAdapter
        tabbedLayout.setupWithViewPager(viewPager)

        for (i in 0 until tabbedLayout.tabCount) {
            Log.d("Int", i.toString())
            if (i == 0){
                tabbedLayout.getTabAt(i)?.setIcon(com.example.volume_android.R.drawable.ic_volumesvg_orange)
            }

            if (i == 1){
                tabbedLayout.getTabAt(i)?.setIcon(com.example.volume_android.R.drawable.ic_book_gray)
            }
            if(i==2) {
                tabbedLayout.getTabAt(i)?.setIcon(com.example.volume_android.R.drawable.ic_bookmark_gray)
            }


        }

        viewPager?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        tabbedLayout.getTabAt(0)?.setIcon(com.example.volume_android.R.drawable.ic_volumesvg_orange)
                        tabbedLayout.getTabAt(1)?.setIcon(com.example.volume_android.R.drawable.ic_book_gray)
                        tabbedLayout.getTabAt(2)?.setIcon(com.example.volume_android.R.drawable.ic_bookmark_gray)
                                            }
                    1 -> {
                        tabbedLayout.getTabAt(0)?.setIcon(com.example.volume_android.R.drawable.ic_volumesvg_gray)
                        tabbedLayout.getTabAt(1)?.setIcon(com.example.volume_android.R.drawable.ic_book_orange)
                        tabbedLayout.getTabAt(2)?.setIcon(com.example.volume_android.R.drawable.ic_bookmark_gray)
                    }
                    2 ->{
                        tabbedLayout.getTabAt(0)?.setIcon(com.example.volume_android.R.drawable.ic_volumesvg_gray)
                        tabbedLayout.getTabAt(1)?.setIcon(com.example.volume_android.R.drawable.ic_book_gray)
                        tabbedLayout.getTabAt(2)?.setIcon(com.example.volume_android.R.drawable.ic_bookmark_orange)}
                }
            }

        })
    }
}