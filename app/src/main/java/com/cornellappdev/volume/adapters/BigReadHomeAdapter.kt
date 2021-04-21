package com.cornellappdev.volume.adapters

import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.cornellappdev.volume.MainActivity
import com.cornellappdev.volume.R
import com.cornellappdev.volume.analytics.NavigationSource
import com.cornellappdev.volume.analytics.NavigationSource.Companion.putParcelableExtra
import com.cornellappdev.volume.databinding.ItemBigReadBinding
import com.cornellappdev.volume.models.Article
import com.squareup.picasso.Picasso

class BigReadHomeAdapter(private val articles: MutableList<Article>) :
        RecyclerView.Adapter<BigReadHomeAdapter.BigReadArticleVH>() {

    private lateinit var context: Context

    class BigReadArticleVH(val binding: ItemBigReadBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BigReadArticleVH {
        val binding = ItemBigReadBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
        context = parent.context
        return BigReadArticleVH(binding)
    }

    override fun getItemCount(): Int {
        return articles.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: BigReadArticleVH, position: Int) {
        val currentItem = articles[position]
        holder.binding.tvArticleTitle.text = currentItem.title
        Article.applyNSFWFilter(currentItem, holder.binding.tvArticleTitle)
        if (!currentItem.imageURL.isNullOrEmpty()) {
            Picasso.get().load(currentItem.imageURL).fit().centerCrop().into(holder.binding.ivArticleImage)
        } else if (!currentItem.publication?.profileImageURL.isNullOrEmpty()) {
            Picasso.get()
                    .load(currentItem.publication?.profileImageURL)
                    .fit()
                    .centerCrop()
                    .into(holder.binding.ivArticleImage)
        }
        Article.setCorrectDateText(currentItem, holder.binding.tvTimePosted, context)
        holder.binding.tvShoutoutCount.text =
                context.getString(R.string.shoutout_count, currentItem.shoutouts.toInt())
        holder.binding.tvPublicationName.text = currentItem.publication?.name
        holder.binding.clBigReadLayout.setOnClickListener { view ->
            val intent = Intent(view.context, MainActivity::class.java)
            intent.putExtra(Article.INTENT_KEY, currentItem)
            intent.putParcelableExtra(NavigationSource.INTENT_KEY, NavigationSource.TRENDING_ARTICLES)
            view.context.startActivity(intent)
        }
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