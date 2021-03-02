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
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
        currentItem.let { Article.applyNSFWFilter(it, holder.articleTitle) }
        if(currentItem.imageURL != null && currentItem.imageURL != ""){
            Picasso.get().load(currentItem.imageURL).into(holder.articleImg)
        }
        val format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val datePublished = LocalDateTime.parse(currentItem.date, format)
        val dur = Duration.between(datePublished, LocalDateTime.now())
        if(dur.toDays() < 1) {
            val hours = dur.toHours()
            holder.postTime.text = "${hours} h ago"
        }
        if(dur.toDays() in 1..6) {
            holder.postTime.text = if (dur.toDays() <= 1) {
                "${dur.toDays()} day ago"
            } else {
                "${dur.toDays()} days ago"
            }
        }
        if(dur.toDays() in 7..29) {
            val weeks = dur.toDays().toInt()/7
            holder.postTime.text = if(weeks <= 1) {
                "$weeks week ago"
            } else {
                "$weeks weeks ago"
            }
        }
        if(dur.toDays() in 30..364) {
            val months = dur.toDays()/30
            holder.postTime.text = if (months <= 1) {
                "$months month ago"
            } else {
                "$months months ago"
            }
        }
        if (dur.toDays() >= 365) {
            val years = dur.toDays()/365
            holder.postTime.text = if(years <= 1) {
                "$years year ago"
            } else {
                "$years years ago"
            }
        }
        holder.shoutoutCount.text = currentItem.shoutouts?.toInt().toString() + " shout-outs"
        holder.layout.setOnClickListener{
            val intent = Intent(holder.layout.context, MainActivity::class.java)
            intent.putExtra("article",currentItem)
            holder.layout.context?.startActivity(intent)
        }
    }
}