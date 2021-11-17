package com.cornellappdev.volume

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.cornellappdev.volume.adapters.OnboardingPagerAdapter
import com.cornellappdev.volume.analytics.EventType
import com.cornellappdev.volume.analytics.VolumeEvent
import com.cornellappdev.volume.databinding.ActivityOnboardingBinding
import com.cornellappdev.volume.fragments.OnboardingFragTwo
import com.cornellappdev.volume.util.ActivityForResultConstants
import com.cornellappdev.volume.util.GraphQlUtil.Companion.hasInternetConnection
import com.cornellappdev.volume.util.PrefUtils
import io.reactivex.disposables.CompositeDisposable

/**
 * This activity is responsible for Onboarding, what the users first see when they install the app.
 *
 * @see {@link com.cornellappdev.volume.R.layout#activity_onboarding}
 * @see {@link OnboardingFragOne}
 * @see {@link OnboardingFragTwo}
 */
class OnboardingActivity : AppCompatActivity(), OnboardingFragTwo.DataPassListener {

    companion object {
        private const val FRAGMENT_COUNT = 2
        private const val SLIDE_UP_DURATION_MS = 1000L
        private const val VOLUME_LOGO_FADE_AWAY_MS = 3000L
        private const val VOLUME_LOGO_MARGIN_TOP = 100
    }

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private lateinit var prefUtils: PrefUtils
    private lateinit var disposables: CompositeDisposable
    private var isOnboarding = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefUtils = PrefUtils(this)
        disposables = CompositeDisposable()

        resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == ActivityForResultConstants.FROM_NO_INTERNET.code && !isOnboarding) {
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
        initializeOnboarding()
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
            val current = binding.vpOnboarding.currentItem
            val context = it.context

            when (current) {
                0 -> binding.vpOnboarding.currentItem = 1
                1 -> {
                    VolumeEvent.logEvent(EventType.GENERAL, VolumeEvent.COMPLETE_ONBOARDING)
                    // On the second page, if the button is clickable then the user is able to
                    // transition to the home page.
                    val intent = Intent(context, TabActivity::class.java)
                    context.startActivity(intent)
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
