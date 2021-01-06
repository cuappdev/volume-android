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
import kotlinx.android.synthetic.main.following_home_card.view.*
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class HomeFollowingArticleAdapters(private val articles: List<Article>) :
        RecyclerView.Adapter<HomeFollowingArticleAdapters.FollowingArticleVH>() {

    class FollowingArticleVH(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val pubName: TextView = itemView.article_pub_name_following
        val articleTitle : TextView = itemView.article_title_home
        val articleImg : ImageView = itemView.article_img_home
        val postTime: TextView = itemView.following_home_time
        val shoutoutCount: TextView = itemView.following_home_shout_out
        val dot: TextView = itemView.following_home_dot

        val layout: ConstraintLayout = itemView.following_home_layout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):  FollowingArticleVH {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.following_home_card, parent, false)

        return FollowingArticleVH (itemView)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: FollowingArticleVH, position: Int) {
        val currentItem : Article = articles[position]

        if (currentItem.title?.length!! > 55){
            holder.articleTitle.text = currentItem.title?.subSequence(0,54).toString() + " ..."
        }
        else {
            holder.articleTitle.text = currentItem.title
        }
        if(currentItem.imageURL != null && currentItem.imageURL != ""){
            Picasso.get().load(currentItem.imageURL).into(holder.articleImg)
        }
        val format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val datePublished = LocalDateTime.parse(currentItem.date, format)
        var dur = Duration.between(datePublished, LocalDateTime.now())
        Log.d("TimeStuff", dur.toDays().toString())
        if(dur.toDays() < 1){
            val hours = dur.toHours()
            holder.postTime.text = hours.toInt().toString() + "h" + " ago"
        }
        if(dur.toDays() in 2..6) {
            holder.postTime.text = dur.toDays().toInt().toString() + " days" + " ago"
        }
        if(dur.toDays() in 7..29) {
            val weeks = dur.toDays()/7
            holder.postTime.text = weeks.toString() + " days" + " ago"
        }
        if(dur.toDays() >= 30 && dur.toDays()> 365){
            val months = dur.toDays()/30
            holder.postTime.text = months.toInt().toString() + " months" + " ago"
        }
        if (dur.toDays()>=365){
            val years = dur.toDays()/365
            holder.postTime.text = years.toInt().toString() + " years" + " ago"
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