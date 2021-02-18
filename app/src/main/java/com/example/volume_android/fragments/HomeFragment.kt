package com.example.volume_android.fragments

import PrefUtils
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.Group
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.volume_android.R
import com.example.volume_android.adapters.BigReadHomeAdapter
import com.example.volume_android.adapters.HomeFollowingArticleAdapters
import com.example.volume_android.adapters.HomeOtherArticleAdapter
import com.example.volume_android.models.Article
import com.example.volume_android.models.Publication
import com.example.volume_android.util.GraphQlUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*


class HomeFragment(val articles: List<Article>) : Fragment() {


    private lateinit var bigRedRv: RecyclerView
    private lateinit var followingRv: RecyclerView
    private lateinit var otherArticles: RecyclerView
    private lateinit var disposables: CompositeDisposable
    private val prefUtils: PrefUtils = PrefUtils()


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view1 = inflater.inflate(R.layout.home_fragment, container, false)


        val followingPublications = prefUtils.getStringSet("following", mutableSetOf())

        disposables = CompositeDisposable()

        Log.d("FOLLOWING", followingPublications.toString())
        Log.d("HomeFragment", "reloaded")
        Log.d("HomeFragment", followingPublications.toString())
        val graphQlUtil = GraphQlUtil()

        val calendar: Calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -14)
        val newDate: Date = calendar.time
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val dateTwoWeeksAgo = dateFormat.format(newDate)

        //while loop with outside boolean waiting to be true marking >= Big Read

        val trendingArticles = mutableListOf<Article>()
        val trendingArticlesId = mutableListOf<String>()
        // Get the trending articles for Big Read section
        val trendingObs = graphQlUtil.getTrendingArticles(10.0, dateTwoWeeksAgo).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        disposables.add(trendingObs.subscribe {
            if (it.data?.getTrendingArticles != null) {
                for (rawArticle in it.data?.getTrendingArticles!!) {
                    trendingArticles.add(Article(
                            title = rawArticle.title,
                            articleURL = rawArticle.articleURL,
                            date = rawArticle.date.toString(),
                            id = rawArticle.id,
                            imageURL = rawArticle.imageURL,
                            publication = Publication(
                                    id = rawArticle.publication.id,
                                    name = rawArticle.publication.name,
                                    profileImageURL = rawArticle.publication.profileImageURL),
                            shoutouts = rawArticle.shoutouts)
                    )
                    trendingArticlesId.add(rawArticle.id)
                }
            }
            bigRedRv = view1.findViewById(R.id.big_red_rv)!!
            bigRedRv.adapter = BigReadHomeAdapter(trendingArticles)
            val linearLayoutManager = LinearLayoutManager(view1.context)
            linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
            bigRedRv.layoutManager = linearLayoutManager
        })

        if(followingPublications?.isEmpty() == true) {
            view1.findViewById<Group>(R.id.following_group).visibility = View.GONE
        } else {
            view1.findViewById<Group>(R.id.following_group).visibility = View.VISIBLE
        }
        // Get all articles for the publications the user follows
        // the followed articles are pulled from shared preferences
        val followingArticles = mutableListOf<Article>()
        val followingArticlesId = mutableListOf<String>()
        for (pub in followingPublications!!) {
            val followingObs = graphQlUtil.getArticleByPublication(pub).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            disposables.add(followingObs.subscribe {
                if (it.data?.getArticlesByPublication != null) {
                    for (rawArticle in it.data?.getArticlesByPublication!!) {
                        followingArticles.add(Article(
                                title = rawArticle.title,
                                articleURL = rawArticle.articleURL,
                                date = rawArticle.date.toString(),
                                id = rawArticle.id,
                                imageURL = rawArticle.imageURL,
                                publication = Publication(
                                        id = rawArticle.publication.id,
                                        name = rawArticle.publication.name,
                                        profileImageURL = rawArticle.publication.profileImageURL),
                                shoutouts = rawArticle.shoutouts)
                        )
                        followingArticlesId.add(rawArticle.id)
                    }
                }
            })
        }
        followingArticles.filter { otherArticle ->
            followingArticlesId.contains(otherArticle.id)
        }
        followingRv = view1.findViewById(R.id.follwing_rv)
        val linearLayoutManager2 = LinearLayoutManager(view1.context)
        followingRv.layoutManager = linearLayoutManager2
        followingRv.adapter = HomeFollowingArticleAdapters(
                followingArticles.sortedWith(compareBy { it.date }).take(20)
        )

        // These are for the other section, i.e. the articles from the publications that the person does not follow
        // ideally we should filter out the articles from the publications that the person already follows (that filter out the Big Read)
        val otherObs = graphQlUtil.getArticlesAfterDate(dateTwoWeeksAgo).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        disposables.add(otherObs.subscribe { it ->
            val others = mutableListOf<Article>()
            it.data?.getArticlesAfterDate?.mapTo(others, { it ->
                Article(title = it.title, articleURL = it.articleURL, date = it.date.toString(), id = it.id, imageURL = it.imageURL, publication = Publication(id = it.publication.id, name = it.publication.name, profileImageURL = it.publication.profileImageURL), shoutouts = it.shoutouts)
            })
            others.filter { otherArticle ->
                followingArticlesId.contains(otherArticle.id)
            }
            otherArticles = view1.findViewById(R.id.other_articlesrv)
            val linearLayoutManager3: LinearLayoutManager = LinearLayoutManager(view1.context)
            otherArticles.layoutManager = linearLayoutManager3
            otherArticles.adapter = HomeOtherArticleAdapter(
                    others.sortedWith(compareBy { it.date })
            )

        })
        return view1
    }

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            fragmentManager!!.beginTransaction().detach(this).attach(this).commit()
        }
    }
}