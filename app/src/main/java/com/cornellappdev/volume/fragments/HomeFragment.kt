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
import com.apollographql.apollo.api.Response
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
import com.kotlin.graphql.AllPublicationsQuery
import com.kotlin.graphql.ArticlesByPublicationIDsQuery
import com.kotlin.graphql.TrendingArticlesQuery
import io.reactivex.Observable
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        disposables = CompositeDisposable()
        graphQlUtil = GraphQlUtil()

        setUpHomeView(binding, isRefreshing = false)

        val volumeOrange: Int? = context?.let { ContextCompat.getColor(it, R.color.volumeOrange) }
        if (volumeOrange != null) {
            binding.srlQuery.setColorSchemeColors(volumeOrange, volumeOrange, volumeOrange)
        }

        binding.srlQuery.setOnRefreshListener {
            setUpHomeView(
                binding, isRefreshing = (
                        this::bigRedRV.isInitialized &&
                                this::followingRV.isInitialized &&
                                this::otherRV.isInitialized)
            )
            binding.srlQuery.isRefreshing = false
        }
        return binding.root
    }

    private fun setUpHomeView(binding: FragmentHomeBinding, isRefreshing: Boolean) {
        val followingPublications =
            prefUtils.getStringSet(PrefUtils.FOLLOWING_KEY, mutableSetOf())?.toMutableList()

        val trendingArticles = mutableListOf<Article>()
        val trendingArticlesId = hashSetOf<String>()
        val followingArticles = mutableListOf<Article>()
        val allPublicationIdsExcludingFollowing = mutableListOf<String>()
        val otherArticles = mutableListOf<Article>()

        // Creates API call observation for retrieving trending articles.
        val trendingObs =
            graphQlUtil.getTrendingArticles(NUMBER_OF_TRENDING_ARTICLES)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())

        // Creates API call observation for retrieving articles from publications the user follows.
        val followingObs =
            followingPublications?.let {
                graphQlUtil.getArticleByPublicationIDs(it)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
            }

        // Creates API call observation for retrieving all publications in the Volume database.
        val allPublicationsObs =
            graphQlUtil.getAllPublications()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())

        disposables.add(hasInternetConnection().subscribe { hasInternet ->
            if (!hasInternet) {
                binding.clHomePage.visibility = View.GONE
                val ft = childFragmentManager.beginTransaction()
                val dialog = NoInternetDialog()
                ft.replace(
                    binding.fragmentContainer.id,
                    dialog,
                    NoInternetDialog.TAG
                ).commit()
            } else {
                childFragmentManager.findFragmentByTag(NoInternetDialog.TAG).let { dialogFrag ->
                    (dialogFrag as? DialogFragment)?.dismiss()
                }

                binding.clHomePage.visibility = View.VISIBLE

                handleTrendingObservable(
                    trendingObs,
                    isRefreshing,
                    trendingArticles,
                    trendingArticlesId
                )

                if (!followingPublications.isNullOrEmpty()) {
                    handleFollowingObservable(
                        followingObs,
                        isRefreshing,
                        followingArticles,
                        trendingArticlesId
                    )
                } else if (isRefreshing) {
                    // Can simply just clear adapter, since there's no following article data
                    // to populate from (the user doesn't follow any publications).
                    val adapter = followingRV.adapter as HomeArticlesAdapter
                    adapter.clear()
                }

                // Get the articles for the other section, first taken from
                // publications the user doesn't follow and then taken from publications the user does follow
                // to make up the difference of the amount needed.
                handleOtherSection(
                    allPublicationsObs,
                    isRefreshing,
                    followingPublications,
                    followingArticles,
                    allPublicationIdsExcludingFollowing,
                    otherArticles,
                    trendingArticlesId
                )
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

    private fun handleOtherSection(
        allPublicationsObs: Observable<Response<AllPublicationsQuery.Data>>,
        isRefreshing: Boolean,
        followingPublications: MutableList<String>?,
        followingArticles: MutableList<Article>,
        allPublicationIdsExcludingFollowing: MutableList<String>,
        otherArticles: MutableList<Article>,
        trendingArticlesId: HashSet<String>
    ) {
        if (allPublicationsObs != null) {
            disposables.add(allPublicationsObs.subscribe { response ->
                val rawPublications = response.data?.getAllPublications
                if (rawPublications != null) {
                    for (publication in rawPublications) {
                        allPublicationIdsExcludingFollowing.add(publication.id)
                    }
                }
                if (!followingPublications.isNullOrEmpty()) {
                    allPublicationIdsExcludingFollowing.removeAll { pubID ->
                        followingPublications.contains(pubID)
                    }
                }

                val otherObs = graphQlUtil
                    .getArticleByPublicationIDs(allPublicationIdsExcludingFollowing)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

                handleOtherObservable(
                    otherObs,
                    isRefreshing,
                    followingArticles,
                    otherArticles,
                    trendingArticlesId
                )
            })
        }
    }

    private fun handleOtherObservable(
        otherObs: Observable<Response<ArticlesByPublicationIDsQuery.Data>>?,
        isRefreshing: Boolean,
        followingArticles: MutableList<Article>,
        otherArticles: MutableList<Article>,
        trendingArticlesId: HashSet<String>
    ) {
        if (otherObs != null) {
            disposables.add(otherObs.subscribe { response ->
                retrieveOtherArticlesFromResponse(response, otherArticles)

                otherArticles.removeAll { article ->
                    trendingArticlesId.contains(article.id)
                }

                if (otherArticles.size < NUMBER_OF_OTHER_ARTICLES && !followingArticles.isNullOrEmpty()
                ) {
                    otherArticles.addAll(
                        followingArticles.take(
                            NUMBER_OF_OTHER_ARTICLES - otherArticles.size
                        )
                    )
                }

                // If not refreshing, must initialize otherRV.
                if (!isRefreshing) {
                    otherRV = binding.rvOtherArticles
                    otherRV.layoutManager = LinearLayoutManager(context)
                    otherRV.adapter = HomeArticlesAdapter(
                        otherArticles.shuffled()
                                as MutableList<Article>
                    )
                } else {
                    // otherRV is already created if initialized, only need to repopulate adapter data.
                    val adapter = otherRV.adapter as HomeArticlesAdapter
                    adapter.clear()
                    adapter.addAll(otherArticles.shuffled())
                }
            })
        }
    }

    private fun retrieveOtherArticlesFromResponse(
        response: Response<ArticlesByPublicationIDsQuery.Data>?,
        otherArticles: MutableList<Article>
    ) {
        response?.data?.getArticlesByPublicationIDs?.mapTo(otherArticles, { article ->
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
                    socials = publication.socials.toList()
                        .map { Social(it.social, it.uRL) }),
                shoutouts = article.shoutouts,
                nsfw = article.nsfw
            )
        })
    }

    private fun handleFollowingObservable(
        followingObs: Observable<Response<ArticlesByPublicationIDsQuery.Data>>?,
        isRefreshing: Boolean,
        followingArticles: MutableList<Article>,
        trendingArticlesId: HashSet<String>
    ) {
        if (followingObs != null) {
            disposables.add(followingObs.subscribe { response ->
                retrieveFollowingArticlesFromResponse(response, followingArticles)

                // Filters any articles the user follows that are trending.
                followingArticles.removeAll { article ->
                    trendingArticlesId.contains(article.id)
                }

                if (followingArticles.isNotEmpty()) {
                    Article.sortByDate(followingArticles)

                    // If not refreshing, must initialize followingRV.
                    if (!isRefreshing) {
                        followingRV = binding.rvFollowing
                        followingRV.layoutManager = LinearLayoutManager(context)
                        followingRV.adapter = HomeArticlesAdapter(
                            followingArticles.take(NUMBER_OF_FOLLOWING_ARTICLES)
                                    as MutableList<Article>
                        )
                    } else {
                        // followingRV is already created if initialized, only need to repopulate adapter data.
                        val adapter = followingRV.adapter as HomeArticlesAdapter
                        adapter.clear()
                        adapter.addAll(followingArticles.take(NUMBER_OF_FOLLOWING_ARTICLES))
                    }

                    if (followingArticles.size
                        <= NUMBER_OF_FOLLOWING_ARTICLES
                    ) {
                        // There would be nothing left if we dropped twenty, so we clear.
                        followingArticles.clear()
                    } else {
                        // We took the first twenty articles, the remaining ones (after removing them)
                        // can be used for the other article section
                        followingArticles.removeAll(
                            followingArticles.take(
                                NUMBER_OF_FOLLOWING_ARTICLES
                            )
                        )
                    }
                }
            })
        }
    }

    /**
     * Parses the raw articles from our ArticlesByPublicationIDs query, turning them into our Article
     * model, adding said articles to followingArticles.
     */
    private fun retrieveFollowingArticlesFromResponse(
        response: Response<ArticlesByPublicationIDsQuery.Data>?,
        followingArticles: MutableList<Article>,
    ) {
        response?.data?.getArticlesByPublicationIDs?.mapTo(
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
                        socials = publication.socials.toList()
                            .map { Social(it.social, it.uRL) }),
                    shoutouts = article.shoutouts,
                    nsfw = article.nsfw
                )
            })
    }

    private fun handleTrendingObservable(
        trendingObs: Observable<Response<TrendingArticlesQuery.Data>>?,
        isRefreshing: Boolean,
        trendingArticles: MutableList<Article>,
        trendingArticlesId: HashSet<String>
    ) {
        if (trendingObs != null) {
            disposables.add(trendingObs.subscribe { response ->
                retrieveTrendingArticlesFromResponse(
                    response,
                    trendingArticles,
                    trendingArticlesId
                )

                // If not refreshing, must initialize bigRedRV.
                if (!isRefreshing) {
                    bigRedRV = binding.rvBigRead
                    bigRedRV.adapter = BigReadHomeAdapter(trendingArticles)
                    val linearLayoutManager = LinearLayoutManager(context)
                    linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
                    bigRedRV.layoutManager = linearLayoutManager
                } else {
                    // bigRedRV is already created if initialized, only need to repopulate adapter data.
                    val adapter = bigRedRV.adapter as BigReadHomeAdapter
                    adapter.clear()
                    adapter.addAll(trendingArticles)
                }
            })
        }
    }

    /**
     * Parses the raw articles from our TrendingArticles query, turning them into our Article
     * model, adding said articles to trendingArticles.
     *
     * Also adds the trending article ids to trendingArticlesId.
     */
    private fun retrieveTrendingArticlesFromResponse(
        response: Response<TrendingArticlesQuery.Data>?,
        trendingArticles: MutableList<Article>,
        trendingArticlesId: HashSet<String>
    ) {
        val rawTrendingArticles = response?.data?.getTrendingArticles
        if (rawTrendingArticles != null) {
            for (trendingArticle in rawTrendingArticles) {
                val publication = trendingArticle.publication
                trendingArticles.add(
                    Article(
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
                            socials = publication.socials.toList()
                                .map { Social(it.social, it.uRL) }),
                        shoutouts = trendingArticle.shoutouts,
                        nsfw = trendingArticle.nsfw
                    )
                )
                trendingArticlesId.add(trendingArticle.id)
            }
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
