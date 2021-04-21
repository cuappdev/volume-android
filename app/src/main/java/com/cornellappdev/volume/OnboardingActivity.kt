package com.cornellappdev.volume

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.cornellappdev.volume.adapters.OnboardingPagerAdapter
import com.cornellappdev.volume.analytics.EventType
import com.cornellappdev.volume.analytics.VolumeEvent
import com.cornellappdev.volume.databinding.ActivityOnboardingBinding
import com.cornellappdev.volume.fragments.OnboardingFragTwo
import com.cornellappdev.volume.util.PrefUtils


class OnboardingActivity : AppCompatActivity(), OnboardingFragTwo.DataPassListener {

    companion object {
        private const val FRAGMENT_COUNT = 2
        private const val SLIDE_UP_DURATION_MS: Long = 1000
        private const val VOLUME_LOGO_FADE_AWAY_MS: Long = 3000
        private const val VOLUME_LOGO_MARGIN_TOP = 100
    }

    private lateinit var prefUtils: PrefUtils
    private lateinit var binding: ActivityOnboardingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefUtils = PrefUtils(this)
        val dropperSave = prefUtils.getBoolean("dropper_save", true)
        if (dropperSave) {
            prefUtils.remove(PrefUtils.FOLLOWING_KEY)
            prefUtils.save("dropper_save", false)
        }

        val firstStart = prefUtils.getBoolean(PrefUtils.FIRST_START_KEY, true)
        if (!firstStart) {
            val intent = Intent(this, TabActivity::class.java)
            this.startActivity(intent)
            finish()
        }

        VolumeEvent.logEvent(EventType.GENERAL, VolumeEvent.START_ONBOARDING)

        binding.vpOnboarding.adapter = OnboardingPagerAdapter(this, FRAGMENT_COUNT)

        binding.vpOnboarding.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> binding.btnNext.text = this@OnboardingActivity.getString(R.string.next)
                    1 -> {
                        binding.btnNext.text = this@OnboardingActivity.getString(R.string.start_reading)
                        binding.btnNext.isClickable = false
                        binding.btnNext.setTextColor(ContextCompat.getColor(
                                this@OnboardingActivity, R.color.gray))
                    }
                }
            }
        })

        binding.btnNext.setOnClickListener {
            val current = binding.vpOnboarding.currentItem
            val context = it.context

            when (current) {
                0 -> binding.vpOnboarding.currentItem = 1
                1 -> {
                    VolumeEvent.logEvent(EventType.GENERAL, VolumeEvent.COMPLETE_ONBOARDING)
                    val intent = Intent(context, TabActivity::class.java)
                    context.startActivity(intent)
                    finish()
                }
            }
        }

        fun View.setMarginTop(marginStart: Int, interpolatedTime: Float) {
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

        val shortAnimationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)

        fun View.fadeIn() {
            apply {
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

        val slideUp: Animation = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                binding.ivVolumeLogo.setMarginTop(VOLUME_LOGO_MARGIN_TOP, interpolatedTime)
            }
        }
        slideUp.duration = SLIDE_UP_DURATION_MS
        slideUp.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                binding.tvWelcome.fadeIn()
                binding.ivDivider.fadeIn()
                binding.vpOnboarding.fadeIn()
                binding.btnNext.fadeIn()
            }

            override fun onAnimationStart(animation: Animation?) {
            }
        })

        Handler(Looper.getMainLooper()).postDelayed({
            binding.ivVolumeLogo.startAnimation(slideUp)
        }, VOLUME_LOGO_FADE_AWAY_MS)

        prefUtils.save(PrefUtils.FIRST_START_KEY, false)
    }

    override fun onPublicationFollowed(numFollowed: Int) {
        if (this::binding.isInitialized) {
            binding.btnNext.isClickable = numFollowed > 0
            binding.btnNext.setTextColor(if (numFollowed > 0) {
                ContextCompat.getColor(
                        this@OnboardingActivity, R.color.volumeOrange)
            } else {
                ContextCompat.getColor(
                        this@OnboardingActivity, R.color.gray)
            })
        }
    }
}