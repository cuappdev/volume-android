package com.cornellappdev.volume

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.apollographql.apollo.api.Response
import com.cornellappdev.volume.adapters.ArticleAdapter
import com.cornellappdev.volume.analytics.EventType
import com.cornellappdev.volume.analytics.NavigationSource
import com.cornellappdev.volume.analytics.VolumeEvent
import com.cornellappdev.volume.databinding.ActivityPublicationProfileBinding
import com.cornellappdev.volume.models.Article
import com.cornellappdev.volume.models.Publication
import com.cornellappdev.volume.models.Social
import com.cornellappdev.volume.util.ActivityForResultConstants
import com.cornellappdev.volume.util.GraphQlUtil
import com.cornellappdev.volume.util.PrefUtils
import com.kotlin.graphql.ArticlesByPublicationIDQuery
import com.squareup.picasso.Picasso
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

/**
 * This activity is responsible for showing information about a publication. Users are redirected here
 * when they click on a publication.
 *
 * @see {@link com.cornellappdev.volume.R.layout#activity_publication_profile}
 */
class PublicationProfileActivity : AppCompatActivity() {

    private lateinit var publication: Publication
    private lateinit var navigationSource: NavigationSource
    private lateinit var binding: ActivityPublicationProfileBinding
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private lateinit var prefUtils: PrefUtils
    private lateinit var disposables: CompositeDisposable
    private lateinit var graphQlUtil: GraphQlUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPublicationProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == ActivityForResultConstants.FROM_NO_INTERNET.code) {
                    initializePublicationProfileActivity()
                }
            }
        disposables = CompositeDisposable()
        graphQlUtil = GraphQlUtil()
        prefUtils = PrefUtils(this)
        initializePublicationProfileActivity()
    }

    private fun initializePublicationProfileActivity() {
        disposables.add(GraphQlUtil.hasInternetConnection().subscribe { hasInternet ->
            if (!hasInternet) {
                resultLauncher.launch(Intent(this, NoInternetActivity::class.java))
            } else {
                val currentFollowingSet =
                    prefUtils.getStringSet(PrefUtils.FOLLOWING_KEY, mutableSetOf()).toMutableSet()

                publication = intent.getParcelableExtra("publication")!!
                setupPublication(publication)
                navigationSource = intent.getParcelableExtra(NavigationSource.INTENT_KEY)!!
                VolumeEvent.logEvent(
                    EventType.PUBLICATION,
                    VolumeEvent.OPEN_PUBLICATION,
                    navigationSource,
                    publication.id
                )

                val volumeOrange = ContextCompat.getColor(this, R.color.volume_orange)
                binding.srlQuery.setColorSchemeColors(volumeOrange, volumeOrange, volumeOrange)

                // The article RecyclerView attempts to re-populate on refresh,
                // useful if user regains internet again and wants to re-query.
                binding.srlQuery.setOnRefreshListener {
                    setupArticleRV()
                    binding.srlQuery.isRefreshing = false
                }

                setupFollowButton(currentFollowingSet)
                setupArticleRV()
            }
        })
    }

    /**
     * Sets up interactions with the follow button.
     */
    private fun setupFollowButton(currentFollowingSet: MutableSet<String>?) {
        // Updates follow button given whether or not user follows publication.
        if (currentFollowingSet!!.contains(publication.id)) {
            binding.btnFollow.apply {
                text = this@PublicationProfileActivity.getString(R.string.following)
                setTextColor(ContextCompat.getColor(this.context, R.color.light_gray))
                setBackgroundResource(R.drawable.rounded_rectange_button_orange)
            }
        } else {
            binding.btnFollow.apply {
                text = this@PublicationProfileActivity.getString(R.string.follow)
                setBackgroundResource(R.drawable.rounded_rectangle_button)
            }
        }

        // Updates the SharedPreferences for who the user is following when they
        // click on the follow button.
        binding.btnFollow.setOnClickListener {
            if (binding.btnFollow.text.equals("Following")) {
                binding.btnFollow.apply {
                    // Removes and updates UI.
                    text = this@PublicationProfileActivity.getString(R.string.follow)
                    setBackgroundResource(R.drawable.rounded_rectangle_button)
                    setTextColor(
                        ContextCompat.getColor(
                            this.context,
                            R.color.volume_orange
                        )
                    )
                    currentFollowingSet.remove(publication.id)
                    prefUtils.save(PrefUtils.FOLLOWING_KEY, currentFollowingSet)
                    VolumeEvent.logEvent(
                        EventType.PUBLICATION,
                        VolumeEvent.UNFOLLOW_PUBLICATION,
                        id = publication.id
                    )
                }
            } else {
                binding.btnFollow.apply {
                    // Adds and updates UI.
                    text = this@PublicationProfileActivity.getString(R.string.following)
                    setTextColor(ContextCompat.getColor(this.context, R.color.light_gray))
                    setBackgroundResource(R.drawable.rounded_rectange_button_orange)
                    currentFollowingSet.add(publication.id)
                    prefUtils.save(PrefUtils.FOLLOWING_KEY, currentFollowingSet)
                    VolumeEvent.logEvent(
                        EventType.PUBLICATION,
                        VolumeEvent.FOLLOW_PUBLICATION,
                        NavigationSource.PUBLICATION_DETAIL,
                        publication.id
                    )
                }
            }
        }
    }

    /**
     * Sets up the RecyclerView showcasing the recent articles published by the publisher.
     */
    private fun setupArticleRV() {
        val articleByPublicationObservable = graphQlUtil
            .getArticleByPublicationID(publication.id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
        disposables.add(GraphQlUtil.hasInternetConnection().subscribe { hasInternet ->
            // Only retrieves the articles given there's internet.
            if (hasInternet) {
                disposables.add(articleByPublicationObservable.subscribe { response ->
                    binding.mShimmerViewContainer.stopShimmer()
                    binding.mShimmerViewContainer.visibility = View.GONE
                    val articles = getArticlesFromResponse(response)
                    // Initializes many of the properties of the Article RecyclerView.
                    with(binding.rvArticles) {
                        adapter = ArticleAdapter(articles)
                        visibility = View.VISIBLE
                        layoutManager = LinearLayoutManager(context)
                        isClickable = true
                        setHasFixedSize(true)
                    }
                })
            } else {
                binding.mShimmerViewContainer.startShimmer()
                binding.mShimmerViewContainer.visibility = View.VISIBLE
                binding.rvArticles.visibility = View.GONE
            }
        })
    }

    /**
     * Returns a list of all the articles specified by the response.
     *
     * Parses the raw articles from our ArticlesByPublicationID query, turning them into our Article
     * model. The articles returned are sorted descending by the date published.
     */
    private fun getArticlesFromResponse(response: Response<ArticlesByPublicationIDQuery.Data>?): MutableList<Article> {
        val articles = mutableListOf<Article>()
        response?.data?.getArticlesByPublicationID?.mapTo(articles, { article ->
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
                nsfw = article.nsfw
            )
        })
        Article.sortByDate(articles)
        return articles
    }

    /**
     * Sets up various UI on the activity with the given publication.
     */
    private fun setupPublication(publication: Publication) {
        binding.tvName.text = publication.name
        binding.tvShoutoutCount.text = this.resources.getQuantityString(
            R.plurals.shoutout_count,
            publication.shoutouts.toInt(),
            publication.shoutouts.toInt()
        )
        binding.tvDescription.text = publication.bio
        Picasso.get().load(publication.backgroundImageURL).fit().centerCrop()
            .into(binding.ivBanner)
        Picasso.get().load(publication.profileImageURL).into(binding.ivLogo)

        setupMedia(publication)
    }

    /**
     * Sets up the media links for the given publication.
     */
    private fun setupMedia(publication: Publication) {
        var hasInstaURL = false
        var hasFacebookURL = false
        if (publication.socials != null) {
            for (social in publication.socials) {
                if (social.social == "insta") {
                    hasInstaURL = true
                    setupInstaOnClick(social.URL)
                } else if (social.social == "facebook") {
                    hasFacebookURL = true
                    binding.clFbHolder.setOnClickListener {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(social.URL))
                        startActivity(intent)
                    }
                }
            }
        }

        if (!hasInstaURL) {
            binding.clInstaHolder.visibility = View.GONE
        }

        if (!hasFacebookURL) {
            binding.clFbHolder.visibility = View.GONE
        }

        if (publication.websiteURL.isNotBlank()) {
            binding.tvWebsiteLink.text = publication.websiteURL
            binding.clWebsiteHolder.setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(publication.websiteURL)))
            }
        } else {
            binding.clWebsiteHolder.visibility = View.GONE
        }
    }

    /**
     * Sets up the intent to the Instagram app for the specific profile if the user has it installed.
     *
     * Defaults to Instagram on the web.
     */
    private fun setupInstaOnClick(url: String) {
        // The redirect to instagram has a specific format which is built below.
        val inAppURL =
            StringBuilder(url).insert(url.indexOf("com") + 4, "_u/").toString()

        binding.clInstaHolder.setOnClickListener {
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

    override fun onDestroy() {
        super.onDestroy()
        VolumeEvent.logEvent(
            EventType.PUBLICATION,
            VolumeEvent.CLOSE_PUBLICATION,
            id = publication.id
        )
        disposables.clear()
    }

    override fun onBackPressed() {
        // Signals back that the user is leaving the PublicationProfileActivity
        setResult(ActivityForResultConstants.FROM_PUBLICATION_PROFILE_ACTIVITY.code)
        super.onBackPressed()
    }
}
