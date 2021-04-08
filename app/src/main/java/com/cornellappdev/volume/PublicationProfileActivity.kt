package com.cornellappdev.volume

import PrefUtils
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.cornellappdev.volume.adapters.ArticleAdapter
import com.cornellappdev.volume.databinding.ActivityPublicationProfileBinding
import com.cornellappdev.volume.models.Article
import com.cornellappdev.volume.models.Publication
import com.cornellappdev.volume.models.Social
import com.cornellappdev.volume.util.GraphQlUtil
import com.squareup.picasso.Picasso
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class PublicationProfileActivity : AppCompatActivity() {

    private lateinit var publication: Publication
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
        getPublication(publication)

        val volumeOrange = ContextCompat.getColor(this, R.color.volumeOrange)
        binding.srlQuery.setColorSchemeColors(volumeOrange, volumeOrange, volumeOrange)

        binding.srlQuery.setOnRefreshListener {
            setUpArticleRV()
            binding.srlQuery.isRefreshing = false
        }

        if (currentFollowingSet!!.contains(publication.id)) {
            binding.btnFollow.apply {
                text = "Following"
                setTextColor(ContextCompat.getColor(this.context, R.color.ligthgray))
                setBackgroundResource(R.drawable.rounded_rectange_button_orange)
            }
        } else {
            binding.btnFollow.apply {
                text = " +  Follow"
                setBackgroundResource(R.drawable.rounded_rectangle_button)
            }
        }

        binding.btnFollow.setOnClickListener {
            if (binding.btnFollow.text.equals("Following")) {
                binding.btnFollow.apply {
                    text = " +  Follow"
                    setBackgroundResource(R.drawable.rounded_rectangle_button)
                    setTextColor(ContextCompat.getColor(this.context, R.color.volumeOrange))
                    currentFollowingSet.remove(publication.id)
                    prefUtils.save(PrefUtils.FOLLOWING_KEY, currentFollowingSet)
                }
            } else {
                binding.btnFollow.apply {
                    text = "Following"
                    setTextColor(ContextCompat.getColor(this.context, R.color.ligthgray))
                    setBackgroundResource(R.drawable.rounded_rectange_button_orange)
                    currentFollowingSet.add(publication.id)
                    prefUtils.save(PrefUtils.FOLLOWING_KEY, currentFollowingSet)
                }
            }
        }
        setUpArticleRV()
    }

    private fun setUpArticleRV() {
        val articles = mutableListOf<Article>()
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
        if (publication.websiteURL.isNotEmpty()) {
            binding.tvWebsiteLink.text = publication.websiteURL
            binding.clWebsiteHolder.setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(publication.websiteURL)))
            }
        } else {
            binding.clWebsiteHolder.visibility = View.GONE
        }
        if (instaURL.isNotEmpty()) {
            setUpInstaOnClick(instaURL)
        } else {
            binding.clInstaHolder.visibility = View.GONE
        }
        if (facebookURL.isNotEmpty()) {
            binding.clFbHolder.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(facebookURL))
                startActivity(intent)
            }
        } else {
            binding.clFbHolder.visibility = View.GONE
        }
        binding.tvName.text = publication.name
        binding.tvShoutoutCount.text =
                publication.shoutouts.toInt().toString() + " shout-outs"
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
}
