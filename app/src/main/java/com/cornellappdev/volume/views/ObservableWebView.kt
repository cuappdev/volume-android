package com.cornellappdev.volume.views

import android.content.Context
import android.util.AttributeSet
import android.webkit.WebView

class ObservableWebView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet,
        defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr) {

    private lateinit var top: WebviewTop
    private lateinit var bot: WebviewBottom

    fun setWebViews(top: WebviewTop, bot: WebviewBottom) {
        this.top = top
        this.bot = bot
    }

    /**
     * Minimizes either the top or bottom WebView given the direction the user is scrolling in.
     */
    override fun onScrollChanged(scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int) {
        super.onScrollChanged(scrollX, scrollY, oldScrollX, oldScrollY)
        when {
            scrollY > oldScrollY -> {
                top.minimize(true)
                bot.minimize(true)
            }
            scrollY < oldScrollY -> {
                top.minimize(false)
                bot.minimize(false)
            }
        }
    }
}

