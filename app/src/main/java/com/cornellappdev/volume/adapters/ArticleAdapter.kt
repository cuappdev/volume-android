package com.cornellappdev.volume.adapters

import android.content.Intent
import android.graphics.BlurMaskFilter
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.cornellappdev.volume.MainActivity
import com.cornellappdev.volume.R
import com.cornellappdev.volume.models.Article
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.article_card.view.*

class ArticleAdapter(private val articles: List<Article>) :
        RecyclerView.Adapter<ArticleAdapter.ArticleVH>() {

    class ArticleVH(itemView : View) : RecyclerView.ViewHolder(itemView){

        val articleTitle : TextView = itemView.article_title
        val articleImg : ImageView = itemView.article_img
        val postTime: TextView = itemView.post_time
        val shoutoutCount: TextView = itemView.article_shout_count

        val layout: ConstraintLayout = itemView.article_layout

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleVH {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.article_card, parent, false)
        return ArticleVH(itemView)

    }

    override fun getItemCount(): Int {
        return articles.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ArticleVH, position: Int) {
        val currentItem : Article = articles[position]
        holder.articleTitle.text = currentItem.title
        Article.applyNSFWFilter(currentItem, holder.articleTitle)
        if(!currentItem.imageURL.isNullOrBlank()) {
            Picasso.get().load(currentItem.imageURL).into(holder.articleImg)
        }
        Article.setCorrectDateText(currentItem, holder.postTime)
        holder.shoutoutCount.text = currentItem.shoutouts?.toInt().toString() + " shout-outs"
        holder.layout.setOnClickListener{
            val intent = Intent(holder.layout.context, MainActivity::class.java)
            intent.putExtra("article",currentItem)
            holder.layout.context?.startActivity(intent)
        }
    }
}