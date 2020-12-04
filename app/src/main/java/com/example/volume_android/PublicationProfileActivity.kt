package com.example.volume_android

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView

import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.volume_android.adapters.ArticleAdapter
import com.example.volume_android.models.Article
import com.example.volume_android.models.Publication
import kotlinx.android.synthetic.main.publication_profile_activity.*


class PublicationProfileActivity : AppCompatActivity() {

    private lateinit var profile_banner : ImageView
    private lateinit var profile_logo : ImageView
    private lateinit var profile_name: TextView
    private lateinit var profile_follow: Button
    private lateinit var profile_articles: TextView
    private lateinit var profile_shoutouts: TextView
    private lateinit var profile_desc: TextView
    private lateinit var profile_articles_rv: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.publication_profile_activity)

        profile_banner = findViewById(R.id.publication_banner)
        profile_logo = findViewById(R.id.publication_logo)
        profile_name = findViewById(R.id.publication_name)
        profile_follow = findViewById(R.id.follow_button)
        profile_articles = findViewById(R.id.publication_article_count)
        profile_shoutouts = findViewById(R.id.shout_count)
        profile_desc = findViewById(R.id.publication_description)
        profile_articles_rv = findViewById(R.id.article_rv)

        //TODO: Dummy Data
        val articledata : ArrayList<Article>  = ArrayList()
        articledata.add(Article("https://www.cremedecornell.net/blog/2020/7/26/sangkhaya-thai-pandan-custard-dip/", "fakedate", 1, "url", "1A", 10.0f, "Sangkhaya: Thai Pandan Custard Dip"))
        articledata.add(Article("https://www.cremedecornell.net/blog/2020/7/26/sangkhaya-thai-pandan-custard-dip/", "fakedate", 2, "url", "1A", 10.0f, "Sangkhaya: Thai Pandan Custard Dip"))
        articledata.add(Article("https://www.cremedecornell.net/blog/2020/7/26/sangkhaya-thai-pandan-custard-dip/", "fakedate", 3, "url", "1A", 10.0f, "Sangkhaya: Thai Pandan Custard Dip"))
        articledata.add(Article("https://www.cremedecornell.net/blog/2020/7/26/sangkhaya-thai-pandan-custard-dip/", "fakedate", 4, "url", "1A", 10.0f, "Sangkhaya: Thai Pandan Custard Dip"))
        articledata.add(Article("https://www.cremedecornell.net/blog/2020/7/26/sangkhaya-thai-pandan-custard-dip/", "fakedate", 5, "url", "1A", 10.0f, "Sangkhaya: Thai Pandan Custard Dip"))

        profile_articles_rv.adapter = ArticleAdapter(articledata)
        profile_articles_rv.layoutManager = LinearLayoutManager(this)
        profile_articles_rv.setHasFixedSize(true)

        follow_button.setOnClickListener {
            if(follow_button.text.equals("Following")){
                follow_button.apply{
                    text = " +  Follow"
                    setBackgroundColor(ContextCompat.getColor(this.context, R.color.ligthgray))
                }

            }
            else {
                follow_button.apply {
                    text = "Following"
                    setBackgroundColor(ContextCompat.getColor(this.context, R.color.volumeOrange))
                }
            }
        }

    }
}
