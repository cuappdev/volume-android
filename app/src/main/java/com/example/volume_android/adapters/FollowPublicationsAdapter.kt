package com.example.volume_android.adapters

import PrefUtils
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.volume_android.PublicationProfileActivity
import com.example.volume_android.R
import com.example.volume_android.models.Publication
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.publication_card.view.*
import kotlinx.android.synthetic.main.publication_profile_activity.*

class FollowPublicationsAdapter(private val publicationList: List<Publication>,
                                private val context: Context) :
        RecyclerView.Adapter<FollowPublicationsAdapter.FollowPublicationVH>() {

    class FollowPublicationVH(itemView : View) : RecyclerView.ViewHolder(itemView){

        val pub_logo : ImageView = itemView.publication_card_logo
        val pub_name : TextView = itemView.publication_card_name
        val pub_desc : TextView = itemView.publication_card_description
        val pub_quote : TextView = itemView.publication_card_quote
        val pub_follow: ImageView = itemView.publication_card_follow
        val pub_layout:ConstraintLayout = itemView.other_pub_layout

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowPublicationVH {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.publication_card, parent, false)
        return FollowPublicationVH(itemView)
    }

    override fun getItemCount(): Int {
        return publicationList.size
    }

    override fun onBindViewHolder(holder: FollowPublicationVH, position: Int) {

        val prefUtils: PrefUtils = PrefUtils(context)
        val currentItem : Publication = publicationList[position]
        val currentFollowingSet = prefUtils.getStringSet("following", mutableSetOf())?.toMutableSet()
        Log.d("Following Pubs", currentFollowingSet?.size.toString())

        if(currentItem.profileImageURL != null && currentItem.profileImageURL != ""){
            Picasso.get().load(currentItem.profileImageURL).into(holder.pub_logo)
        }

        holder.pub_name.text = currentItem.name
        holder.pub_desc.text = currentItem.bio
        holder.pub_quote.text = currentItem.mostRecentArticle?.title

        if(currentFollowingSet!!.contains(currentItem.id)){
            holder.pub_follow.setImageResource(R.drawable.ic_followchecksvg)
        }
        else{
            holder.pub_follow.setImageResource(R.drawable.ic_followplussvg)
        }

        holder.pub_follow.setOnClickListener {
            if(holder.pub_follow.drawable.constantState == ContextCompat.getDrawable(context,
                            R.drawable.ic_followplussvg)!!.constantState){
                holder.pub_follow.setImageResource(R.drawable.ic_followchecksvg)
                currentFollowingSet?.add(currentItem.id)
                if (currentFollowingSet != null) {
                    prefUtils.save("following", currentFollowingSet)
                }

            }else  {
                holder.pub_follow.setImageResource(R.drawable.ic_followplussvg)
                currentFollowingSet?.remove(currentItem.id)
                if (currentFollowingSet != null) {
                    prefUtils.save("following", currentFollowingSet)
                }

            }
        }

        holder.pub_layout.setOnClickListener{
            val intent = Intent(holder.pub_layout.context, PublicationProfileActivity::class.java)
            intent.putExtra("publication", currentItem)
            holder.pub_layout.context?.startActivity(intent)
        }
    }
}