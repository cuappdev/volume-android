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
import kotlinx.android.synthetic.main.home_bookmarks_article_card.view.*

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
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):  SavedArticleVH {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.home_bookmarks_article_card, parent, false)

        return SavedArticleVH (itemView)
    }

    override fun onBindViewHolder(holder: SavedArticleVH, position: Int) {
        val currentItem : Article = articles[position]

        holder.articleTitle.text = currentItem.title
        //holder.articleImg.setImageResource()
        holder.postTime.text = "6h ago"
        holder.shoutoutCount.text = currentItem.shoutouts.toString() + " shout-outs"
        holder.pubName.text = "Creme de Cornell"

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