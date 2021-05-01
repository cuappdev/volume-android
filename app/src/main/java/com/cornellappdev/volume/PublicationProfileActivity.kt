package com.cornellappdev.volume

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.cornellappdev.volume.adapters.ArticleAdapter
import com.cornellappdev.volume.analytics.EventType
import com.cornellappdev.volume.analytics.NavigationSource
import com.cornellappdev.volume.analytics.VolumeEvent
import com.cornellappdev.volume.databinding.ActivityPublicationProfileBinding
import com.cornellappdev.volume.models.Article
import com.cornellappdev.volume.models.Publication
import com.cornellappdev.volume.models.Social
import com.cornellappdev.volume.util.GraphQlUtil
import com.cornellappdev.volume.util.PrefUtils
import com.squareup.picasso.Picasso
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class PublicationProfileActivity : AppCompatActivity() {

    private lateinit var publication: Publication
    private lateinit var navigationSource: NavigationSource
    private lateinit var binding: ActivityPublicationProfileBinding
    private val disposables = CompositeDisposable()
    private val graphQlUtil = GraphQlUtil()
    private val prefUtils = PrefUtils()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPublicationProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val currentFollowingSet =
                prefUtils.getStringSet(PrefUtils.FOLLOWING_KEY, mutableSetOf())?.toMutableSet()

        publication = intent.getParcelableExtra("publication")!!
        navigationSource = intent.getParcelableExtra(NavigationSource.INTENT_KEY)!!

        getPublication(publication)

        VolumeEvent.logEvent(EventType.PUBLICATION, VolumeEvent.OPEN_PUBLICATION, navigationSource, publication.id)


        val volumeOrange = ContextCompat.getColor(this, R.color.volume_orange)
        binding.srlQuery.setColorSchemeColors(volumeOrange, volumeOrange, volumeOrange)

        binding.srlQuery.setOnRefreshListener {
            setUpArticleRV()
            binding.srlQuery.isRefreshing = false
        }

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

        binding.btnFollow.setOnClickListener {
            if (binding.btnFollow.text.equals("Following")) {
                binding.btnFollow.apply {
                    text = this@PublicationProfileActivity.getString(R.string.follow)
                    setBackgroundResource(R.drawable.rounded_rectangle_button)
                    setTextColor(ContextCompat.getColor(this.context, R.color.volume_orange))
                    currentFollowingSet.remove(publication.id)
                    prefUtils.save(PrefUtils.FOLLOWING_KEY, currentFollowingSet)
                    VolumeEvent.logEvent(EventType.PUBLICATION, VolumeEvent.UNFOLLOW_PUBLICATION, id = publication.id)
                }
            } else {
                binding.btnFollow.apply {
                    text = this@PublicationProfileActivity.getString(R.string.following)
                    setTextColor(ContextCompat.getColor(this.context, R.color.light_gray))
                    setBackgroundResource(R.drawable.rounded_rectange_button_orange)
                    currentFollowingSet.add(publication.id)
                    prefUtils.save(PrefUtils.FOLLOWING_KEY, currentFollowingSet)
                    VolumeEvent.logEvent(EventType.PUBLICATION,
                            VolumeEvent.FOLLOW_PUBLICATION,
                            NavigationSource.PUBLICATION_DETAIL,
                            publication.id)
                }
            }
        }
        setUpArticleRV()
    }

    private fun setUpArticleRV() {
        var articles = mutableListOf<Article>()
        val followingObs = graphQlUtil
                .getArticleByPublicationID(publication.id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        disposables.add(GraphQlUtil.hasInternetConnection().subscribe { hasInternet ->
            if (hasInternet) {
                disposables.add(followingObs.subscribe { response ->
                    binding.mShimmerViewContainer.stopShimmer()
                    binding.mShimmerViewContainer.visibility = View.GONE
                    response.data?.getArticlesByPublicationID?.mapTo(articles, { article ->
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
                    articles = Article.sortByDate(articles)
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

    private fun getPublication(publication: Publication) {
        var instaURL = ""
        var facebookURL = ""
        if (publication.socials != null) {
            for (social in publication.socials) {
                if (social.social == "insta") {
                    instaURL = social.URL
                } else if (social.social == "facebook") {
                    facebookURL = social.URL
                }
            }
        }
        if (publication.websiteURL.isNotBlank()) {
            binding.tvWebsiteLink.text = publication.websiteURL
            binding.clWebsiteHolder.setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(publication.websiteURL)))
            }
        } else {
            binding.clWebsiteHolder.visibility = View.GONE
        }
        if (instaURL.isNotBlank()) {
            setUpInstaOnClick(instaURL)
        } else {
            binding.clInstaHolder.visibility = View.GONE
        }
        if (facebookURL.isNotBlank()) {
            binding.clFbHolder.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(facebookURL))
                startActivity(intent)
            }
        } else {
            binding.clFbHolder.visibility = View.GONE
        }
        binding.tvName.text = publication.name
        binding.tvShoutoutCount.text =
                this.getString(R.string.shoutout_count, publication.shoutouts.toInt())
        binding.tvDescription.text = publication.bio
        Picasso.get().load(publication.backgroundImageURL).fit().centerCrop().into(binding.ivBanner)
        Picasso.get().load(publication.profileImageURL).into(binding.ivLogo)
    }

    private fun setUpInstaOnClick(url: String) {
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
        VolumeEvent.logEvent(EventType.PUBLICATION, VolumeEvent.CLOSE_PUBLICATION, id = publication.id)
    }
}
