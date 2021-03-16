package com.cornellappdev.volume.views

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.startActivity
import com.cornellappdev.volume.R
import com.cornellappdev.volume.models.Article


class WebviewTop @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet,
        defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {
    private lateinit var name: TextView
    private lateinit var readingText: TextView
    private lateinit var compass: ImageView
    private lateinit var book: ImageView
    private lateinit var article: Article

    init {
        LayoutInflater.from(context).inflate(R.layout.webview_top_fragment, this, true)
        name = findViewById(R.id.webview_article_name)
        readingText = findViewById(R.id.webview_readingtext)
        compass = findViewById(R.id.webview_compass)
    }

    fun setName(article: Article) {
        name.text = article.title
    }

    fun minimize(b: Boolean) {
        if (b) {
            val param = name.layoutParams as ViewGroup.MarginLayoutParams
            param.setMargins(0, 20, 0, 10)
            name.layoutParams = param
            readingText.visibility = View.GONE
            compass.visibility = View.INVISIBLE
        } else {
            val param = name.layoutParams as ViewGroup.MarginLayoutParams
            param.setMargins(0, 20, 0, 0)
            name.layoutParams = param
            readingText.visibility = View.VISIBLE
            compass.visibility = View.VISIBLE
        }
    }
}