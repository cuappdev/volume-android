package com.example.volume_android.views

import PrefUtils
import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.volume_android.PublicationProfileActivity
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
    private var prefUtils = PrefUtils()
    private lateinit var article: Article
    val currentBookmarks = prefUtils.getStringSet("savedArticles", mutableSetOf())?.toMutableSet()

    init {
        LayoutInflater.from(context).inflate(R.layout.bottom_webview_actions, this, true)
        profileImageView = findViewById(R.id.placeholder_org_profile)
        seeMoreButton = findViewById(R.id.see_more_button)
        bookMark = findViewById(R.id.bookmark)
        shareContent = findViewById(R.id.share_content)
        shoutOuts = findViewById(R.id.heart)
        shoutOutsNum = findViewById(R.id.like_count)
        prefUtils = PrefUtils(context)
    }

    fun setUpView(){
        if(article.publication?.profileImageURL != null && article.publication?.profileImageURL != ""){
            Picasso.get().load(article.publication?.profileImageURL).into(profileImageView)
        }
        shoutOutsNum.text = article.shoutouts?.toInt().toString()

        if (currentBookmarks != null) {
            if(currentBookmarks.contains(article.id)){
                bookMark.setImageResource(R.drawable.ic_orange_bookmarksvg)
            }
            else{
                bookMark.setImageResource(R.drawable.ic_black_bookmarksvg)
            }
            prefUtils.save("savedArticles", currentBookmarks)
        }
        bookMark.setOnClickListener{bookmarkArticle()}
        seeMoreButton.setOnClickListener {publicationIntent()}
    }

    fun minimize(b: Boolean) {

        if (b) {
            this.visibility = View.GONE
        } else {
            this.visibility = View.VISIBLE
        }
    }

    fun publicationIntent(){
        val intent = Intent(context, PublicationProfileActivity::class.java)
        intent.putExtra("publication", article.publication)
        context?.startActivity(intent)
    }

    fun bookmarkArticle(){
        if (currentBookmarks != null) {
            if(!currentBookmarks.contains(article.id)){
                article.id?.let { currentBookmarks.add(it) }
                bookMark.startAnimation(AnimationUtils.loadAnimation(context ,R.anim.shake));
                bookMark.setImageResource(R.drawable.ic_orange_bookmarksvg)
            }
            else{
                currentBookmarks.remove(article.id)
                bookMark.startAnimation(AnimationUtils.loadAnimation(context ,R.anim.shake));
                bookMark.setImageResource(R.drawable.ic_black_bookmarksvg)
            }
            prefUtils.save("savedArticles", currentBookmarks)
        }
        Log.d("WebviewBottom", "Article Pressed")
    }

    fun setArticle(a: Article){
        this.article = a
    }
}