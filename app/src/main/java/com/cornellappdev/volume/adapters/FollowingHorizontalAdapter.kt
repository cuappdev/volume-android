package com.cornellappdev.volume.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.cornellappdev.volume.PublicationProfileActivity
import com.cornellappdev.volume.R
import com.cornellappdev.volume.models.Publication
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.vertical_circular_publication_card.view.*

class FollowingHorizontalAdapter(private val followedPublications: List<Publication>) :
        RecyclerView.Adapter<FollowingHorizontalAdapter.FollowHorizontalVH>() {

    class FollowHorizontalVH(itemView : View) : RecyclerView.ViewHolder(itemView){

        val pub_logo : ImageView = itemView.vertical_publication_logo
        val pub_name : TextView = itemView.vertical_publication_name
        val layout: ConstraintLayout = itemView.vert_cert_layout

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowHorizontalVH {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.vertical_circular_publication_card, parent, false)
        return FollowHorizontalVH(itemView)

    }

    override fun getItemCount(): Int {
        return followedPublications.size
    }

    override fun onBindViewHolder(holder: FollowHorizontalVH, position: Int) {
        val currentItem : Publication = followedPublications[position]

        if(currentItem.profileImageURL != null && currentItem.profileImageURL != ""){
            Picasso.get().load(currentItem.profileImageURL).into(holder.pub_logo)
        }

        holder.pub_name.text = currentItem.name

        holder.layout.setOnClickListener {
            val intent = Intent(holder.layout.context, PublicationProfileActivity::class.java)
            intent.putExtra("publication", currentItem)
            holder.layout.context?.startActivity(intent)
        }

    }
}