package com.cornellappdev.volume.adapters

import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.cornellappdev.volume.MainActivity
import com.cornellappdev.volume.databinding.ItemSavedArticleBinding
import com.cornellappdev.volume.models.Article
import com.squareup.picasso.Picasso

class SavedArticlesAdapter(private val articles: List<Article>) :
        RecyclerView.Adapter<SavedArticlesAdapter.SavedArticleVH>() {

    class SavedArticleVH(val binding: ItemSavedArticleBinding) : RecyclerView.ViewHolder(binding.root)
//    class SavedArticleVH(val binding: ItemSavedArticleBinding) : RecyclerView.ViewHolder(binding.root) {
//        val pubName: TextView = itemView.pub_name_article_layout_b
//        val articleTitle : TextView = itemView.article_title_b
//        val articleImg : ImageView = itemView.article_img_b
//        val postTime: TextView = itemView.post_time_b
//        val shoutoutCount: TextView = itemView.article_shout_count_b
//        val layout: ConstraintLayout = itemView.desc_holder_b
//        val layoutMain: ConstraintLayout = itemView.saved_article_layout
//    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):  SavedArticleVH {
        val binding = ItemSavedArticleBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
        return SavedArticleVH(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: SavedArticleVH, position: Int) {
        val currentItem = articles[position]
        holder.binding.tvArticleTitle.text = currentItem.title
        Article.applyNSFWFilter(currentItem, holder.binding.tvArticleTitle)
        if (!currentItem.imageURL.isNullOrBlank()) {
            holder.binding.ivArticleImage.visibility = View.VISIBLE
            Picasso.get().load(currentItem.imageURL).into(holder.binding.ivArticleImage)
        }
        Article.setCorrectDateText(currentItem, holder.binding.tvTimePosted)
        holder.binding.tvShoutoutCount.text =
                currentItem.shoutouts?.toInt().toString() + " shout-outs"
        holder.binding.tvPublicationName.text = currentItem.publication!!.name
        holder.binding.clArticleLayout.setOnClickListener { view ->
//            val intent = Intent(holder.layout.context, MainActivity::class.java)
//            intent.putExtra("article",currentItem)
//            holder.layout.context?.startActivity(intent)
            val intent = Intent(view.context, MainActivity::class.java)
            intent.putExtra("article", currentItem)
            view.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return articles.size
    }
}