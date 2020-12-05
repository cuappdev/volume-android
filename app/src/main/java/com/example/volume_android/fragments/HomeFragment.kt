package com.example.volume_android.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.volume_android.OnboardingFragHolder
import com.example.volume_android.R
import com.example.volume_android.adapters.BigReadHomeAdapter
import com.example.volume_android.adapters.HomeFollowingArticleAdapters
import com.example.volume_android.models.Article
import com.example.volume_android.models.Publication

class HomeFragment(val articles: List<Article>) : Fragment() {


    private lateinit var bigRedRv: RecyclerView
    private lateinit var followingRv: RecyclerView
    private lateinit var otherArticles: RecyclerView

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view1 = inflater.inflate(R.layout.home_fragment, container, false)
        bigRedRv = view1.findViewById(R.id.big_red_rv)!!
        bigRedRv.adapter = BigReadHomeAdapter(articles)
        val linearLayoutManager : LinearLayoutManager = LinearLayoutManager(view1.context)
        linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        bigRedRv.layoutManager = linearLayoutManager

        followingRv = view1.findViewById(R.id.follwing_rv)
        val linearLayoutManager2: LinearLayoutManager = LinearLayoutManager(view1.context)
        followingRv.layoutManager = linearLayoutManager2
        followingRv.adapter = HomeFollowingArticleAdapters(articles)
        otherArticles = view1.findViewById(R.id.other_articlesrv)
        val linearLayoutManager3: LinearLayoutManager = LinearLayoutManager(view1.context)
        otherArticles.layoutManager = linearLayoutManager3
        otherArticles.adapter = HomeFollowingArticleAdapters(articles)
        return view1
    }
}