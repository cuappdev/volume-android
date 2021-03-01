package com.appdev.volume_android.views

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.webkit.WebView
import com.appdev.volume_android.R
import kotlinx.android.synthetic.main.activity_main.view.*

class ObservableWebView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet,
        defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr) {


    private lateinit var top: WebviewTop
    private lateinit var bot: WebviewBottom

    fun setTopBot(top1: WebviewTop, bot1: WebviewBottom) {
        top = top1
        bot = bot1
    }

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
        Log.d("WebView", "Scrolled")
    }
}

