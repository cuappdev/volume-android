package com.example.volume_android.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.volume_android.R
import com.example.volume_android.models.Article
import com.squareup.picasso.Picasso

class WebviewBottom @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet,
        defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var profileImageView: ImageView
    private var seeMoreButton:Button
    private var bookMark:ImageView
    private var shareContent:ImageView
    private var shoutOuts:ImageView
    private var shoutOutsNum: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.bottom_webview_actions, this, true)
        profileImageView = findViewById(R.id.placeholder_org_profile)
        seeMoreButton = findViewById(R.id.see_more_button)
        bookMark = findViewById(R.id.bookmark)
        shareContent = findViewById(R.id.share_content)
        shoutOuts = findViewById(R.id.heart)
        shoutOutsNum = findViewById(R.id.like_count)
    }

    fun setUpView(article: Article){
        if(article.imageURL != null && article.imageURL != ""){
            Picasso.get().load(article.imageURL).into(profileImageView)
        }
        shoutOutsNum.text = article.shoutouts.toString()
    }

    fun minimize(b: Boolean) {

        if (b) {
            this.visibility = View.GONE
        } else {
            this.visibility = View.VISIBLE
        }
    }
}