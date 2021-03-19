package com.cornellappdev.volume.adapters

import PrefUtils
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.cornellappdev.volume.PublicationProfileActivity
import com.cornellappdev.volume.R
import com.cornellappdev.volume.databinding.ItemMorePublicationBinding
import com.cornellappdev.volume.models.Article
import com.cornellappdev.volume.models.Publication
import com.squareup.picasso.Picasso

class MorePublicationsAdapter(private val publicationList: List<Publication>,
                              private val prefUtils: PrefUtils) :
        RecyclerView.Adapter<MorePublicationsAdapter.MorePublicationVH>() {

    class MorePublicationVH(
            val binding: ItemMorePublicationBinding) : RecyclerView.ViewHolder(binding.root)
//    {
//        val pub_logo : ImageView = itemView.publication_card_logo
//        val pub_name : TextView = itemView.publication_card_name
//        val pub_desc : TextView = itemView.publication_card_description
//        val pub_quote : TextView = itemView.publication_card_quote
//        val pub_follow: ImageView = itemView.publication_card_follow
//        val pub_layout: ConstraintLayout = itemView.other_pub_layout
//    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MorePublicationVH {
        val binding = ItemMorePublicationBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
        return MorePublicationVH(binding)
    }

    override fun getItemCount(): Int {
        return publicationList.size
    }

    override fun onBindViewHolder(holder: MorePublicationVH, position: Int) {
        val currentItem = publicationList[position]
        if (!currentItem.profileImageURL.isNullOrBlank()) {
            Picasso.get().load(currentItem.profileImageURL).into(holder.binding.ivLogo)
        }
        currentItem.mostRecentArticle?.let {
            Article.applyNSFWFilter(it, holder.binding.tvRecentArticleTitle)
        }
        holder.binding.tvName.text = currentItem.name
        holder.binding.tvDescription.text = currentItem.bio
        holder.binding.tvRecentArticleTitle.text = currentItem.mostRecentArticle?.title

        val currentFollowingSet =
                prefUtils.getStringSet("following", mutableSetOf())

        if (currentFollowingSet?.contains(currentItem.id) == true) {
            holder.binding.btnFollow.setImageResource(R.drawable.ic_followchecksvg)
        } else {
            holder.binding.btnFollow.setImageResource(R.drawable.ic_followplussvg)
        }

        holder.binding.btnFollow.setOnClickListener {
            if (holder.binding.btnFollow.drawable.constantState == ContextCompat.getDrawable(it.context,
                            R.drawable.ic_followplussvg)!!.constantState) {
                holder.binding.btnFollow.setImageResource(R.drawable.ic_followchecksvg)
                    val tempSet =
                            prefUtils.getStringSet("following", mutableSetOf())?.toMutableSet()
                    if (tempSet != null) {
                        tempSet.add(currentItem.id)
                        prefUtils.save("following", tempSet)
                    }
            } else {
                holder.binding.btnFollow.setImageResource(R.drawable.ic_followplussvg)
                val tempSet =
                        prefUtils.getStringSet("following", mutableSetOf())?.toMutableSet()
                if (tempSet != null) {
                    tempSet.remove(currentItem.id)
                    prefUtils.save("following", tempSet)
                }
            }
        }

        holder.binding.clPublicationLayout.setOnClickListener { view ->
            val intent = Intent(view.context, PublicationProfileActivity::class.java)
            intent.putExtra("publication", currentItem)
            view.context.startActivity(intent)
        }
    }
}