package com.example.volume_android.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.volume_android.R
import com.example.volume_android.adapters.BigReadHomeAdapter
import com.example.volume_android.adapters.HomeFollowingArticleAdapters
import com.example.volume_android.models.Article
import com.example.volume_android.networking.GraphQlUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException

class HomeFragment(val articles: List<Article>) : Fragment() {


    private lateinit var bigRedRv: RecyclerView
    private lateinit var followingRv: RecyclerView
    private lateinit var otherArticles: RecyclerView
    private lateinit var disposables: CompositeDisposable



    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view1 = inflater.inflate(R.layout.home_fragment, container, false)

        disposables = CompositeDisposable()


        val graphQlUtil = GraphQlUtil()

        //Get Trending Articles
        val trendingObs = graphQlUtil.getTrendingArticles(10.0, "2020-12-10T12:34:20.000Z").subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        disposables.add(trendingObs.subscribe{
            var trendingArticles = mutableListOf<Article>()

            it.data?.getTrendingArticles?.mapTo(trendingArticles, { it -> Article(it.articleURL, it.date.toString(), it.id, it.imageURL, it.publicationID, it.shoutouts, it.title)
            })

            bigRedRv = view1.findViewById(R.id.big_red_rv)!!

            bigRedRv.adapter = BigReadHomeAdapter(trendingArticles)
            val linearLayoutManager : LinearLayoutManager = LinearLayoutManager(view1.context)
            linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
            bigRedRv.layoutManager = linearLayoutManager
        })



        followingRv = view1.findViewById(R.id.follwing_rv)
        val linearLayoutManager2: LinearLayoutManager = LinearLayoutManager(view1.context)
        followingRv.layoutManager = linearLayoutManager2
        followingRv.adapter = HomeFollowingArticleAdapters(articles)
        otherArticles = view1.findViewById(R.id.other_articlesrv)
        val linearLayoutManager3: LinearLayoutManager = LinearLayoutManager(view1.context)
        otherArticles.layoutManager = linearLayoutManager3
        otherArticles.adapter = HomeFollowingArticleAdapters(articles)


        val networkUtil = GraphQlUtil()
        networkUtil.getAllArticles()


        return view1
    }
}