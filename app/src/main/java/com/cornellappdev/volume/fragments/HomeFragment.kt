package com.cornellappdev.volume.fragments

import PrefUtils
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.Group
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cornellappdev.volume.R
import com.cornellappdev.volume.TabbedActivity
import com.cornellappdev.volume.adapters.BigReadHomeAdapter
import com.cornellappdev.volume.adapters.HomeFollowingArticleAdapters
import com.cornellappdev.volume.adapters.HomeOtherArticleAdapter
import com.cornellappdev.volume.models.Article
import com.cornellappdev.volume.models.Publication
import com.cornellappdev.volume.util.GraphQlUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class HomeFragment : Fragment() {

    private lateinit var bigRedRv: RecyclerView
    private lateinit var followingRv: RecyclerView
    private lateinit var otherRV: RecyclerView
    private lateinit var disposables: CompositeDisposable
    private lateinit var graphQlUtil: GraphQlUtil
    private lateinit var homeView: View
    private val prefUtils: PrefUtils = PrefUtils()

    companion object {
        private const val NUMBER_OF_TRENDING_ARTICLES = 7.0
        private const val NUMBER_OF_FOLLOWING_ARTICLES = 20
        private const val NUMBER_OF_OTHER_ARTICLES = 45
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        homeView = inflater.inflate(R.layout.home_fragment, container, false)
        disposables = CompositeDisposable()
        graphQlUtil = GraphQlUtil()
        setUpHomeView()
        return homeView
    }

    fun setUpHomeView() {
        val followingPublications = prefUtils.getStringSet("following", mutableSetOf())?.toMutableList()
        // Get the trending articles for Big Read section
        val trendingArticles = mutableListOf<Article>()
        val trendingArticlesId = mutableListOf<String>()
        val trendingObs =
                graphQlUtil.getTrendingArticles(NUMBER_OF_TRENDING_ARTICLES)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
        disposables.add(trendingObs.subscribe{ response ->
            val rawTrendingArticles = response.data?.getTrendingArticles
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
                            shoutouts = rawArticle.shoutouts,
                            nsfw = rawArticle.nsfw)
                    )
                    trendingArticlesId.add(rawArticle.id)
                }
            }
            bigRedRv = homeView.findViewById(R.id.big_red_rv)
            bigRedRv.adapter = BigReadHomeAdapter(trendingArticles)
            val linearLayoutManager = LinearLayoutManager(homeView.context)
            linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
            bigRedRv.layoutManager = linearLayoutManager
        })
        // Retrieve articles from those followed
        var followingArticles: MutableList<Article> = mutableListOf()
        if (!followingPublications.isNullOrEmpty()) {
            val followingObs =
                    graphQlUtil.getArticleByPublicationIDs(followingPublications)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
            if (followingObs != null) {
                disposables.add(followingObs.subscribe { response ->
                    if (response.data?.getArticlesByPublicationIDs != null) {
                        response.data?.getArticlesByPublicationIDs?.mapTo(
                                followingArticles, { article ->
                            Article(
                                    title = article.title,
                                    articleURL = article.articleURL,
                                    date = article.date.toString(),
                                    id = article.id,
                                    imageURL = article.imageURL,
                                    publication = Publication(
                                            id = article.publication.id,
                                            name = article.publication.name,
                                            profileImageURL = article.publication.profileImageURL),
                                    shoutouts = article.shoutouts,
                                    nsfw = article.nsfw)
                        })
                        followingArticles = followingArticles.filter { article ->
                            !trendingArticlesId.contains(article.id)
                        } as MutableList<Article>
                        if (followingArticles.isNotEmpty()) {
                            followingArticles = followingArticles.sortedWith(
                                    compareByDescending {
                                        article -> article.date
                                    }) as MutableList<Article>
                            followingRv = homeView.findViewById(R.id.follwing_rv)
                            followingRv.layoutManager = LinearLayoutManager(homeView.context)
                            followingRv.adapter = HomeFollowingArticleAdapters(
                                    followingArticles.take(NUMBER_OF_FOLLOWING_ARTICLES)
                                            as MutableList<Article>
                            )
                            followingArticles = if (followingArticles.size
                                    <= NUMBER_OF_FOLLOWING_ARTICLES) {
                                mutableListOf()
                            } else {
                                followingArticles.drop(NUMBER_OF_FOLLOWING_ARTICLES)
                                        as MutableList<Article>
                            }
                        }
                    }
                })
            }
        }
        // Get the articles for the other section, first taken from
        // publications the user doesn't follow then so to make up the difference
        // of the amount needed.
        var allPublicationIdsExcludingFollowing = mutableListOf<String>()
        var otherArticles = mutableListOf<Article>()
        val allPublicationsObs =
                graphQlUtil.getAllPublications()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
        disposables.add(allPublicationsObs.subscribe{ response ->
            val rawPublications = response.data?.getAllPublications
            if (rawPublications != null) {
                for (publication in rawPublications) {
                    allPublicationIdsExcludingFollowing.add(publication.id)
                }
            }
            if (!followingPublications.isNullOrEmpty()) {
                allPublicationIdsExcludingFollowing =
                        allPublicationIdsExcludingFollowing.filter { pubID ->
                            !followingPublications.contains(pubID)
                        } as MutableList<String>
            }
            val otherObs = graphQlUtil
                    .getArticleByPublicationIDs(allPublicationIdsExcludingFollowing)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
            if (otherObs != null) {
                disposables.add(otherObs.subscribe {
                    if (it.data?.getArticlesByPublicationIDs != null) {
                        it.data?.getArticlesByPublicationIDs?.mapTo(otherArticles, { article ->
                            Article(
                                    article.id,
                                    article.title,
                                    article.articleURL,
                                    article.imageURL,
                                    Publication(id = article.publication.id,
                                            name = article.publication.name,
                                            profileImageURL = article.publication.profileImageURL),
                                    article.date.toString(),
                                    shoutouts = article.shoutouts,
                                    nsfw = article.nsfw)
                        })
                        otherArticles = otherArticles.filter { article ->
                            !trendingArticlesId.contains(article.id)
                        } as MutableList<Article>
                        if (otherArticles.isNotEmpty()) {
                            if (otherArticles.size < NUMBER_OF_OTHER_ARTICLES &&
                                    !followingArticles.isNullOrEmpty()) {
                                otherArticles.addAll(followingArticles.take(
                                        NUMBER_OF_OTHER_ARTICLES - otherArticles.size))
                            }
                            otherRV = homeView.findViewById(R.id.other_articlesrv)
                            otherRV.layoutManager = LinearLayoutManager(homeView.context)
                            otherRV.adapter = HomeOtherArticleAdapter(otherArticles.shuffled())
                        }
                    }
                })
            }
        })
        if (followingPublications?.isEmpty() == true) {
            homeView.findViewById<Group>(R.id.following_group).visibility = View.INVISIBLE
            homeView.findViewById<Group>(R.id.not_following_group).visibility = View.VISIBLE
        } else {
            homeView.findViewById<Group>(R.id.following_group).visibility = View.VISIBLE
            homeView.findViewById<Group>(R.id.not_following_group).visibility = View.GONE
        }
    }

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }
}
