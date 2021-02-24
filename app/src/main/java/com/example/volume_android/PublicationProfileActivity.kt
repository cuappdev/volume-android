package com.example.volume_android

import PrefUtils
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.volume_android.adapters.ArticleAdapter
import com.example.volume_android.models.Article
import com.example.volume_android.models.Publication
import com.example.volume_android.util.GraphQlUtil
import com.squareup.picasso.Picasso
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.publication_profile_activity.*


class PublicationProfileActivity : AppCompatActivity() {

    private lateinit var profile_banner : ImageView
    private lateinit var profile_logo : ImageView
    private lateinit var profile_name: TextView
    private lateinit var profile_follow: Button
    private lateinit var profile_articles: TextView
    private lateinit var profile_shoutouts: TextView
    private lateinit var profile_link_holder: ConstraintLayout
    private lateinit var profile_link: TextView
    private lateinit var profile_desc: TextView
    private lateinit var profile_articles_rv: RecyclerView
    private lateinit var publication:Publication

    val disposables = CompositeDisposable()

    val graphQlUtil = GraphQlUtil()

    val prefUtils = PrefUtils()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.publication_profile_activity)

        val currentFollowingSet =
                prefUtils.getStringSet("following", mutableSetOf())?.toMutableSet()

        profile_banner = findViewById(R.id.publication_banner)
        profile_logo = findViewById(R.id.publication_logo)
        profile_name = findViewById(R.id.publication_name)
        profile_follow = findViewById(R.id.follow_button)
        profile_articles = findViewById(R.id.publication_article_count)
        profile_shoutouts = findViewById(R.id.shout_count)
        profile_link_holder = findViewById(R.id.link_holder)
        profile_link = findViewById(R.id.link_text)
        profile_desc = findViewById(R.id.publication_description)
        profile_articles_rv = findViewById(R.id.article_rv)
        publication = intent.getParcelableExtra("publication")!!
        getPublication(publication.id)

        if(currentFollowingSet!!.contains(publication.id)) {
            follow_button.apply {
                text = "Following"
                setTextColor(ContextCompat.getColor(this.context, R.color.ligthgray))
                setBackgroundResource(R.drawable.rounded_rectange_button_orange)
            }
        } else {
            follow_button.apply {
                text = " +  Follow"
                setBackgroundResource(R.drawable.rounded_rectangle_button)}
        }

        follow_button.setOnClickListener {
            if(follow_button.text.equals("Following")){
                follow_button.apply{
                    text = " +  Follow"
                    setBackgroundResource(R.drawable.rounded_rectangle_button)
                    setTextColor(ContextCompat.getColor(this.context, R.color.volumeOrange))
                    currentFollowingSet.remove(publication.id)
                    prefUtils.save("following", currentFollowingSet)
                }

            }
            else {
                follow_button.apply {
                    text = "Following"
                    setTextColor(ContextCompat.getColor(this.context, R.color.ligthgray))
                    setBackgroundResource(R.drawable.rounded_rectange_button_orange)
                    currentFollowingSet.add(publication.id)
                    prefUtils.save("following", currentFollowingSet)
                }
            }
        }
        setUpArticleRV()
    }

    private fun setUpArticleRV(){
        val articles = mutableListOf<Article>()
        val followingObs = graphQlUtil
                .getArticleByPublicationID(publication.id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        disposables.add(followingObs.subscribe {
            it.data?.getArticlesByPublicationID?.mapTo(articles, { article ->
                Article(
                        title = article.title,
                        articleURL = article.articleURL,
                        date = article.date.toString(),
                        id = article.id,
                        imageURL = article.imageURL,
                        publication = Publication(
                                id = article.publication.id,
                                name = article.publication.name,
                                profileImageURL = publication.profileImageURL),
                        shoutouts = article.shoutouts)
            })
            profile_articles_rv.adapter = ArticleAdapter(articles)
            profile_articles_rv.layoutManager = LinearLayoutManager(this)
            profile_articles_rv.setHasFixedSize(true)
        })
    }

    private fun getPublication(pub: String){
        var instaURL = ""
        var facebookURL = ""
        val followingObs = graphQlUtil.getPublicationByID(pub).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        disposables.add(followingObs.subscribe {
            val publication = it.data?.getPublicationByID
            if (publication != null) {
                for (rawSocial in publication.socialURLs) {
                    if (rawSocial.social == "insta") {
                        instaURL = rawSocial.uRL
                    } else if (rawSocial.social == "facebook") {
                        facebookURL = rawSocial.uRL
                    }
                }
                if (publication.websiteURL.isNotEmpty()) {
                    profile_link.text = publication.websiteURL
                    profile_link_holder.setOnClickListener {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(publication.websiteURL)))
                    }
                } else {
                    profile_link_holder.visibility = View.GONE
                }
                if (instaURL.isNotEmpty()) {
                    setUpInstaOnClick(instaURL)
                } else {
                    findViewById<ConstraintLayout>(R.id.insta_holder).visibility = View.GONE
                }
                if (facebookURL.isNotEmpty()) {
                    findViewById<ConstraintLayout>(R.id.fb_holder).setOnClickListener {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(facebookURL))
                        startActivity(intent)
                    }
                } else {
                    findViewById<ConstraintLayout>(R.id.fb_holder).visibility = View.GONE
                }
                profile_name.text = publication.name
                profile_shoutouts.text = publication.shoutouts?.toInt().toString() + " shoutouts"
                profile_desc.text = publication.bio
                Picasso.get().load(publication.backgroundImageURL).into(profile_banner)
                Picasso.get().load(publication.profileImageURL).into(profile_logo)
            }
        })
    }

    private fun setUpInstaOnClick(url: String) {
        val inAppURL =
                StringBuilder(url).insert(url.indexOf("com") + 4, "_u/").toString()
        val instaHolder: ConstraintLayout = findViewById(R.id.insta_holder)
        instaHolder.setOnClickListener {
            val uri: Uri = Uri.parse(inAppURL)
            val instaIntent = Intent(Intent.ACTION_VIEW, uri)
            instaIntent.setPackage("com.instagram.android")
            try {
                startActivity(instaIntent)
            } catch (e: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
        }
    }
}
