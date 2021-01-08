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
import kotlinx.android.synthetic.main.home_bookmarks_article_card.view.*
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
        val dot: ImageView = itemView.dot_b
        val bookmarkImage = itemView.article_bookmark_icon
        val layout: ConstraintLayout = itemView.desc_holder_b
        val layoutMain:ConstraintLayout = itemView.saved_article_layout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):  SavedArticleVH {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.home_bookmarks_article_card, parent, false)

        return SavedArticleVH (itemView)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: SavedArticleVH, position: Int) {
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
        holder.pubName.text = currentItem.publication?.name

        holder.layoutMain.setOnClickListener{
            Log.d("SavedArticle", "layout clicked")
            val intent = Intent(holder.layout.context, MainActivity::class.java)
            intent.putExtra("article",currentItem)
            holder.layout.context?.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return articles.size
    }
}