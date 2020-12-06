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
import com.example.volume_android.adapters.SavedArticlesAdapter
import com.example.volume_android.models.Article

class SavedPublicationsFragment(val articles: List<Article>) : Fragment() {

    private lateinit var savedArticlesRV: RecyclerView


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.bookmarks_page, container, false)
        savedArticlesRV = view.findViewById(R.id.saved_articles_rv)
        savedArticlesRV.adapter = SavedArticlesAdapter(articles)
        val layoutManager: LinearLayoutManager = LinearLayoutManager(view.context)
        savedArticlesRV.layoutManager = layoutManager
        return view
    }
}