package com.example.volume_android.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.volume_android.R
import com.example.volume_android.adapters.FollowPublicationsAdapter
import com.example.volume_android.adapters.FollowingHorizontalAdapter
import com.example.volume_android.models.Publication

class PublicationsFragment(val publications: List<Publication>) : Fragment() {

    private lateinit var followpublicationRV : RecyclerView
    private lateinit var morepublicationRV: RecyclerView


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view =  inflater.inflate(R.layout.all_publications, container, false)

        followpublicationRV = view.findViewById(R.id.following_all_publications_rv)
        followpublicationRV.adapter = FollowingHorizontalAdapter(publications)
        val linearLayoutManager : LinearLayoutManager = LinearLayoutManager(view.context)
        linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        followpublicationRV.layoutManager = linearLayoutManager
        followpublicationRV.setHasFixedSize(true)


        morepublicationRV = view.findViewById(R.id.follwing_more_publications_rv)
        morepublicationRV.adapter = FollowPublicationsAdapter(publications, view.context)
        morepublicationRV.layoutManager = LinearLayoutManager(view.context)
        morepublicationRV.setHasFixedSize(true)


        return view
    }
}