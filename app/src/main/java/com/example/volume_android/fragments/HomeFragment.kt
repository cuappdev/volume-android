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


class HomeFragment(val articles: List<Article>) : Fragment() {

    companion object {
        const val NUMBER_OF_TRENDING_ARTICLES = 7.0
        const val NUMBER_OF_FOLLOWING_ARTICLES = 20
        const val NUMBER_OF_OTHER_ARTICLES = 45
    }


    private lateinit var bigRedRv: RecyclerView
    private lateinit var followingRv: RecyclerView
    private lateinit var otherArticles: RecyclerView
    private lateinit var disposables: CompositeDisposable
    private lateinit var graphQlUtil: GraphQlUtil
    private val prefUtils: PrefUtils = PrefUtils()


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view1 = inflater.inflate(R.layout.home_fragment, container, false)
        val followingPublications = prefUtils.getStringSet("following", mutableSetOf())?.toMutableSet()
        disposables = CompositeDisposable()

        Log.d("FOLLOWING", followingPublications.toString())
        Log.d("HomeFragment", "reloaded")
        Log.d("HomeFragment", followingPublications.toString())
        graphQlUtil = GraphQlUtil()

        val allPublicationIdsExcludingFollowing: List<String> = getAllPublicationIds(followingPublications)
        val trendingArticlesId = setUpBigRedArticles(view1)

        // Get all articles for the publications the user follows
        // the followed articles are pulled from shared preferences
        var followingArticles: List<Article> = setUpFollowingArticles(followingPublications,
                trendingArticlesId)

        followingRv = view1.findViewById(R.id.follwing_rv)
        val linearLayoutManager2 = LinearLayoutManager(view1.context)
        followingRv.layoutManager = linearLayoutManager2
        followingRv.adapter = HomeFollowingArticleAdapters(
                followingArticles.take(NUMBER_OF_FOLLOWING_ARTICLES)
        )
        followingArticles = followingArticles.drop(NUMBER_OF_FOLLOWING_ARTICLES)
        setUpOtherArticles(view1,
                trendingArticlesId,
                allPublicationIdsExcludingFollowing,
                followingArticles)
        if(followingPublications?.isEmpty() == true) {
            view1.findViewById<Group>(R.id.following_group).visibility = View.GONE
        } else {
            view1.findViewById<Group>(R.id.following_group).visibility = View.VISIBLE
        }
        return view1
    }

    private fun getAllPublicationIds(followingPublications: MutableSet<String>?): List<String> {
        val pubIds = mutableListOf<String>()
        val allPublicationsObs =
                graphQlUtil.getAllPublications().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        disposables.add(allPublicationsObs.subscribe {
            val rawPublications = it.data?.getAllPublications
            rawPublications?.mapTo(pubIds, { rawPublication -> rawPublication.id })
        })
        if(!followingPublications.isNullOrEmpty()) {
            return pubIds.filter {
                followingPublications.contains(it)
            }
        }
        return pubIds
    }

    private fun setUpBigRedArticles(view1: View?): MutableList<String> {
        val trendingArticles = mutableListOf<Article>()
        val trendingArticlesId = mutableListOf<String>()
        // Get the trending articles for Big Read section
        val trendingObs = graphQlUtil.getTrendingArticles(NUMBER_OF_TRENDING_ARTICLES).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
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
            bigRedRv = view1?.findViewById(R.id.big_red_rv)!!
            bigRedRv.adapter = BigReadHomeAdapter(trendingArticles)
            val linearLayoutManager = LinearLayoutManager(view1.context)
            linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
            bigRedRv.layoutManager = linearLayoutManager
        })
        return trendingArticlesId
    }

    private fun setUpFollowingArticles(followingPublications: MutableSet<String>?,
                                       trendingArticlesId: MutableList<String>): List<Article> {
        val followingArticles: MutableList<Article> = mutableListOf()
        for (pub in followingPublications!!) {
            val followingObs = graphQlUtil.getArticleByPublication(pub).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            disposables.add(followingObs.subscribe {
                if (it.data?.getArticlesByPublication != null) {
                    for (rawArticle in it.data?.getArticlesByPublication!!) {
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
            })
        }
        return followingArticles.sortedWith(compareBy { it.date })
    }

    private fun setUpOtherArticles(view1: View?,
                                   trendingArticlesId: MutableList<String>,
                                   allPublicationIdsExcludingFollowing: List<String>,
                                   followingArticles: List<Article>) {
        val others = mutableListOf<Article>()
        for (pub in allPublicationIdsExcludingFollowing) {
            val othersObs = graphQlUtil.getArticleByPublication(pub).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            disposables.add(othersObs.subscribe {
                if (it.data?.getArticlesByPublication != null) {
                    for (rawArticle in it.data?.getArticlesByPublication!!) {
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
            })
        }
        if(others.size < NUMBER_OF_OTHER_ARTICLES) {
            others.addAll(followingArticles.take(NUMBER_OF_OTHER_ARTICLES - others.size))
        }
        if(view1 != null) {
            otherArticles = view1.findViewById(R.id.other_articlesrv)
            val linearLayoutManager3 = LinearLayoutManager(view1.context)
            otherArticles.layoutManager = linearLayoutManager3
            otherArticles.adapter = HomeOtherArticleAdapter(others)
        }
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