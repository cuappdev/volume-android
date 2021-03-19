package com.cornellappdev.volume

import PrefUtils
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.cornellappdev.volume.adapters.OnboardingPagerAdapter
import com.cornellappdev.volume.databinding.ActivityOnboardingBinding


class OnboardingActivity : AppCompatActivity() {

    companion object {
        private const val FRAGMENT_COUNT = 2
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
            prefUtils.remove("following")
            prefUtils.save("dropper_save", false)
        }

        val firstStart = prefUtils.getBoolean("firstStart", true)
        if (!firstStart) {
            val intent = Intent(this, TabActivity::class.java)
            this.startActivity(intent)
            finish()
        }
        
        binding.vpOnboarding.adapter = OnboardingPagerAdapter(this, FRAGMENT_COUNT)

        binding.vpOnboarding.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> binding.btnNext.text = "Next"
                    1 -> binding.btnNext.text = "Start Reading"
                }
            }
        })

        binding.btnNext.setOnClickListener {
            val current = binding.vpOnboarding.currentItem
            val context = it.context

            when (current) {
                0 -> binding.vpOnboarding.currentItem = 1
                1 -> {
                    val intent = Intent(context, TabActivity::class.java)
                    context?.startActivity(intent)
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
                val top = 100
                binding.ivVolumeLogo.setMarginTop(top, interpolatedTime)
            }
        }
        slideUp.duration = 1000 // in ms
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
        }, 3000)
        
        prefUtils.save("firstStart", false)
    }
}