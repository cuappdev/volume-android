package com.appdev.volume_android.adapters

import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.appdev.volume_android.MainActivity
import com.appdev.volume_android.R
import com.appdev.volume_android.models.Article
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.saved_article.view.*
import kotlinx.android.synthetic.main.saved_article.view.article_img_b
import kotlinx.android.synthetic.main.saved_article.view.article_shout_count_b
import kotlinx.android.synthetic.main.saved_article.view.article_title_b
import kotlinx.android.synthetic.main.saved_article.view.desc_holder_b
import kotlinx.android.synthetic.main.saved_article.view.post_time_b
import kotlinx.android.synthetic.main.saved_article.view.pub_name_article_layout_b
import kotlinx.android.synthetic.main.saved_article.view.saved_article_layout
import kotlinx.android.synthetic.main.saved_article.view.*
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SavedArticlesAdapter(private val articles: List<Article>) :
        RecyclerView.Adapter<SavedArticlesAdapter.SavedArticleVH>() {

    class SavedArticleVH(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val pubName: TextView = itemView.pub_name_article_layout_b
        val articleTitle : TextView = itemView.article_title_b
        val articleImg : ImageView = itemView.article_img_b
        val postTime: TextView = itemView.post_time_b
        val shoutoutCount: TextView = itemView.article_shout_count_b
        val layout: ConstraintLayout = itemView.desc_holder_b
        val layoutMain: ConstraintLayout = itemView.saved_article_layout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):  SavedArticleVH {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.saved_article, parent, false)
        return SavedArticleVH (itemView)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: SavedArticleVH, position: Int) {
        val currentItem : Article = articles[position]
        holder.articleTitle.text = currentItem.title
        if(!currentItem.imageURL.isNullOrBlank()) {
            holder.articleImg.visibility = View.VISIBLE
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
        holder.pubName.text = currentItem.publication?.name

        holder.layoutMain.setOnClickListener{
            val intent = Intent(holder.layout.context, MainActivity::class.java)
            intent.putExtra("article",currentItem)
            holder.layout.context?.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return articles.size
    }
}