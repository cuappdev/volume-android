package com.cornellappdev.volume.views

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import androidx.constraintlayout.widget.ConstraintLayout
import com.cornellappdev.volume.PublicationProfileActivity
import com.cornellappdev.volume.R
import com.cornellappdev.volume.analytics.EventType
import com.cornellappdev.volume.analytics.NavigationSource
import com.cornellappdev.volume.analytics.NavigationSource.Companion.putParcelableExtra
import com.cornellappdev.volume.analytics.VolumeEvent
import com.cornellappdev.volume.databinding.LayoutWebviewBottomBinding
import com.cornellappdev.volume.models.Article
import com.cornellappdev.volume.models.Publication
import com.cornellappdev.volume.util.GraphQlUtil
import com.cornellappdev.volume.util.PrefUtils
import com.squareup.picasso.Picasso
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class WebviewBottom @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet,
        defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val prefUtils = PrefUtils(context)
    private var graphQlUtil = GraphQlUtil()
    private var disposables = CompositeDisposable()
    private lateinit var article: Article
    private val currentBookmarks =
            prefUtils.getStringSet(PrefUtils.SAVED_ARTICLES_KEY, mutableSetOf())?.toMutableSet()

    private val binding: LayoutWebviewBottomBinding =
            LayoutWebviewBottomBinding.inflate(
                    LayoutInflater.from(context),
                    this,
                    true)

    companion object {
        private const val MAX_SHOUTOUTS = 5
    }

    fun setUpView() {
        if (!article.publication?.profileImageURL.isNullOrBlank()) {
            Picasso.get().load(article.publication?.profileImageURL).into(binding.ivPublicationLogo)
        }
        disposables.add(GraphQlUtil.hasInternetConnection().subscribe { hasInternet ->
            article.id.let {
                if (prefUtils.getInt(it, 0) >= MAX_SHOUTOUTS) {
                    binding.ivShoutout.setImageResource(R.drawable.filled_shoutout)
                } else {
                    if (hasInternet) {
                        binding.ivShoutout.setOnClickListener { likeArticle() }
                    }
                }
            }
        })
        binding.tvShoutoutCount.text = article.shoutouts.toInt().toString()
        if (currentBookmarks != null) {
            if (currentBookmarks.contains(article.id)) {
                binding.ivBookmarkIcon.setImageResource(R.drawable.orange_shoutout_svg)
            } else {
                binding.ivBookmarkIcon.setImageResource(R.drawable.ic_black_bookmarksvg)
            }
            prefUtils.save(PrefUtils.SAVED_ARTICLES_KEY, currentBookmarks)
        }
        binding.ivBookmarkIcon.setOnClickListener { bookmarkArticle() }
        binding.btnSeeMore.setOnClickListener { publicationIntent() }
        binding.ivShare.setOnClickListener { shareArticle() }
    }

    fun minimize(isMinimized: Boolean) {
        if (isMinimized) {
            this.visibility = View.GONE
        } else {
            this.visibility = View.VISIBLE
        }
    }

    private fun publicationIntent() {
        val intent = Intent(context, PublicationProfileActivity::class.java)
        intent.putParcelableExtra(NavigationSource.INTENT_KEY, NavigationSource.ARTICLE_DETAIL)
        intent.putExtra(Publication.INTENT_KEY, article.publication)
        context?.startActivity(intent)
    }

    private fun bookmarkArticle() {
        if (currentBookmarks != null) {
            if (!currentBookmarks.contains(article.id)) {
                VolumeEvent.logEvent(EventType.ARTICLE, VolumeEvent.BOOKMARK_ARTICLE, id = article.id)
                currentBookmarks.add(article.id)
                binding.ivBookmarkIcon.startAnimation(AnimationUtils.loadAnimation(context, R.anim.shake))
                binding.ivBookmarkIcon.setImageResource(R.drawable.orange_shoutout_svg)
            } else {
                VolumeEvent.logEvent(EventType.ARTICLE, VolumeEvent.UNBOOKMARK_ARTICLE, id = article.id)
                currentBookmarks.remove(article.id)
                binding.ivBookmarkIcon.startAnimation(AnimationUtils.loadAnimation(context, R.anim.shake))
                binding.ivBookmarkIcon.setImageResource(R.drawable.ic_black_bookmarksvg)
            }
            prefUtils.save(PrefUtils.SAVED_ARTICLES_KEY, currentBookmarks)
        }
    }

    private fun shareArticle() {
        VolumeEvent.logEvent(EventType.ARTICLE, VolumeEvent.SHARE_ARTICLE, id = article.id)
        binding.ivShare.startAnimation(AnimationUtils.loadAnimation(context, R.anim.shake))
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.putExtra(Intent.EXTRA_TEXT,
                "Look at this article I found on Volume: ${article.articleURL}")
        intent.type = "text/plain"
        context.startActivity(Intent.createChooser(intent, "Share To:"))
    }

    fun setArticle(a: Article) {
        this.article = a
    }

    private fun likeArticle() {
        var numOfShoutouts = prefUtils.getInt(article.id, 0)
        if (numOfShoutouts < MAX_SHOUTOUTS) {
            VolumeEvent.logEvent(EventType.ARTICLE, VolumeEvent.SHOUTOUT_ARTICLE, id = article.id)
            binding.ivShoutout.startAnimation(AnimationUtils.loadAnimation(context, R.anim.shake))
            val likeObs = graphQlUtil
                    .likeArticle(article.id)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
            disposables.add(likeObs.subscribe { response ->
                binding.tvShoutoutCount.text = response.data!!.incrementShoutouts.shoutouts.toInt().toString()
            })
            numOfShoutouts++
            prefUtils.save(article.id, numOfShoutouts)
        }
        if (numOfShoutouts >= MAX_SHOUTOUTS) {
            binding.ivShoutout.setImageResource(R.drawable.filled_shoutout)
        }
    }
}