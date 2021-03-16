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
import kotlinx.android.synthetic.main.following_home_card.view.following_home_layout
import kotlinx.android.synthetic.main.home_other_articles.view.*

class HomeOtherArticleAdapter(private val articles: MutableList<Article>) :
        RecyclerView.Adapter<HomeOtherArticleAdapter.OtherArticleVH>() {

    class OtherArticleVH(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val pubName: TextView = itemView.article_pub_name_following_o
        val articleTitle: TextView = itemView.article_title_home_o
        val articleImg: ImageView = itemView.article_img_home_o
        val postTime: TextView = itemView.following_home_time_o
        val shoutoutCount: TextView = itemView.following_home_shout_out_o
        val layout: ConstraintLayout = itemView.following_home_layout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):  OtherArticleVH {
        val itemView = LayoutInflater
                .from(parent.context)
                .inflate(R.layout.home_other_articles, parent, false)
        return OtherArticleVH (itemView)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: OtherArticleVH, position: Int) {
        val currentItem = articles[position]
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
            intent.putExtra("article",currentItem)
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
