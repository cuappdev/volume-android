package com.cornellappdev.volume.views

import PrefUtils
import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import androidx.constraintlayout.widget.ConstraintLayout
import com.cornellappdev.volume.PublicationProfileActivity
import com.cornellappdev.volume.R
import com.cornellappdev.volume.databinding.LayoutWebviewBottomBinding
import com.cornellappdev.volume.models.Article
import com.cornellappdev.volume.util.GraphQlUtil
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
            prefUtils.getStringSet("savedArticles", mutableSetOf())?.toMutableSet()

    private val binding: LayoutWebviewBottomBinding =
            LayoutWebviewBottomBinding.bind(
                    LayoutInflater
                            .from(context)
                            .inflate(
                                    R.layout.layout_webview_bottom,
                                    this,
                                    true))
//    private val binding: LayoutWebviewBottomBinding =
//            LayoutWebviewBottomBinding.inflate(
//                    LayoutInflater.from(context),
//                    this,
//                    true)

    companion object {
        private const val MAX_SHOUTOUTS = 5
    }

    fun setUpView() {
        if (!article.publication?.profileImageURL.isNullOrBlank()) {
            Picasso.get().load(article.publication?.profileImageURL).into(binding.ivPublicationLogo)
        }
        val articleFreshObs =
                article.id?.let {
                    graphQlUtil
                            .getArticleByID(it)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                }
        if (articleFreshObs != null) {
            disposables.add(articleFreshObs.subscribe { response ->
                binding.tvShoutoutCount.text = response.data?.getArticleByID?.shoutouts?.toInt().toString()
            })
        }
        binding.tvShoutoutCount.text = article.shoutouts?.toInt().toString()
        if (currentBookmarks != null) {
            if (currentBookmarks.contains(article.id)) {
                binding.ivBookmarkIcon.setImageResource(R.drawable.orange_shoutout_svg)
            } else {
                binding.ivBookmarkIcon.setImageResource(R.drawable.ic_black_bookmarksvg)
            }
            prefUtils.save("savedArticles", currentBookmarks)
        }
        binding.ivBookmarkIcon.setOnClickListener { bookmarkArticle() }
        binding.btnSeeMore.setOnClickListener { publicationIntent() }
        binding.ivShare.setOnClickListener { shareArticle() }
        article.id?.let {
            if (prefUtils.getInt(it, 0) >= MAX_SHOUTOUTS) {
                binding.ivShoutout.setImageResource(R.drawable.filled_shoutout)
            } else {
                binding.ivShoutout.setOnClickListener { likeArticle() }
            }
        }
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
        intent.putExtra("publication", article.publication)
        context?.startActivity(intent)
    }

    private fun bookmarkArticle() {
        if (currentBookmarks != null) {
            if (!currentBookmarks.contains(article.id)) {
                article.id?.let { currentBookmarks.add(it) }
                binding.ivBookmarkIcon.startAnimation(AnimationUtils.loadAnimation(context, R.anim.shake))
                binding.ivBookmarkIcon.setImageResource(R.drawable.orange_shoutout_svg)
            } else {
                currentBookmarks.remove(article.id)
                binding.ivBookmarkIcon.startAnimation(AnimationUtils.loadAnimation(context, R.anim.shake))
                binding.ivBookmarkIcon.setImageResource(R.drawable.ic_black_bookmarksvg)
            }
            prefUtils.save("savedArticles", currentBookmarks)
        }
    }

    private fun shareArticle() {
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
        this.article.id?.let {
            var numOfShoutouts = prefUtils.getInt(it, 0)
            if (numOfShoutouts < MAX_SHOUTOUTS) {
                binding.ivShoutout.startAnimation(AnimationUtils.loadAnimation(context, R.anim.shake))
                val likeObs = graphQlUtil
                        .likeArticle(it)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                disposables.add(likeObs.subscribe { response ->
                    binding.tvShoutoutCount.text = response.data!!.incrementShoutouts.shoutouts.toInt().toString()
                })
                numOfShoutouts++
                prefUtils.save(it, numOfShoutouts)
            }
            if (numOfShoutouts >= MAX_SHOUTOUTS) {
                binding.ivShoutout.setImageResource(R.drawable.filled_shoutout)
            }
        }
    }
}