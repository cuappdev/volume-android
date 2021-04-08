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
                              private val prefUtils: PrefUtils,
                              private val mAdapterOnClickHandler: AdapterOnClickHandler?) :
        RecyclerView.Adapter<MorePublicationsAdapter.MorePublicationVH>() {

    interface AdapterOnClickHandler {
        fun onFollowClick(wasFollowed: Boolean)
    }

    class MorePublicationVH(
            val binding: ItemMorePublicationBinding) : RecyclerView.ViewHolder(binding.root)

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
                prefUtils.getStringSet(PrefUtils.FOLLOWING_KEY, mutableSetOf())

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
                        prefUtils.getStringSet(PrefUtils.FOLLOWING_KEY, mutableSetOf())?.toMutableSet()
                if (tempSet != null) {
                    tempSet.add(currentItem.id)
                    prefUtils.save(PrefUtils.FOLLOWING_KEY, tempSet)
                }
                mAdapterOnClickHandler?.onFollowClick(wasFollowed = true)
            } else {
                holder.binding.btnFollow.setImageResource(R.drawable.ic_followplussvg)
                val tempSet =
                        prefUtils.getStringSet(PrefUtils.FOLLOWING_KEY, mutableSetOf())?.toMutableSet()
                if (tempSet != null) {
                    tempSet.remove(currentItem.id)
                    prefUtils.save(PrefUtils.FOLLOWING_KEY, tempSet)
                }
                mAdapterOnClickHandler?.onFollowClick(wasFollowed = false)
            }
        }

        holder.binding.clPublicationLayout.setOnClickListener { view ->
            val intent = Intent(view.context, PublicationProfileActivity::class.java)
            intent.putExtra(Publication.INTENT_KEY, currentItem)
            view.context.startActivity(intent)
        }
    }
}