package com.example.volume_android.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.volume_android.R
import com.example.volume_android.models.Publication
import kotlinx.android.synthetic.main.publication_card.view.*

class FollowPublicationsAdapter(private val publicationList: List<Publication>,
                                private val context: Context) :
        RecyclerView.Adapter<FollowPublicationsAdapter.FollowPublicationVH>() {

    class FollowPublicationVH(itemView : View) : RecyclerView.ViewHolder(itemView){

        val pub_logo : ImageView = itemView.publication_card_logo
        val pub_name : TextView = itemView.publication_card_name
        val pub_desc : TextView = itemView.publication_card_description
        val pub_quote : TextView = itemView.publication_card_quote
        val pub_follow: ImageView = itemView.publication_card_follow

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowPublicationVH {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.publication_card, parent, false)
        return FollowPublicationVH(itemView)
    }

    override fun getItemCount(): Int {
        return publicationList.size
    }

    override fun onBindViewHolder(holder: FollowPublicationVH, position: Int) {
        val currentItem : Publication = publicationList[position]

        //TODO: This resource should take in a link, but will pass it an id for now
        holder.pub_logo.setImageResource(R.drawable.cremelogotrans)
        holder.pub_name.text = currentItem.name
        holder.pub_desc.text = currentItem.bio
        holder.pub_quote.text = "We Creme for Cornell" //TODO: What is this?

        holder.pub_follow.setOnClickListener {
            if(holder.pub_follow.drawable.constantState == ContextCompat.getDrawable(context,
                            R.drawable.ic_followplussvg)!!.constantState){
                holder.pub_follow.setImageResource(R.drawable.ic_followchecksvg)
            }else  holder.pub_follow.setImageResource(R.drawable.ic_followplussvg)
        }

    }
}