package com.example.volume_android.fragments

import PrefUtils
import android.os.Bundle
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

class HomeFragment : Fragment() {
    private lateinit var bigRedRv: RecyclerView
    private lateinit var followingRv: RecyclerView
    private lateinit var otherRV: RecyclerView
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
        val followingPublications = prefUtils.getStringSet("following", mutableSetOf())?.toMutableList()
        disposables = CompositeDisposable()
        graphQlUtil = GraphQlUtil()

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
                            shoutouts = rawArticle.shoutouts)
                    )
                    trendingArticlesId.add(rawArticle.id)
                }
            }
            bigRedRv = view1.findViewById(R.id.big_red_rv)
            bigRedRv.adapter = BigReadHomeAdapter(trendingArticles)
            val linearLayoutManager = LinearLayoutManager(view1.context)
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
                                        shoutouts = article.shoutouts)
                            })
                            followingArticles = followingArticles.filter { article ->
                                !trendingArticlesId.contains(article.id)
                            } as MutableList<Article>
                            if (followingArticles.isNotEmpty()) {
                                followingArticles = followingArticles.sortedWith(
                                        compareByDescending {
                                    article -> article.date
                                }) as MutableList<Article>
                                followingRv = view1.findViewById(R.id.follwing_rv)
                                followingRv.layoutManager = LinearLayoutManager(view1.context)
                                followingRv.adapter = HomeFollowingArticleAdapters(
                                        followingArticles.take(NUMBER_OF_FOLLOWING_ARTICLES)
                                )
                                followingArticles =
                                        if (followingArticles.size < NUMBER_OF_FOLLOWING_ARTICLES) {
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
                                    shoutouts = article.shoutouts)
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
                            otherRV = view1.findViewById(R.id.other_articlesrv)
                            otherRV.layoutManager = LinearLayoutManager(view1.context)
                            otherRV.adapter = HomeOtherArticleAdapter(otherArticles.shuffled())
                        }
                    }
                })
            }
        })
        if (followingPublications?.isEmpty() == true) {
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
}
