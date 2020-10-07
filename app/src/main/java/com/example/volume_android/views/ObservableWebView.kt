package com.example.volume_android.views

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.webkit.WebView

class ObservableWebView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet,
        defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr) {

    override fun onScrollChanged(scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int) {
        super.onScrollChanged(scrollX, scrollY, oldScrollX, oldScrollY)
        when {
            scrollY > oldScrollY -> {Log.d("WebView", "scrollDown")}
            scrollY < oldScrollY -> {Log.d("WebView", "scrollUp")}
        }
        Log.d("WebView", "Scrolled")
    }
}

