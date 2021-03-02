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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.cornellappdev.volume.MainActivity
import com.cornellappdev.volume.R
import com.cornellappdev.volume.models.Article
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.vertical_article_home_card.view.*
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


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
        holder.articleTitle.text = currentItem.title
        currentItem.let { Article.applyNSFWFilter(it, holder.articleTitle) }
        if(!currentItem.imageURL.isNullOrEmpty()){
            Picasso.get().load(currentItem.imageURL).into(holder.articleImg)
        } else if (!currentItem.publication?.profileImageURL.isNullOrEmpty()) {
            Picasso.get()
                    .load(currentItem.publication?.profileImageURL)
                    .resize(180, 180)
                    .centerCrop()
                    .into(holder.articleImg)
        }
        Article.setCorrectDateText(currentItem, holder.postTime)
        holder.shoutoutCount.text = currentItem.shoutouts?.toInt().toString() + " shout-outs"
        holder.pubName.text = currentItem.publication!!.name

        holder.layout.setOnClickListener{
            val intent = Intent(holder.layout.context, MainActivity::class.java)
            intent.putExtra("article", currentItem)
            holder.layout.context?.startActivity(intent)
        }
    }


}