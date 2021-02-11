package com.example.volume_android.adapters

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
import com.example.volume_android.MainActivity
import com.example.volume_android.R
import com.example.volume_android.models.Article
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.following_home_card.view.following_home_layout
import kotlinx.android.synthetic.main.home_other_articles.view.*
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class HomeOtherArticleAdapter(private val articles: List<Article>) :
        RecyclerView.Adapter<HomeOtherArticleAdapter.OtherArticleVH>() {

    class OtherArticleVH(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val pubName: TextView = itemView.article_pub_name_following_o
        val articleTitle : TextView = itemView.article_title_home_o
        val articleImg : ImageView = itemView.article_img_home_o
        val postTime: TextView = itemView.following_home_time_o
        val shoutoutCount: TextView = itemView.following_home_shout_out_o
        val dot: TextView = itemView.following_home_dot_o

        val layout: ConstraintLayout = itemView.following_home_layout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):  OtherArticleVH {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.home_other_articles, parent, false)

        return OtherArticleVH (itemView)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: OtherArticleVH, position: Int) {
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
        if(dur.toDays() in 30..364){
            val months = dur.toDays()/30
            holder.postTime.text = if (months <= 1) {
                "$months month ago"
            } else {
                "$months months ago"
            }
        }
        if (dur.toDays() >= 365){
            val years = dur.toDays()/365
            holder.postTime.text = if(years <= 1) {
                "$years year ago"
            } else {
                "$years years ago"
            }
        }
        holder.shoutoutCount.text = currentItem.shoutouts?.toInt().toString() + " shout-outs"
        holder.pubName.text = currentItem.publication!!.name

        holder.layout.setOnClickListener{
            val intent = Intent(holder.layout.context, MainActivity::class.java)
            intent.putExtra("article",currentItem)
            holder.layout.context?.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return articles.size
    }
}
