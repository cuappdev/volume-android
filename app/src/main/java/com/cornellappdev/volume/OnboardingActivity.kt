package com.cornellappdev.volume

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.apollographql.apollo.api.Response
import com.cornellappdev.volume.adapters.OnboardingPagerAdapter
import com.cornellappdev.volume.analytics.EventType
import com.cornellappdev.volume.analytics.VolumeEvent
import com.cornellappdev.volume.databinding.ActivityOnboardingBinding
import com.cornellappdev.volume.fragments.OnboardingFragTwo
import com.cornellappdev.volume.models.Article
import com.cornellappdev.volume.models.Publication
import com.cornellappdev.volume.models.Social
import com.cornellappdev.volume.models.WeeklyDebrief
import com.cornellappdev.volume.util.ActivityForResultConstants
import com.cornellappdev.volume.util.GraphQlUtil
import com.cornellappdev.volume.util.GraphQlUtil.Companion.hasInternetConnection
import com.cornellappdev.volume.util.NotificationService
import com.cornellappdev.volume.util.PrefUtils
import com.kotlin.graphql.ArticleByIDQuery
import com.kotlin.graphql.GetUserQuery
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

/**
 * This activity is responsible for Onboarding, what the users first see when they install the app.
 *
 * @see {@link com.cornellappdev.volume.R.layout#activity_onboarding}
 * @see {@link OnboardingFragOne}
 * @see {@link OnboardingFragTwo}
 */
class OnboardingActivity : AppCompatActivity(), OnboardingFragTwo.DataPassListener {

    companion object {
        private const val TAG = "ONBOARDING_ACTIVITY"
        private const val FRAGMENT_COUNT = 2
        private const val SLIDE_UP_DURATION_MS = 1000L
        private const val VOLUME_LOGO_FADE_AWAY_MS = 3000L
        private const val VOLUME_LOGO_MARGIN_TOP = 100
    }

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private lateinit var prefUtils: PrefUtils
    private lateinit var disposables: CompositeDisposable
    private lateinit var graphQlUtil: GraphQlUtil
    private var isOnboarding = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefUtils = PrefUtils(this)
        disposables = CompositeDisposable()
        graphQlUtil = GraphQlUtil()

        resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == ActivityForResultConstants.FROM_NO_INTERNET.code && !isOnboarding || result.resultCode == ActivityForResultConstants.FROM_MAIN_ACTIVITY.code) {
                    initializeOnboarding()
                } else if (result.resultCode == ActivityForResultConstants.FROM_PUBLICATION_PROFILE_ACTIVITY.code) {
                    val followingPublications =
                        prefUtils.getStringSet(PrefUtils.FOLLOWING_KEY, mutableSetOf())

                    // When the user returns to the OnboardingActivity, e.g., in the case that they click
                    // on a publication, we must check to see if the following list is non-empty as there's no
                    // callback from other activities to check for a new follow.
                    if (followingPublications.isNotEmpty()) {
                        binding.btnNext.isClickable = true
                        binding.btnNext.setTextColor(
                            ContextCompat.getColor(
                                this@OnboardingActivity, R.color.volume_orange
                            )
                        )
                    }
                }
            }

        val extras = intent.extras
        if (extras != null) {
            Log.d(TAG, "Contains extras")
            Log.d(
                TAG,
                extras[NotificationService.NotificationDataKeys.NOTIFICATION_TYPE.key] as String
            )
            when (extras[NotificationService.NotificationDataKeys.NOTIFICATION_TYPE.key]) {
                NotificationService.NotificationType.NEW_ARTICLE.type -> {
                    Log.d(TAG, "New Article")
                    val articleID = extras[NotificationService.NotificationDataKeys.ARTICLE_ID.key]
                    val getArticleObs =
                        graphQlUtil.getArticleByID(articleID as String)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                    launchArticleView(getArticleObs)
                }
                NotificationService.NotificationType.WEEKLY_DEBRIEF.type -> {
                    // Get new weekly debrief
                    val uuid = prefUtils.getString(PrefUtils.UUID, null)
                    if (uuid != null) {
                        val getWeeklyDebrief =
                            graphQlUtil.getUser(uuid)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                        processWeeklyDebrief(getWeeklyDebrief)
                    } else {
                        initializeOnboarding()
                    }
                }
            }
        } else {
            initializeOnboarding()
        }
    }

    private fun processWeeklyDebrief(weeklyDebriefObs: Observable<Response<GetUserQuery.Data>>) {
        disposables.add(weeklyDebriefObs.subscribe { response ->
            val rawWeeklyDebrief = response?.data?.getUser?.weeklyDebrief
            val randomArticles = mutableListOf<Article>()
            val readArticles = mutableListOf<Article>()

            if (rawWeeklyDebrief != null) {
                rawWeeklyDebrief.randomArticles.mapTo(
                    randomArticles, { article ->
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
                rawWeeklyDebrief.readArticles.mapTo(
                    readArticles, { article ->
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

                val weeklyDebrief = WeeklyDebrief(
                    rawWeeklyDebrief.createdAt.time,
                    rawWeeklyDebrief.expirationDate.time,
                    rawWeeklyDebrief.numShoutouts,
                    rawWeeklyDebrief.numBookmarkedArticles,
                    rawWeeklyDebrief.numReadArticles,
                    readArticles,
                    randomArticles
                )

                prefUtils.save(PrefUtils.CACHED_DEBRIEF, weeklyDebrief)
                val intent = Intent(this, TabActivity::class.java)
                intent.putExtra(WeeklyDebrief.INTENT_KEY, true)
                this.startActivity(intent)

                // It's important that this activity is closed, so the user can't accidentally
                // swipe back to this activity.
                finish()
            }
        })
    }

    private fun launchArticleView(articleObs: Observable<Response<ArticleByIDQuery.Data>>) {
        disposables.add(articleObs.subscribe { response ->
            val rawArticles = response?.data?.getArticleByID!!
            val publication = rawArticles.publication
            val article = Article(
                title = rawArticles.title,
                articleURL = rawArticles.articleURL,
                date = rawArticles.date.toString(),
                id = rawArticles.id,
                imageURL = rawArticles.imageURL,
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
                shoutouts = rawArticles.shoutouts,
                nsfw = rawArticles.nsfw
            )
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra(Article.INTENT_KEY, article)
            resultLauncher.launch(intent)
        })
    }

    private fun initializeOnboarding() {
        disposables.add(hasInternetConnection().subscribe { hasInternet ->
            if (!hasInternet) {
                resultLauncher.launch(Intent(this, NoInternetActivity::class.java))
            } else {
                // If this isn't the first launch of this app, redirects to the home page.
                val firstStart = prefUtils.getBoolean(PrefUtils.FIRST_START_KEY, true)
                if (!firstStart) {
                    val intent = Intent(this, TabActivity::class.java)
                    this.startActivity(intent)

                    // It's important that this activity is closed, so the user can't accidentally
                    // swipe back to this activity.
                    finish()
                } else {
                    isOnboarding = true
                    prefUtils.save(PrefUtils.FOLLOWING_KEY, mutableSetOf())
                    setupViewPager()
                    setupNextButton()
                    setupAnimations()
                }
            }
        })
    }

    /**
     * Sets up the main viewpager for the OnboardingActivity.
     *
     * The ViewPager includes the two main onboarding page fragments.
     */
    private fun setupViewPager() {
        VolumeEvent.logEvent(EventType.GENERAL, VolumeEvent.START_ONBOARDING)
        binding.vpOnboarding.adapter = OnboardingPagerAdapter(this, FRAGMENT_COUNT)
        binding.vpOnboarding.isUserInputEnabled = false

        // Updates UI to reflect what's on the respective page.
        binding.vpOnboarding.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> binding.btnNext.text = this@OnboardingActivity.getString(R.string.next)
                    1 -> {
                        binding.btnNext.text =
                            this@OnboardingActivity.getString(R.string.start_reading)
                        binding.btnNext.isClickable = false
                        binding.btnNext.setTextColor(
                            ContextCompat.getColor(
                                this@OnboardingActivity, R.color.gray
                            )
                        )
                    }
                }
            }
        })
    }

    /**
     * Sets up the next button.
     */
    private fun setupNextButton() {
        binding.btnNext.setOnClickListener {
            when (binding.vpOnboarding.currentItem) {
                0 -> binding.vpOnboarding.currentItem = 1
                1 -> {
                    VolumeEvent.logEvent(EventType.GENERAL, VolumeEvent.COMPLETE_ONBOARDING)
                    // On the second page, if the button is clickable then the user is able to
                    // transition to the home page.
                    val intent = Intent(this, TabActivity::class.java)
                    startActivity(intent)

                    prefUtils.save(PrefUtils.FIRST_START_KEY, false)
                    // It's important that this activity is closed, so the user can't accidentally
                    // swipe back to this activity.
                    finish()
                }
            }
        }
    }

    private fun View.setMarginTop(marginStart: Int, interpolatedTime: Float) {
        val params = layoutParams as ViewGroup.MarginLayoutParams
        val animateMargin =
            params.topMargin + ((marginStart - params.topMargin) * interpolatedTime).toInt()
        params.setMargins(
            params.leftMargin,
            animateMargin,
            params.rightMargin,
            params.bottomMargin
        )
        layoutParams = params
    }

    private fun View.fadeIn() {
        apply {
            val shortAnimationDuration =
                resources.getInteger(android.R.integer.config_shortAnimTime)

            // Set the content view to 0% opacity but visible, so that it is visible
            // (but fully transparent) during the animation.
            alpha = 0f
            visibility = View.VISIBLE

            // Animate the content view to 100% opacity, and clear any animation
            // listener set on the view.
            animate()
                .alpha(1f)
                .setDuration(shortAnimationDuration.toLong())
                .setListener(null)
        }
    }

    /**
     * Sets up the animations for the various elements in the OnboardingActivity.
     */
    private fun setupAnimations() {
        val slideUp: Animation = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                binding.ivVolumeLogo.setMarginTop(VOLUME_LOGO_MARGIN_TOP, interpolatedTime)
            }
        }

        slideUp.duration = SLIDE_UP_DURATION_MS
        slideUp.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {}

            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                binding.tvWelcome.fadeIn()
                binding.ivDivider.fadeIn()
                binding.vpOnboarding.fadeIn()
                binding.btnNext.fadeIn()
            }
        })

        // Slides the volume logo up after VOLUME_LOGO_FADE_AWAY_MS time.
        Handler(Looper.getMainLooper()).postDelayed({
            binding.ivVolumeLogo.startAnimation(slideUp)
        }, VOLUME_LOGO_FADE_AWAY_MS)
    }

    /**
     * Detects changes in the following list through a callback with {@link OnboardingFragTwo}.
     *
     * A user cannot precede to the home page until they follow someone,
     * which this function maintains.
     */
    override fun onPublicationFollowed(numFollowed: Int) {
        if (this::binding.isInitialized) {
            binding.btnNext.isClickable = numFollowed > 0
            binding.btnNext.setTextColor(
                if (numFollowed > 0) {
                    ContextCompat.getColor(
                        this@OnboardingActivity, R.color.volume_orange
                    )
                } else {
                    ContextCompat.getColor(
                        this@OnboardingActivity, R.color.gray
                    )
                }
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }
}
