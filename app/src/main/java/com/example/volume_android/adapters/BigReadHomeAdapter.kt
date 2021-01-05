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
import kotlinx.android.synthetic.main.article_card.view.*
import kotlinx.android.synthetic.main.article_card.view.article_img
import kotlinx.android.synthetic.main.vertical_article_home_card.view.*

class BigReadHomeAdapter (private val articles: List<Article>) :
        RecyclerView.Adapter<BigReadHomeAdapter.BigReadArticleVH>() {

    class BigReadArticleVH(itemView : View) : RecyclerView.ViewHolder(itemView){
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
        return BigReadArticleVH (itemView)

    }

    override fun getItemCount(): Int {
        return articles.size
    }

    override fun onBindViewHolder(holder: BigReadArticleVH, position: Int) {
        val currentItem : Article = articles[position]
        if (currentItem.title?.length!! > 62){
            holder.articleTitle.text = currentItem.title?.subSequence(0,61).toString() + " ..."
        }
        else {
            holder.articleTitle.text = currentItem.title
        }

        if(currentItem.imageURL != null && currentItem.imageURL != ""){
            Picasso.get().load(currentItem.imageURL).into(holder.articleImg)
        }
        holder.postTime.text = currentItem.date
        holder.shoutoutCount.text = currentItem.shoutouts.toString() + " shout-outs"
        holder.pubName.text = currentItem.publication!!.name

        holder.layout.setOnClickListener{
            val intent = Intent(holder.layout.context, MainActivity::class.java)
            //intent.putExtra("articleURL",currentItem.articleURL)
            intent.putExtra("article", currentItem)
            holder.layout.context?.startActivity(intent)
        }
    }


}