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
import com.example.volume_android.models.Publication

class OnboardingFrag2 (val publications: List<Publication>) : Fragment() {

    private lateinit var publicationRV : RecyclerView

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view =  inflater.inflate(R.layout.onboardingfrag2, container, false)

        publicationRV = view.findViewById(R.id.onboarding2_rv)
        publicationRV.adapter = FollowPublicationsAdapter(publications, view.context)
        publicationRV.layoutManager = LinearLayoutManager(view.context)
        publicationRV.setHasFixedSize(true)

        return view
    }
}