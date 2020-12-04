package com.example.volume_android.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.volume_android.PublicationProfileActivity
import com.example.volume_android.R
import com.example.volume_android.models.Publication
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
        
        holder.pub_logo.setImageResource(R.drawable.cremelogotrans)
        holder.pub_name.text = currentItem.name

        holder.layout.setOnClickListener {
            val intent = Intent(holder.layout.context, PublicationProfileActivity::class.java)
            holder.layout.context?.startActivity(intent)
        }

    }
}