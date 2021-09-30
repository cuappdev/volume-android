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
            true
        )

    companion object {
        // There's a limit imposed on how much a user can shoutout an article. Currently it is 5.
        private const val MAX_SHOUTOUTS = 5
    }

    /**
     * Sets up the WebviewBottom.
     */
    fun setUpView() {
        if (!article.publication?.profileImageURL.isNullOrBlank()) {
            Picasso.get().load(article.publication?.profileImageURL).into(binding.ivPublicationLogo)
        }

        disposables.add(GraphQlUtil.hasInternetConnection().subscribe { hasInternet ->
            article.id?.let {
                // The way shoutout count is kept track of per user is through SharedPreferences.
                if (prefUtils.getInt(it, 0) >= MAX_SHOUTOUTS) {
                    binding.ivShoutout.setImageResource(R.drawable.filled_shoutout)
                } else {
                    if (hasInternet) {
                        binding.ivShoutout.setOnClickListener { shoutoutArticle() }
                    }
                }
            }
        })

        binding.tvShoutoutCount.text = article.shoutouts?.toInt().toString()

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

    /**
     * Redirects user to learn more about the publication the user is reading about.
     */
    private fun publicationIntent() {
        val intent = Intent(context, PublicationProfileActivity::class.java)
        intent.putExtra(Publication.INTENT_KEY, article.publication)
        context?.startActivity(intent)
    }

    /**
     * Adds/removes the respective article to/from the user's bookmarks, updating UI as needed.
     */
    private fun bookmarkArticle() {
        if (currentBookmarks != null) {
            if (!currentBookmarks.contains(article.id)) {
                article.id?.let { currentBookmarks.add(it) }
                binding.ivBookmarkIcon.startAnimation(
                    AnimationUtils.loadAnimation(
                        context,
                        R.anim.shake
                    )
                )
                binding.ivBookmarkIcon.setImageResource(R.drawable.orange_shoutout_svg)
            } else {
                currentBookmarks.remove(article.id)
                binding.ivBookmarkIcon.startAnimation(
                    AnimationUtils.loadAnimation(
                        context,
                        R.anim.shake
                    )
                )
                binding.ivBookmarkIcon.setImageResource(R.drawable.ic_black_bookmarksvg)
            }
            prefUtils.save(PrefUtils.SAVED_ARTICLES_KEY, currentBookmarks)
        }
    }

    /**
     * Creates a choosing intent for users to share the articles.
     */
    private fun shareArticle() {
        binding.ivShare.startAnimation(AnimationUtils.loadAnimation(context, R.anim.shake))
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.putExtra(
            Intent.EXTRA_TEXT,
            "Look at this article I found on Volume: ${article.articleURL}"
        )
        intent.type = "text/plain"
        context.startActivity(Intent.createChooser(intent, "Share To:"))
    }

    fun setArticle(a: Article) {
        this.article = a
    }

    /**
     * Attempts to shoutout the article.
     *
     * If the user hasn't exceeded the max shoutout count,
     * increases the shoutout count for the article and updating UI as needed
     * (e.g. text, shoutout icon).
     */
    private fun shoutoutArticle() {
        this.article.id?.let {
            var numOfShoutouts = prefUtils.getInt(it, 0)
            if (numOfShoutouts < MAX_SHOUTOUTS) {
                binding.ivShoutout.startAnimation(
                    AnimationUtils.loadAnimation(
                        context,
                        R.anim.shake
                    )
                )
                val likeObs = graphQlUtil
                    .likeArticle(it)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                disposables.add(likeObs.subscribe { response ->
                    binding.tvShoutoutCount.text =
                        response.data!!.incrementShoutouts.shoutouts.toInt().toString()
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