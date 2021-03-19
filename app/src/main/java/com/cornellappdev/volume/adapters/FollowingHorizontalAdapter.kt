package com.cornellappdev.volume.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cornellappdev.volume.PublicationProfileActivity
import com.cornellappdev.volume.databinding.ItemFollowedPublicationBinding
import com.cornellappdev.volume.models.Publication
import com.squareup.picasso.Picasso

class FollowingHorizontalAdapter(private val followedPublications: MutableList<Publication>) :
        RecyclerView.Adapter<FollowingHorizontalAdapter.FollowHorizontalVH>() {

    class FollowHorizontalVH(
            val binding: ItemFollowedPublicationBinding) : RecyclerView.ViewHolder(binding.root)
//    {
//        val pub_logo: ImageView = itemView.vertical_publication_logo
//        val pub_name: TextView = itemView.vertical_publication_name
//        val layout: ConstraintLayout = itemView.vert_cert_layout
//    }

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
        if (!currentItem.profileImageURL.isNullOrBlank()) {
            Picasso.get().load(currentItem.profileImageURL).into(holder.binding.ivLogo)
        }
        holder.binding.tvName.text = currentItem.name
        holder.binding.clVeticalPublicationLayout.setOnClickListener { view ->
            val intent = Intent(view.context, PublicationProfileActivity::class.java)
            intent.putExtra("publication", currentItem)
            view.context.startActivity(intent)
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