package com.example.volume_android.fragments

import PrefUtils
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.constraintlayout.widget.Group
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.volume_android.OnboardingFragHolder
import com.example.volume_android.R
import com.example.volume_android.adapters.BigReadHomeAdapter
import com.example.volume_android.adapters.SavedArticlesAdapter
import com.example.volume_android.models.Article
import com.example.volume_android.models.Publication
import com.example.volume_android.util.GraphQlUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class SavedPublicationsFragment(val articles: List<Article>) : Fragment() {

    private lateinit var savedArticlesRV: RecyclerView
    private val prefUtils = PrefUtils()


    private val savedArticles = mutableListOf<Article>()
    private var disposables = CompositeDisposable()
    private val graphQlUtil = GraphQlUtil()


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.bookmarks_page, container, false)
        loadArticles(view)
        if(savedArticles.isEmpty()) {
            view.findViewById<Group>(R.id.no_saved_articles_group).visibility = View.VISIBLE
        } else {
            view.findViewById<Group>(R.id.no_saved_articles_group).visibility = View.GONE
        }
        return view
    }

//    override fun onResume() {
//        super.onResume()
//        if(view != null) {
//            if (savedArticles.isEmpty()) {
//                view!!.findViewById<Group>(R.id.no_saved_articles_group).visibility = View.VISIBLE
//            } else {
//                view!!.findViewById<Group>(R.id.no_saved_articles_group).visibility = View.GONE
//            }
//        }
//    }

    @SuppressLint("LongLogTag")
    private fun loadArticles(view: View){
        Log.d("saved", savedArticles.size.toString())
        val articleIds = prefUtils.getStringSet("savedArticles", mutableSetOf())?.toMutableSet()
        val obs = articleIds?.let { graphQlUtil.getArticlesByIds(it).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()) }
        if (obs != null) {
            disposables.add(obs.subscribe{
                it.data?.getArticlesByIDs?.mapTo(savedArticles, { it ->
                    Article(it.id, it.title, it.articleURL, it.imageURL, Publication(id = it.publication.id, name = it.publication.name, profileImageURL = it.publication.profileImageURL),  it.date.toString(), shoutouts =  it.shoutouts)
                })
                savedArticlesRV = view.findViewById(R.id.saved_articles_rv)
                savedArticlesRV.adapter = SavedArticlesAdapter(savedArticles)
                val layoutManager: LinearLayoutManager = LinearLayoutManager(view.context)
                savedArticlesRV.layoutManager = layoutManager
                view.findViewById<Group>(R.id.no_saved_articles_group).visibility = View.GONE
            })
        }
        if(savedArticles.isEmpty()) {
            view.findViewById<Group>(R.id.no_saved_articles_group).visibility = View.VISIBLE
        } else {
            view.findViewById<Group>(R.id.no_saved_articles_group).visibility = View.GONE
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            fragmentManager!!.beginTransaction().detach(this).attach(this).commit()
        }
    }
}