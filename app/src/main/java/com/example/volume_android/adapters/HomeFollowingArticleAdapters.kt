package com.example.volume_android.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.volume_android.MainActivity
import com.example.volume_android.R
import com.example.volume_android.models.Article
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.following_home_card.view.*

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

    override fun onBindViewHolder(holder: FollowingArticleVH, position: Int) {
        val currentItem : Article = articles[position]

        holder.articleTitle.text = currentItem.title
        if(currentItem.imageURL != null && currentItem.imageURL != ""){
            Picasso.get().load(currentItem.imageURL).into(holder.articleImg)
        }
        holder.postTime.text = currentItem.date
        holder.shoutoutCount.text = currentItem.shoutouts.toString() + " shout-outs"
        holder.pubName.text = currentItem.publication!!.name

        holder.layout.setOnClickListener{
            val intent = Intent(holder.layout.context, MainActivity::class.java)
            intent.putExtra("articleURL",currentItem.articleURL)
            holder.layout.context?.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return articles.size
    }
}