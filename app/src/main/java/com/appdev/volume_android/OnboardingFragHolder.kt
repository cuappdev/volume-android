package com.appdev.volume_android

import PrefUtils
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager.widget.ViewPager
import com.appdev.volume_android.adapters.OnboardingPageAdapter
import com.appdev.volume_android.fragments.HomeFragment
import com.appdev.volume_android.models.Publication


class OnboardingFragHolder : AppCompatActivity() {

    private lateinit var viewPager: ViewPager
    private lateinit var volLogo: ImageView
    private lateinit var volMsg: TextView
    private lateinit var divider: ImageView
    private lateinit var vPager: ViewPager
    private lateinit var nextButton: Button
    private lateinit var holderLayout: ConstraintLayout
    private lateinit var prefUtils: PrefUtils



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.onboarding_holder)

        prefUtils = PrefUtils(this)
        val firstStart = prefUtils.getBoolean("firstStart", true)

        if(!firstStart){
            val intent = Intent(this, TabbedActivity::class.java)
            this?.startActivity(intent)
        }


        var initialClick = true
        Log.d("Test",initialClick.toString())

        viewPager = findViewById(R.id.onboarding_pageviewer)
        volLogo = findViewById(R.id.vol_logo)
        volMsg = findViewById(R.id.onboarding_msg)
        divider = findViewById(R.id.onboarding_divider)
        vPager = findViewById(R.id.onboarding_pageviewer)
        nextButton = findViewById(R.id.onboarding_button)
        holderLayout = findViewById(R.id.holder_layout)


        //TODO: This is fake data for onboarding
        val onboardingdata : ArrayList<Publication>  = ArrayList()
        onboardingdata.add(Publication("We are Creme de Cornell", "1", "bio", "Creme de Cornell", "image", "rssName", "rssURL", "website"))
        onboardingdata.add(Publication("We are Creme de Cornell", "1", "bio", "Creme de Cornell", "image", "rssName", "rssURL", "website"))
        onboardingdata.add(Publication("We are Creme de Cornell", "1", "bio", "Creme de Cornell", "image", "rssName", "rssURL", "website"))
        onboardingdata.add(Publication("We are Creme de Cornell", "1", "bio", "Creme de Cornell", "image", "rssName", "rssURL", "website"))
        onboardingdata.add(Publication("We are Creme de Cornell", "1", "bio", "Creme de Cornell", "image", "rssName", "rssURL", "website"))
        onboardingdata.add(Publication("We are Creme de Cornell", "1", "bio", "Creme de Cornell", "image", "rssName", "rssURL", "website"))

        val fragmentAdapter = OnboardingPageAdapter(supportFragmentManager, onboardingdata)
        viewPager.adapter = fragmentAdapter


        viewPager?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> nextButton.setText("Next")
                    1 -> nextButton.setText("Start Reading")
                }
            }

        })

        nextButton.setOnClickListener {
            var current = viewPager.currentItem
            val context = it.context

            when(current){
                0 -> viewPager.setCurrentItem(1)
                1 -> {val intent = Intent(context, TabbedActivity::class.java)
                context?.startActivity(intent)}
            }
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

            fun View.setMarginTop(marginStart: Int, interpolatedTime: Float) {

                val params = layoutParams as ViewGroup.MarginLayoutParams
                val topMargStart = params.topMargin
                val animateMargin = topMargStart +  ((marginStart - topMargStart) * interpolatedTime).toInt()
                params.setMargins(params.leftMargin, animateMargin, params.rightMargin, params.bottomMargin)
                layoutParams = params
            }

            val slideUp: Animation = object : Animation() {
                override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                    val top = 100
                    volLogo.setMarginTop(top, interpolatedTime)
                }
            }
            slideUp.duration = 1000 // in ms
            slideUp.setAnimationListener( object: Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {
                }

                override fun onAnimationEnd(animation: Animation?) {
                    volMsg.fadeIn()
                    divider.fadeIn()
                    vPager.fadeIn()
                    nextButton.fadeIn()
                }

                override fun onAnimationStart(animation: Animation?) {
                }
            })

            holderLayout.setOnClickListener {
                if (initialClick) {
                    volLogo.startAnimation(slideUp)
                    initialClick = false
                }

            }

        prefUtils.save("firstStart", false)


    }
}