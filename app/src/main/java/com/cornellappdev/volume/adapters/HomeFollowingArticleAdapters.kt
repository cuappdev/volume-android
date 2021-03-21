package com.cornellappdev.volume.adapters

import android.content.Intent
import android.os.Build
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
import kotlinx.android.synthetic.main.following_home_card.view.*

class HomeFollowingArticleAdapters(private val articles: MutableList<Article>) :
        RecyclerView.Adapter<HomeFollowingArticleAdapters.FollowingArticleVH>() {

    class FollowingArticleVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val pubName: TextView = itemView.article_pub_name_following
        val articleTitle: TextView = itemView.article_title_home
        val articleImg: ImageView = itemView.article_img_home
        val postTime: TextView = itemView.following_home_time
        val shoutoutCount: TextView = itemView.following_home_shout_out
        val dot: TextView = itemView.following_home_dot

        val layout: ConstraintLayout = itemView.following_home_layout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowingArticleVH {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.following_home_card, parent, false)

        return FollowingArticleVH(itemView)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: FollowingArticleVH, position: Int) {
        val currentItem: Article = articles[position]
        holder.articleTitle.text = currentItem.title
        Article.applyNSFWFilter(currentItem, holder.articleTitle)
        if (!currentItem.imageURL.isNullOrBlank()) {
            holder.articleImg.visibility = View.VISIBLE
            Picasso.get().load(currentItem.imageURL).into(holder.articleImg)
        }
        Article.setCorrectDateText(currentItem, holder.postTime)
        holder.shoutoutCount.text = currentItem.shoutouts?.toInt().toString() + " shout-outs"
        holder.pubName.text = currentItem.publication!!.name

        holder.layout.setOnClickListener {
            val intent = Intent(holder.layout.context, MainActivity::class.java)
            intent.putExtra("article", currentItem)
            holder.layout.context?.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return articles.size
    }

    fun clear() {
        articles.clear()
        notifyDataSetChanged()
    }

    fun addAll(list: List<Article>) {
        articles.addAll(list)
        notifyDataSetChanged()
    }
}