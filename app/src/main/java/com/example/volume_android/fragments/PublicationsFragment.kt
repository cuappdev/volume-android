package com.example.volume_android.fragments

import PrefUtils
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
import com.example.volume_android.models.Article
import com.example.volume_android.models.Publication
import com.example.volume_android.util.GraphQlUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class PublicationsFragment(val publications: List<Publication>) : Fragment() {

    private lateinit var followpublicationRV : RecyclerView
    private lateinit var morepublicationRV: RecyclerView
    val graphQlUtil = GraphQlUtil()
    val disposables = CompositeDisposable()
    val prefUtils: PrefUtils = PrefUtils()

    //val followingPublications = prefUtils.getStringSet("following", mutableSetOf())


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


        getMorePublications(view)


        return view
    }

    fun getMorePublications(view: View){
        val otherObs = graphQlUtil.getAllPublications().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        disposables.add(otherObs.subscribe{
            var others = mutableListOf<Publication>()

            it.data?.getAllPublications?.mapTo(others, { it -> Publication(id = it.id, backgroundImageURL = it.backgroundImageURL, bio = it.bio, name = it.name, profileImageURL = it.profileImageURL, rssName = it.rssName, rssURL = it.rssURL, slug = it.slug, shoutouts = it.shoutouts, websiteURL = it.websiteURL) })

            morepublicationRV = view.findViewById(R.id.follwing_more_publications_rv)
            morepublicationRV.adapter = FollowPublicationsAdapter(others, view.context)
            morepublicationRV.layoutManager = LinearLayoutManager(view.context)
            morepublicationRV.setHasFixedSize(true)

        })
    }
}