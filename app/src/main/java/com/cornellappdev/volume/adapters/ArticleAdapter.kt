package com.cornellappdev.volume.adapters

import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.cornellappdev.volume.MainActivity
import com.cornellappdev.volume.databinding.ItemArticleBinding
import com.cornellappdev.volume.models.Article
import com.squareup.picasso.Picasso

class ArticleAdapter(private val articles: List<Article>) :
        RecyclerView.Adapter<ArticleAdapter.ArticleVH>() {

    class ArticleVH(val binding: ItemArticleBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleVH {
        val binding = ItemArticleBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
        return ArticleVH(binding)
    }

    override fun getItemCount(): Int {
        return articles.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ArticleVH, position: Int) {
        val currentItem = articles[position]
        holder.binding.tvArticleTitle.text = currentItem.title
        Article.applyNSFWFilter(currentItem, holder.binding.tvArticleTitle)
        if (!currentItem.imageURL.isNullOrBlank()) {
            holder.binding.ivArticleImage.visibility = View.VISIBLE
            Picasso.get().load(currentItem.imageURL).fit().centerCrop().into(holder.binding.ivArticleImage)
        }
        Article.setCorrectDateText(currentItem, holder.binding.tvTimePosted)
        holder.binding.tvShoutoutCount.text =
                currentItem.shoutouts?.toInt().toString() + " shout-outs"
        holder.binding.clArticleLayout.setOnClickListener { view ->
            val intent = Intent(view.context, MainActivity::class.java)
            intent.putExtra(Article.INTENT_KEY, currentItem)
            view.context.startActivity(intent)
        }
    }
}