package com.example.volume_android.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.volume_android.MainActivity
import com.example.volume_android.R
import com.example.volume_android.models.Article
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.article_card.view.*

class ArticleAdapter(private val articles: List<Article>) :
        RecyclerView.Adapter<ArticleAdapter.ArticleVH>() {

    class ArticleVH(itemView : View) : RecyclerView.ViewHolder(itemView){

        val articleTitle : TextView = itemView.article_title
        val articleImg : ImageView = itemView.article_img
        val postTime: TextView = itemView.post_time
        val shoutoutCount: TextView = itemView.article_shout_count

        val layout: ConstraintLayout = itemView.article_layout

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleVH {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.article_card, parent, false)
        return ArticleVH(itemView)

    }

    override fun getItemCount(): Int {
        return articles.size
    }

    override fun onBindViewHolder(holder: ArticleVH, position: Int) {
        val currentItem : Article = articles[position]

        holder.articleTitle.text = currentItem.title
        //holder.articleImg.setImageResource()
        holder.postTime.text = "6h ago"
        holder.shoutoutCount.text = currentItem.shoutouts.toString() + " shout-outs"
        if(currentItem.imageURL != null && currentItem.imageURL != ""){
            Picasso.get().load(currentItem.imageURL).into(holder.articleImg)
        }
        holder.layout.setOnClickListener{
            val intent = Intent(holder.layout.context, MainActivity::class.java)
            intent.putExtra("articleURL",currentItem.articleURL)
            holder.layout.context?.startActivity(intent)
        }
    }
}