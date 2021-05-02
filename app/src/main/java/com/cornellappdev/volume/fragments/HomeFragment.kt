package com.cornellappdev.volume.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cornellappdev.volume.R
import com.cornellappdev.volume.adapters.BigReadHomeAdapter
import com.cornellappdev.volume.adapters.HomeArticlesAdapter
import com.cornellappdev.volume.databinding.FragmentHomeBinding
import com.cornellappdev.volume.models.Article
import com.cornellappdev.volume.models.Publication
import com.cornellappdev.volume.models.Social
import com.cornellappdev.volume.util.GraphQlUtil
import com.cornellappdev.volume.util.GraphQlUtil.Companion.hasInternetConnection
import com.cornellappdev.volume.util.PrefUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers


class HomeFragment : Fragment() {

    private lateinit var bigRedRV: RecyclerView
    private lateinit var followingRV: RecyclerView
    private lateinit var otherRV: RecyclerView
    private lateinit var disposables: CompositeDisposable
    private lateinit var graphQlUtil: GraphQlUtil
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val prefUtils = PrefUtils()

    companion object {
        private const val NUMBER_OF_TRENDING_ARTICLES = 7.0
        private const val NUMBER_OF_FOLLOWING_ARTICLES = 20
        private const val NUMBER_OF_OTHER_ARTICLES = 45
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        disposables = CompositeDisposable()
        graphQlUtil = GraphQlUtil()

        setUpHomeView(binding, isRefreshing = false)

        val volumeOrange: Int? = context?.let { ContextCompat.getColor(it, R.color.volume_orange) }
        if (volumeOrange != null) {
            binding.srlQuery.setColorSchemeColors(volumeOrange, volumeOrange, volumeOrange)
        }
        binding.srlQuery.setOnRefreshListener {
            binding.cover.visibility = View.VISIBLE
            setUpHomeView(binding, isRefreshing = (
                    this::bigRedRV.isInitialized &&
                            this::followingRV.isInitialized &&
                            this::otherRV.isInitialized)
            )
            binding.srlQuery.isRefreshing = false
        }
        return binding.root
    }


    private fun setUpHomeView(binding: FragmentHomeBinding, isRefreshing: Boolean) {
        val followingPublications = prefUtils.getStringSet(PrefUtils.FOLLOWING_KEY, mutableSetOf())?.toMutableList()
        // Get the trending articles for Big Read section
        val trendingArticles = mutableListOf<Article>()
        val trendingArticlesId = mutableListOf<String>()
        val trendingObs =
                graphQlUtil.getTrendingArticles(NUMBER_OF_TRENDING_ARTICLES)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
        disposables.add(hasInternetConnection().subscribe { hasInternet ->
            if (hasInternet) {
                childFragmentManager.findFragmentByTag(NoInternetDialog.TAG).let { dialogFrag ->
                    (dialogFrag as? DialogFragment)?.dismiss()
                }
                binding.clHomePage.visibility = View.VISIBLE
                disposables.add(trendingObs.subscribe { response ->
                    val rawTrendingArticles = response.data?.getTrendingArticles
                    if (rawTrendingArticles != null) {
                        for (trendingArticle in rawTrendingArticles) {
                            val publication = trendingArticle.publication
                            trendingArticles.add(Article(
                                    title = trendingArticle.title,
                                    articleURL = trendingArticle.articleURL,
                                    date = trendingArticle.date.toString(),
                                    id = trendingArticle.id,
                                    imageURL = trendingArticle.imageURL,
                                    publication = Publication(
                                            id = publication.id,
                                            backgroundImageURL = publication.backgroundImageURL,
                                            bio = publication.bio,
                                            name = publication.name,
                                            profileImageURL = publication.profileImageURL,
                                            rssName = publication.rssName,
                                            rssURL = publication.rssURL,
                                            slug = publication.slug,
                                            shoutouts = publication.shoutouts,
                                            websiteURL = publication.websiteURL,
                                            socials = publication.socials.toList().map { Social(it.social, it.uRL) }),
                                    shoutouts = trendingArticle.shoutouts,
                                    nsfw = trendingArticle.nsfw))
                            trendingArticlesId.add(trendingArticle.id)
                        }
                    }
                    if (!isRefreshing) {
                        bigRedRV = binding.rvBigRead
                        bigRedRV.adapter = BigReadHomeAdapter(trendingArticles)
                        val linearLayoutManager = LinearLayoutManager(context)
                        linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
                        bigRedRV.layoutManager = linearLayoutManager
                    } else {
                        val adapter = bigRedRV.adapter as BigReadHomeAdapter
                        adapter.clear()
                        adapter.addAll(trendingArticles)
                    }
                })
                // Retrieve articles from those followed
                var followingArticles = mutableListOf<Article>()
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
                                    val publication = article.publication
                                    Article(
                                            title = article.title,
                                            articleURL = article.articleURL,
                                            date = article.date.toString(),
                                            id = article.id,
                                            imageURL = article.imageURL,
                                            publication = Publication(
                                                    id = publication.id,
                                                    backgroundImageURL = publication.backgroundImageURL,
                                                    bio = publication.bio,
                                                    name = publication.name,
                                                    profileImageURL = publication.profileImageURL,
                                                    rssName = publication.rssName,
                                                    rssURL = publication.rssURL,
                                                    slug = publication.slug,
                                                    shoutouts = publication.shoutouts,
                                                    websiteURL = publication.websiteURL,
                                                    socials = publication.socials.toList().map { Social(it.social, it.uRL) }),
                                            shoutouts = article.shoutouts,
                                            nsfw = article.nsfw)
                                })
                                followingArticles = followingArticles.filter { article ->
                                    !trendingArticlesId.contains(article.id)
                                } as MutableList<Article>
                                if (followingArticles.isNotEmpty()) {
                                    followingArticles = Article.sortByDate(followingArticles)
                                    if (!isRefreshing) {
                                        followingRV = binding.rvFollowing
                                        followingRV.layoutManager = LinearLayoutManager(context)
                                        followingRV.adapter = HomeArticlesAdapter(
                                                followingArticles.take(NUMBER_OF_FOLLOWING_ARTICLES)
                                                        as MutableList<Article>
                                        )
                                    } else {
                                        val adapter = followingRV.adapter as HomeArticlesAdapter
                                        adapter.clear()
                                        adapter.addAll(followingArticles.take(NUMBER_OF_FOLLOWING_ARTICLES))
                                    }
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
                } else if (isRefreshing) {
                    val adapter = followingRV.adapter as HomeArticlesAdapter
                    adapter.clear()
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
                disposables.add(allPublicationsObs.subscribe { response ->
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
                        disposables.add(otherObs.subscribe { articleResponse ->
                            if (articleResponse.data?.getArticlesByPublicationIDs != null) {
                                articleResponse.data?.getArticlesByPublicationIDs?.mapTo(otherArticles, { article ->
                                    val publication = article.publication
                                    Article(
                                            title = article.title,
                                            articleURL = article.articleURL,
                                            date = article.date.toString(),
                                            id = article.id,
                                            imageURL = article.imageURL,
                                            publication = Publication(
                                                    id = publication.id,
                                                    backgroundImageURL = publication.backgroundImageURL,
                                                    bio = publication.bio,
                                                    name = publication.name,
                                                    profileImageURL = publication.profileImageURL,
                                                    rssName = publication.rssName,
                                                    rssURL = publication.rssURL,
                                                    slug = publication.slug,
                                                    shoutouts = publication.shoutouts,
                                                    websiteURL = publication.websiteURL,
                                                    socials = publication.socials.toList().map { Social(it.social, it.uRL) }),
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
                                    if (!isRefreshing) {
                                        otherRV = binding.rvOtherArticles
                                        otherRV.layoutManager = LinearLayoutManager(context)
                                        otherRV.adapter = HomeArticlesAdapter(otherArticles.shuffled()
                                                as MutableList<Article>, isOtherArticles = true)
                                    } else {
                                        val adapter = otherRV.adapter as HomeArticlesAdapter
                                        adapter.clear()
                                        adapter.addAll(otherArticles.shuffled())
                                    }
                                }
                                binding.cover.visibility = View.GONE
                            }
                        })
                    }
                })
            } else {
                binding.clHomePage.visibility = View.GONE
                val ft = childFragmentManager.beginTransaction()
                val dialog = NoInternetDialog()
                ft.replace(binding.fragmentContainer.id, dialog, NoInternetDialog.TAG).commit()
                binding.cover.visibility = View.GONE
            }
        })
        if (followingPublications?.isEmpty() == true) {
            binding.groupFollowing.visibility = View.INVISIBLE
            binding.groupNotFollowing.visibility = View.VISIBLE
        } else {
            binding.groupFollowing.visibility = View.VISIBLE
            binding.groupNotFollowing.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
