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
import java.util.*

class HomeFragment : Fragment() {
    private lateinit var bigRedRv: RecyclerView
    private lateinit var followingRv: RecyclerView
    private lateinit var otherArticles: RecyclerView
    private lateinit var disposables: CompositeDisposable
    private lateinit var graphQlUtil: GraphQlUtil
    private val prefUtils: PrefUtils = PrefUtils()

    companion object {
        private const val NUMBER_OF_TRENDING_ARTICLES = 7.0
        private const val NUMBER_OF_FOLLOWING_ARTICLES = 20
        private const val NUMBER_OF_OTHER_ARTICLES = 45
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view1 = inflater.inflate(R.layout.home_fragment, container, false)
        val followingPublications = prefUtils.getStringSet("following", mutableSetOf())?.toMutableSet()
        disposables = CompositeDisposable()
        graphQlUtil = GraphQlUtil()

        // Get the trending articles for Big Read section
        val trendingArticles = mutableListOf<Article>()
        val trendingArticlesId = mutableListOf<String>()
        val trendingObs = graphQlUtil.getTrendingArticles(NUMBER_OF_TRENDING_ARTICLES).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        disposables.add(trendingObs.subscribe({
            val rawTrendingArticles = it.data?.getTrendingArticles
            if (rawTrendingArticles != null) {
                for (rawArticle in rawTrendingArticles) {
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
            bigRedRv = view1?.findViewById(R.id.big_red_rv)!!
            bigRedRv.adapter = BigReadHomeAdapter(trendingArticles)
            val linearLayoutManager = LinearLayoutManager(view1.context)
            linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
            bigRedRv.layoutManager = linearLayoutManager
        }, { error -> Log.d("homerror_trending", error.toString()) }))
        // Retrieve articles from those followed
        var followingArticles: MutableList<Article> = mutableListOf()
        if (followingPublications != null) {
            for (pub in followingPublications) {
                Log.d("homerror_following", pub)
                val followingObs = graphQlUtil.getArticleByPublication(pub).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                disposables.add(followingObs.subscribe ({ response ->
                    if (response.data?.getArticlesByPublication != null) {
                        for (rawArticle in response.data?.getArticlesByPublication!!) {
                            if (!trendingArticlesId.contains(rawArticle.id)) {
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
                            }
                        }
                    }
                    if(pub == followingPublications.last() && followingArticles.isNotEmpty()) {
                        followingArticles = followingArticles.sortedWith(compareByDescending { it.date }) as MutableList<Article>
                        followingRv = view1.findViewById(R.id.follwing_rv)
                        followingRv.layoutManager = LinearLayoutManager(view1.context)
                        followingRv.adapter = HomeFollowingArticleAdapters(
                                followingArticles.take(NUMBER_OF_FOLLOWING_ARTICLES)
                        )
                        if(followingArticles.size < NUMBER_OF_FOLLOWING_ARTICLES) {
                            followingArticles = mutableListOf()
                        } else if(followingArticles.isNotEmpty()){
                            followingArticles = followingArticles.drop(NUMBER_OF_FOLLOWING_ARTICLES) as MutableList<Article>
                        }
                    }
                }, {
                    error -> Log.d("homerror_following", error.message.toString())
                    Log.d("homerror_following", error.localizedMessage.toString())
                    Log.d("homerror_following", error.stackTraceToString())
                    Log.d("homer", error.toString())
                }))
            }
        }
        // Get the articles for the other section, first taken from publications the user doesn't follow
        // then so to make up the difference of the amount needed.
        var allPublicationIdsExcludingFollowing = mutableListOf<String>()
        val others = mutableListOf<Article>()
        val allPublicationsObs =
                graphQlUtil.getAllPublications().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        disposables.add(allPublicationsObs.subscribe({ response ->
            val rawPublications = response.data?.getAllPublications
            if (rawPublications != null) {
                for (publication in rawPublications) {
                    allPublicationIdsExcludingFollowing.add(publication.id)
                }
            }
            if (!followingPublications.isNullOrEmpty()) {
                allPublicationIdsExcludingFollowing = allPublicationIdsExcludingFollowing.filter {
                    !followingPublications.contains(it)
                } as MutableList<String>
            }
            for (pub in allPublicationIdsExcludingFollowing) {
                //Log.d("homerror_other_inner", pub)
                val othersObs = graphQlUtil.getArticleByPublication(pub).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                disposables.add(othersObs.subscribe ({
                    Log.d("homerror_other_inner", "outside")
                    if (it.data?.getArticlesByPublication != null) {
                        for (rawArticle in it.data?.getArticlesByPublication!!) {
                            Log.d("homerror_other_inner", rawArticle.toString())
                            if (!trendingArticlesId.contains(rawArticle.id)) {
                                others.add(Article(
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
                            }
                        }
                    }
                    if (pub == allPublicationIdsExcludingFollowing.last() && others.isNotEmpty()) {
                        if (others.size < NUMBER_OF_OTHER_ARTICLES && !followingArticles.isNullOrEmpty()) {
                            others.addAll(followingArticles.take(NUMBER_OF_OTHER_ARTICLES - others.size))
                        }
                        otherArticles = view1.findViewById(R.id.other_articlesrv)
                        otherArticles.layoutManager = LinearLayoutManager(view1.context)
                        otherArticles.adapter = HomeOtherArticleAdapter(others.shuffled())
                    }
                }, { error -> Log.d("homerror_other_inner", error.toString()) }))
            }
        }, { error -> Log.d("homerror_other", error.toString()) }))
        if(followingPublications?.isEmpty() == true) {
            view1.findViewById<Group>(R.id.following_group).visibility = View.GONE
        } else {
            view1.findViewById<Group>(R.id.following_group).visibility = View.VISIBLE
        }
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
