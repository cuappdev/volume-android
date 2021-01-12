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
import kotlinx.android.synthetic.main.vertical_article_home_card.view.*
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

class BigReadHomeAdapter(private val articles: List<Article>) :
        RecyclerView.Adapter<BigReadHomeAdapter.BigReadArticleVH>() {

    class BigReadArticleVH(itemView: View) : RecyclerView.ViewHolder(itemView){
        val pubName: TextView = itemView.vert_card_pub_name
        val articleTitle : TextView = itemView.article_title_big_read
        val articleImg : ImageView = itemView.vert_img_view
        val postTime: TextView = itemView.big_read_card_layout_time
        val shoutoutCount: TextView = itemView.big_read_card_layout_shoutouts
        val dot: TextView = itemView.big_read_card_layout_dot

        val layout: ConstraintLayout = itemView.big_read_layout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BigReadArticleVH {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.vertical_article_home_card, parent, false)
        return BigReadArticleVH(itemView)

    }

    override fun getItemCount(): Int {
        return articles.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: BigReadArticleVH, position: Int) {
        val currentItem : Article = articles[position]
        if (currentItem.title?.length!! > 62){
            holder.articleTitle.text = currentItem.title?.subSequence(0, 61).toString() + " ..."
        }
        else {
            holder.articleTitle.text = currentItem.title
        }

        if(currentItem.imageURL != null && currentItem.imageURL != ""){
            Picasso.get().load(currentItem.imageURL).into(holder.articleImg)
        }
        holder.postTime.text = currentItem.date
        Log.d("Time", currentItem.date)
        //getting article posting time and date in phones timeZone
        val format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val datePublished = LocalDateTime.parse(currentItem.date, format)
        var dur = Duration.between(datePublished, LocalDateTime.now())
        Log.d("TimeStuff", dur.toDays().toString())
        if(dur.toDays() < 1){
            val hours = dur.toHours()
            holder.postTime.text = hours.toInt().toString() + "h" + " ago"
        }
        if(dur.toDays() in 1..6) {
            if (dur.toDays() <= 1) {
                holder.postTime.text = dur.toDays().toInt().toString() + " day" + " ago"
            }
            holder.postTime.text = dur.toDays().toInt().toString() + " days" + " ago"
        }
        if(dur.toDays() in 7..29) {
            val weeks = dur.toDays()/7
            if(weeks <= 1){
                holder.postTime.text = weeks.toString() + " week" + "ago"
            }
            holder.postTime.text = weeks.toString() + " weeks" + " ago"
        }
        if(dur.toDays() >= 30 && dur.toDays()> 365){
            val months = dur.toDays()/30
            if (months <= 1) {
                holder.postTime.text = months.toInt().toString() + " month" + " ago"
            }
            holder.postTime.text = months.toInt().toString() + " months" + " ago"
        }
        if (dur.toDays()>=365){
            val years = dur.toDays()/365
            if(years <= 1){
                holder.postTime.text = years.toInt().toString() + " year" + " ago"
            }
            holder.postTime.text = years.toInt().toString() + " years" + " ago"
        }
        holder.shoutoutCount.text = currentItem.shoutouts?.toInt().toString() + " shout-outs"
        holder.pubName.text = currentItem.publication!!.name

        holder.layout.setOnClickListener{
            val intent = Intent(holder.layout.context, MainActivity::class.java)
            //intent.putExtra("articleURL",currentItem.articleURL)
            intent.putExtra("article", currentItem)
            holder.layout.context?.startActivity(intent)
        }
    }


}