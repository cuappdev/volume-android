package com.example.volume_android

import PrefUtils
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView

import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.volume_android.adapters.ArticleAdapter
import com.example.volume_android.adapters.FollowingHorizontalAdapter
import com.example.volume_android.adapters.HomeFollowingArticleAdapters
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
    private lateinit var profile_desc: TextView
    private lateinit var profile_articles_rv: RecyclerView
    private lateinit var publication:Publication
    private lateinit var graphQPublication: Publication

    val disposables = CompositeDisposable()

    val graphQlUtil = GraphQlUtil()

    val prefUtils = PrefUtils()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.publication_profile_activity)

        val currentFollowingSet = prefUtils.getStringSet("following", mutableSetOf())?.toMutableSet()

        profile_banner = findViewById(R.id.publication_banner)
        profile_logo = findViewById(R.id.publication_logo)
        profile_name = findViewById(R.id.publication_name)
        profile_follow = findViewById(R.id.follow_button)
        profile_articles = findViewById(R.id.publication_article_count)
        profile_shoutouts = findViewById(R.id.shout_count)
        profile_desc = findViewById(R.id.publication_description)
        profile_articles_rv = findViewById(R.id.article_rv)
        publication = intent.getParcelableExtra("publication")!!

        getPublication(publication.id)


        Log.d("PUBLICATION", publication.id + " "  + publication.name + " " + publication.shoutouts?.toInt().toString() + " " +   publication.bio + " " + publication.backgroundImageURL + " " + publication.profileImageURL)


        if(currentFollowingSet!!.contains(publication.id)){
            follow_button.apply {
                text = "Following"
                setTextColor(ContextCompat.getColor(this.context, R.color.ligthgray))
                setBackgroundResource(R.drawable.rounded_rectange_button_orange)
            }
        }
        else{
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
                    currentFollowingSet?.remove(publication.id)
                    if (currentFollowingSet != null) {
                        prefUtils.save("following", currentFollowingSet)
                    }
                }

            }
            else {
                follow_button.apply {
                    text = "Following"
                    setTextColor(ContextCompat.getColor(this.context, R.color.ligthgray))
                    setBackgroundResource(R.drawable.rounded_rectange_button_orange)
                    currentFollowingSet?.add(publication.id)
                    if (currentFollowingSet != null) {
                        prefUtils.save("following", currentFollowingSet)
                    }
                }
            }
        }

        setUpArticleRV()

    }

    private fun setUpArticleRV(){
        var articles = mutableListOf<Article>()
        var tempArticles = mutableListOf<Article>()
        val followingObs = graphQlUtil.getArticleByPublicationID(publication.id).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        disposables.add(followingObs.subscribe{

            it.data?.getArticlesByPublicationID?.mapTo(tempArticles, { it -> Article(title = it.title, articleURL =  it.articleURL, date =  it.date.toString(), id= it.id, imageURL = it.imageURL, publication = Publication(id = it.publication.id, name = it.publication.name, profileImageURL=publication.profileImageURL), shoutouts = it.shoutouts)
            })
            articles.addAll(tempArticles)

            profile_articles_rv.adapter = ArticleAdapter(articles)
            profile_articles_rv.layoutManager = LinearLayoutManager(this)
            profile_articles_rv.setHasFixedSize(true)


        })


    }

    private fun getPublication(pub : String){

        val followingObs = graphQlUtil.getPublicationById(pub).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        disposables.add(followingObs.subscribe {

            val res = it.data?.getPublicationByID
            graphQPublication = Publication(res!!.id, res.backgroundImageURL,
                    res.bio, res.name, res.profileImageURL, res.rssName, res.rssURL, res.slug, res.shoutouts, res.websiteURL, Article(res.mostRecentArticle?.id, res.mostRecentArticle?.title, res.mostRecentArticle?.articleURL, res.mostRecentArticle?.imageURL))

            profile_name.text = graphQPublication.name
            profile_shoutouts.text = graphQPublication.shoutouts?.toInt().toString() + " shoutouts"
            profile_desc.text = graphQPublication.bio
            Picasso.get().load(graphQPublication.backgroundImageURL).into(profile_banner)
            Picasso.get().load(graphQPublication.profileImageURL).into(profile_logo)

        })
    }
}
