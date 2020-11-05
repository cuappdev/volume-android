package com.example.volume_android.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.volume_android.R

class WebviewTop @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet,
        defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    private lateinit var name: TextView
    private lateinit var readingText: TextView
    private lateinit var compass: ImageView
    private lateinit var book: ImageView

    init {
        LayoutInflater.from(context).inflate(R.layout.webview_top_fragment, this, true)
        name = findViewById(R.id.webview_article_name)
        readingText = findViewById(R.id.webview_readingtext)
        compass = findViewById(R.id.webview_compass)
        book = findViewById(R.id.webview_book)

    }

    fun minimize(b: Boolean) {

        if (b) {
            readingText.visibility = View.GONE
            compass.visibility = View.GONE
            book.visibility = View.GONE


        }
        else{
            readingText.visibility = View.VISIBLE
            compass.visibility = View.VISIBLE
            book.visibility = View.VISIBLE
        }


    }
}