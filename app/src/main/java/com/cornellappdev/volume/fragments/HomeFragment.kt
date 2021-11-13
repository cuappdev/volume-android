package com.cornellappdev.volume.fragments

import com.cornellappdev.volume.MyDiffCallback
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.apollographql.apollo.api.Response
import com.cornellappdev.volume.NoInternetActivity
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
import java.util.concurrent.Executors

/**
 * Fragment for the home page, holds the Big Red Read, articles from publications users follows,
 * and articles from other publications.
 *
 *  @see {@link com.cornellappdev.volume.R.layout#fragment_home}
 */
class HomeFragment : Fragment() {

    private lateinit var bigRedRV: RecyclerView
    private lateinit var followingRV: RecyclerView
    private lateinit var otherRV: RecyclerView
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private lateinit var prefUtils: PrefUtils
    private lateinit var disposables: CompositeDisposable
    private lateinit var graphQlUtil: GraphQlUtil
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

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
        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                setupHomeFragment()
            }
        }
        disposables = CompositeDisposable()
        graphQlUtil = GraphQlUtil()
        prefUtils = PrefUtils()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupHomeFragment()
    }

    /**
     * Sets up the entirety of the home fragment, adds onClicks, checks for internet, and sets up
     * the articles.
     */
    private fun setupHomeFragment() {
        disposables.add(hasInternetConnection().subscribe { hasInternet ->
            if (!hasInternet) {
                resultLauncher.launch(Intent(context, NoInternetActivity::class.java))
            } else {
                setUpArticles(binding, isRefreshing = false)

                val volumeOrange: Int? =
                    context?.let { ContextCompat.getColor(it, R.color.volume_orange) }
                with(binding.srlQuery) {
                    if (volumeOrange != null) {
                        setColorSchemeColors(volumeOrange)
                    }

                    // Re-populates the RecyclerViews on refresh, is dependent on whether or not they are
                    // initialized.
                    setOnRefreshListener {
                        disposables.add(hasInternetConnection().subscribe { hasInternet ->
                            if (!hasInternet) {
                                startActivity(Intent(context, NoInternetActivity::class.java))
                            } else {
                                setUpArticles(
                                    binding, isRefreshing = (
                                            this@HomeFragment::bigRedRV.isInitialized &&
                                                    this@HomeFragment::followingRV.isInitialized &&
                                                    this@HomeFragment::otherRV.isInitialized)
                                )
                            }
                            // After repopulating, can stop signifying the refresh animation.
                            binding.srlQuery.isRefreshing = false
                        })
                    }
                }
            }
        })
    }

    /**
     * Sets up the home view and all three sections on the home page.
     */
    private fun setUpArticles(binding: FragmentHomeBinding, isRefreshing: Boolean) {
        val followingPublications =
            prefUtils.getStringSet(PrefUtils.FOLLOWING_KEY, mutableSetOf()).toMutableList()

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
            followingPublications.let {
                graphQlUtil.getArticlesByPublicationIDs(it)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
            }

        // Creates API call observation for retrieving all publications in the Volume database.
        val allPublicationsObs =
            graphQlUtil.getAllPublications()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())

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
            binding.rvFollowing.visibility = View.GONE
            binding.shimmerFollowing.visibility = View.VISIBLE

            // Can simply just clear adapter, since there's no following article data
            // to populate from (the user doesn't follow any publications).
            val adapter = followingRV.adapter as HomeArticlesAdapter
            val newArticles = mutableListOf<Article>()
            val result = DiffUtil.calculateDiff(MyDiffCallback(adapter.articles, newArticles))
            adapter.articles = newArticles
            result.dispatchUpdatesTo(adapter)

            binding.rvFollowing.visibility = View.VISIBLE
            binding.shimmerFollowing.visibility = View.GONE
        } else {
            // shimmer off
            binding.shimmerFollowing.visibility = View.GONE
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

        if (followingPublications.isEmpty()) {
            binding.groupFollowing.visibility = View.INVISIBLE
            binding.groupNotFollowing.visibility = View.VISIBLE
        } else {
            binding.groupFollowing.visibility = View.VISIBLE
            binding.groupNotFollowing.visibility = View.INVISIBLE
        }
    }

    /**
     * Parses the raw articles from our ArticlesByPublicationIDs query, turning them into our Article
     * model, adding said articles to the list passed in.
     */
    private fun retrieveArticlesFromResponse(
        response: Response<ArticlesByPublicationIDsQuery.Data>?,
        articles: MutableList<Article>,
    ) {
        response?.data?.getArticlesByPublicationIDs?.mapTo(
            articles, { article ->
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

    /**
     * Parses the raw articles from our TrendingArticles query, turning them into our Article
     * model, adding said articles to trendingArticles.
     *
     * Also adds the trending article ids to trendingArticlesId.
     */
    private fun retrieveTrendingArticlesFromResponse(
        response: Response<TrendingArticlesQuery.Data>,
        trendingArticles: MutableList<Article>,
        trendingArticlesId: HashSet<String>
    ) {
        val rawTrendingArticles = response.data?.getTrendingArticles
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

    /**
     * Retrieves the articles that are trending on Volume and initializes/refreshes the Big Red Read
     * RecyclerView.
     */
    private fun handleTrendingObservable(
        trendingObs: Observable<Response<TrendingArticlesQuery.Data>>,
        isRefreshing: Boolean,
        trendingArticles: MutableList<Article>,
        trendingArticlesId: HashSet<String>
    ) {
        disposables.add(trendingObs.subscribe { response ->
            retrieveTrendingArticlesFromResponse(
                response,
                trendingArticles,
                trendingArticlesId
            )

            // If not refreshing, must initialize bigRedRV.
            if (!isRefreshing) {
                bigRedRV = binding.rvBigRead
                with(bigRedRV) {
                    adapter = BigReadHomeAdapter(trendingArticles)
                    layoutManager = LinearLayoutManager(context)
                    (layoutManager as LinearLayoutManager).orientation =
                        LinearLayoutManager.HORIZONTAL
                }
            } else {
                binding.rvBigRead.visibility = View.GONE
                binding.shimmerBigRead.visibility = View.VISIBLE

                // bigRedRV is already created if initialized, only need to repopulate adapter data.
                val adapter = bigRedRV.adapter as HomeArticlesAdapter
                val result = DiffUtil.calculateDiff(MyDiffCallback(adapter.articles, trendingArticles))
                adapter.articles = trendingArticles
                result.dispatchUpdatesTo(adapter)

                binding.rvBigRead.visibility = View.VISIBLE
                binding.shimmerBigRead.visibility = View.GONE
            }

            // shimmer off
            binding.shimmerBigRead.visibility = View.GONE
            bigRedRV.visibility = View.VISIBLE
            val params = binding.ivFollowingHeader.layoutParams as ConstraintLayout.LayoutParams
            params.topToBottom = bigRedRV.id
        })
    }

    /**
     * Retrieves the articles by publications the user follows and initializes/refreshes the following reads
     * RecyclerView.
     *
     * The section is populated with the first NUMBER_OF_FOLLOWING_ARTICLES most recent following
     * articles. Following articles do not contain any of the articles from the Big Red Read.
     */
    private fun handleFollowingObservable(
        followingObs: Observable<Response<ArticlesByPublicationIDsQuery.Data>>?,
        isRefreshing: Boolean,
        followingArticles: MutableList<Article>,
        trendingArticlesId: HashSet<String>
    ) {
        if (followingObs != null) {
            disposables.add(followingObs.subscribe { response ->
                retrieveArticlesFromResponse(response, followingArticles)

                // Filters any articles the user follows that are trending.
                followingArticles.removeAll { article ->
                    trendingArticlesId.contains(article.id)
                }

                if (followingArticles.isNotEmpty()) {
                    Article.sortByDate(followingArticles)

                    // If not refreshing, must initialize followingRV.
                    if (!isRefreshing) {
                        followingRV = binding.rvFollowing
                        with(followingRV) {
                            adapter = HomeArticlesAdapter(
                                followingArticles.take(NUMBER_OF_FOLLOWING_ARTICLES)
                                        as MutableList<Article>
                            )
                            layoutManager = LinearLayoutManager(context)
                        }
                    } else {
                        binding.rvFollowing.visibility = View.VISIBLE
                        binding.shimmerFollowing.visibility = View.GONE

                        // followingRV is already created if initialized, only need to repopulate adapter data.
                        val adapter = followingRV.adapter as HomeArticlesAdapter
                        val newArticles = followingArticles.take(NUMBER_OF_FOLLOWING_ARTICLES) as MutableList<Article>
                        val result = DiffUtil.calculateDiff(MyDiffCallback(adapter.articles, newArticles))
                        adapter.articles = newArticles
                        result.dispatchUpdatesTo(adapter)

                        binding.rvFollowing.visibility = View.VISIBLE
                        binding.shimmerFollowing.visibility = View.GONE
                    }

                    if (followingArticles.size
                        <= NUMBER_OF_FOLLOWING_ARTICLES
                    ) {
                        // There would be nothing left if we dropped twenty, so we clear.
                        followingArticles.clear()
                    } else {
                        // We took the first twenty articles, the remaining ones (after removing them)
                        // can be used for the other article section (see the description of handleOtherObservable).
                        followingArticles.removeAll(
                            followingArticles.take(
                                NUMBER_OF_FOLLOWING_ARTICLES
                            )
                        )
                    }
                }
                binding.shimmerFollowing.visibility = View.GONE
                followingRV.visibility = View.VISIBLE
                val params =
                    binding.volumeLogoMoreArticles.layoutParams as ConstraintLayout.LayoutParams
                params.topToBottom = followingRV.id
            })
        }
    }

    /**
     * Populates the section that contains articles from other publications.
     */
    private fun handleOtherSection(
        allPublicationsObs: Observable<Response<AllPublicationsQuery.Data>>,
        isRefreshing: Boolean,
        followingPublications: MutableList<String>?,
        followingArticles: MutableList<Article>,
        allPublicationIdsExcludingFollowing: MutableList<String>,
        otherArticles: MutableList<Article>,
        trendingArticlesId: HashSet<String>
    ) {
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
                .getArticlesByPublicationIDs(allPublicationIdsExcludingFollowing)
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

    /**
     * Retrieves the articles used for the other reads section and initializes/refreshes the other reads
     * RecyclerView.
     *
     * The section is first populated with articles that aren't trending or from publications that
     * users follow and then pulls from excess articles from pubs the user follows if there
     * isn't enough to populate other reads.
     */
    private fun handleOtherObservable(
        otherObs: Observable<Response<ArticlesByPublicationIDsQuery.Data>>?,
        isRefreshing: Boolean,
        followingArticles: MutableList<Article>,
        otherArticles: MutableList<Article>,
        trendingArticlesId: HashSet<String>
    ) {
        if (otherObs != null) {
            disposables.add(otherObs.subscribe { response ->
                retrieveArticlesFromResponse(response, otherArticles)

                // Removes trending articles.
                otherArticles.removeAll { article ->
                    trendingArticlesId.contains(article.id)
                }

                // Adds excess followingArticles to otherArticles if below threshold.
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
                    with(otherRV) {
                        adapter = HomeArticlesAdapter(
                            otherArticles.shuffled().take(NUMBER_OF_OTHER_ARTICLES)
                                    as MutableList<Article>, true
                        )
                        layoutManager = LinearLayoutManager(context)
                    }
                } else {
                    binding.rvOtherArticles.visibility = View.VISIBLE
                    binding.shimmerOtherArticles.visibility = View.GONE

                    // otherRV is already created if initialized, only need to repopulate adapter data.
                    val adapter = otherRV.adapter as HomeArticlesAdapter
                    val newArticles = otherArticles.shuffled().take(NUMBER_OF_OTHER_ARTICLES) as MutableList<Article>
                    val result = DiffUtil.calculateDiff(MyDiffCallback(adapter.articles, newArticles))
                    adapter.articles = newArticles
                    result.dispatchUpdatesTo(adapter)

                    binding.rvOtherArticles.visibility = View.VISIBLE
                    binding.shimmerBigRead.visibility = View.GONE
                }

                // shimmer off
                binding.shimmerOtherArticles.visibility = View.GONE
                otherRV.visibility = View.VISIBLE
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
