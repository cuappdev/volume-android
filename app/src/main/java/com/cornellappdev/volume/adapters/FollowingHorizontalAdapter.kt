package com.cornellappdev.volume.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cornellappdev.volume.PublicationProfileActivity
import com.cornellappdev.volume.analytics.NavigationSource
import com.cornellappdev.volume.analytics.NavigationSource.Companion.putParcelableExtra
import com.cornellappdev.volume.databinding.ItemFollowedPublicationBinding
import com.cornellappdev.volume.models.Publication
import com.squareup.picasso.Picasso

class FollowingHorizontalAdapter(
    var followedPublications: MutableList<Publication>,
    private val mAdapterOnClickHandler: AdapterOnClickHandler?
) :
    RecyclerView.Adapter<FollowingHorizontalAdapter.FollowHorizontalVH>() {

    interface AdapterOnClickHandler {
        fun onPublicationClick(publication: Publication)
    }

    class FollowHorizontalVH(
        val binding: ItemFollowedPublicationBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowHorizontalVH {
        val binding = ItemFollowedPublicationBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return FollowHorizontalVH(binding)
    }

    override fun getItemCount(): Int {
        return followedPublications.size
    }

    override fun onBindViewHolder(holder: FollowHorizontalVH, position: Int) {
        val currentItem = followedPublications[position]
        if (currentItem.profileImageURL.isNotBlank()) {
            Picasso.get().load(currentItem.profileImageURL).into(holder.binding.ivLogo)
        }
        holder.binding.tvName.text = currentItem.name
        holder.binding.clVeticalPublicationLayout.setOnClickListener {
            mAdapterOnClickHandler?.onPublicationClick(currentItem)
        }
    }

    fun clear() {
        followedPublications.clear()
        notifyDataSetChanged()
    }

    fun addAll(list: List<Publication>) {
        followedPublications.addAll(list)
        notifyDataSetChanged()
    }
}