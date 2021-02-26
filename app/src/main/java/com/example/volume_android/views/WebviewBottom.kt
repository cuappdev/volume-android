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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.volume_android.PublicationProfileActivity
import com.example.volume_android.R
import com.example.volume_android.adapters.FollowPublicationsAdapter
import com.example.volume_android.models.Article
import com.example.volume_android.models.Publication
import com.example.volume_android.util.GraphQlUtil
import com.squareup.picasso.Picasso
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

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
    private var graphQlUtil = GraphQlUtil()
    private var disposables = CompositeDisposable()

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
                bookMark.setImageResource(R.drawable.orange_shoutout_svg)
            }
            else{
                bookMark.setImageResource(R.drawable.ic_black_bookmarksvg)
            }
            prefUtils.save("savedArticles", currentBookmarks)
        }
        bookMark.setOnClickListener{bookmarkArticle()}
        seeMoreButton.setOnClickListener {publicationIntent()}
        shareContent.setOnClickListener {shareArticle()}
        shoutOuts.setOnClickListener { likeArticle() }

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
                bookMark.setImageResource(R.drawable.orange_shoutout_svg)
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

    fun shareArticle(){
        shareContent.startAnimation(AnimationUtils.loadAnimation(context ,R.anim.shake));
        val intent= Intent()
        intent.action=Intent.ACTION_SEND
        intent.putExtra(Intent.EXTRA_TEXT,"Look at this article I found on Volume: ${article.articleURL}")
        intent.type="text/plain"
        context.startActivity(Intent.createChooser(intent,"Share To:"))
    }

    fun setArticle(a: Article){
        this.article = a
    }

    fun likeArticle(){
        shoutOuts.startAnimation(AnimationUtils.loadAnimation(context ,R.anim.shake));
        val likeObs = this.article.id?.let { graphQlUtil.likeArticle(it).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()) }
        disposables.add(likeObs!!.subscribe() {
            it ->
            shoutOutsNum.text = it.data!!.incrementShoutouts.shoutouts.toInt().toString()
        })
    }
}