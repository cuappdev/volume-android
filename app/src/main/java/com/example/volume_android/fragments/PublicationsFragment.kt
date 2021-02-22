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


class PublicationsFragment : Fragment() {

    private lateinit var followpublicationRV : RecyclerView
    private lateinit var morepublicationRV: RecyclerView
    val graphQlUtil = GraphQlUtil()
    val disposables = CompositeDisposable()
    val prefUtils: PrefUtils = PrefUtils()
    private lateinit var view1: View
    private var shouldRefreshOnResume = false


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view =  inflater.inflate(R.layout.all_publications, container, false)

        getFollowingPublications(view)
        getMorePublications(view)
        view1 = view
        return view
    }

    fun getMorePublications(view: View){
        val otherObs = graphQlUtil.getAllPublications().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        disposables.add(otherObs.subscribe { response ->
            val others = mutableListOf<Publication>()
            response.data?.getAllPublications?.mapTo(others, { it ->
                Publication(id = it.id, backgroundImageURL = it.backgroundImageURL, bio = it.bio, name = it.name, profileImageURL = it.profileImageURL, rssName = it.rssName, rssURL = it.rssURL, slug = it.slug, shoutouts = it.shoutouts, websiteURL = it.websiteURL,
                        mostRecentArticle = Article(it.mostRecentArticle?.id, it.mostRecentArticle?.title, it.mostRecentArticle?.articleURL, it.mostRecentArticle?.imageURL))
            })

            morepublicationRV = view.findViewById(R.id.follwing_more_publications_rv)
            morepublicationRV.adapter = FollowPublicationsAdapter(others, view.context)
            morepublicationRV.layoutManager = LinearLayoutManager(view.context)
            morepublicationRV.setHasFixedSize(true)

        })
    }

    fun getFollowingPublications(view: View){

        val followingPublicationsIds = prefUtils.getStringSet("following", mutableSetOf())
        val followingPublications = mutableListOf<Publication>()

        for ( pub in followingPublicationsIds!!){
            val followingObs = graphQlUtil.getPublicationById(pub).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            disposables.add(followingObs.subscribe {
                val res = it.data?.getPublicationByID
                if(res != null) {
                    val publication = Publication(res.id, res.backgroundImageURL,
                            res.bio, res.name, res.profileImageURL, res.rssName, res.rssURL, res.slug, res.shoutouts, res.websiteURL, Article(res.mostRecentArticle?.id, res.mostRecentArticle?.title, res.mostRecentArticle?.articleURL, res.mostRecentArticle?.imageURL))

                    followingPublications.add(publication)
                }

                if (pub == followingPublicationsIds.last()) {
                    followpublicationRV = view.findViewById(R.id.following_all_publications_rv)
                    followpublicationRV.adapter = FollowingHorizontalAdapter(followingPublications)
                    val linearLayoutManager = LinearLayoutManager(view.context)
                    linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
                    followpublicationRV.layoutManager = linearLayoutManager
                    followpublicationRV.setHasFixedSize(true)

                }
            })
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            fragmentManager!!.beginTransaction().detach(this).attach(this).commit()
        }
    }

    override fun onResume() {
        super.onResume()
        getMorePublications(view1)
        getFollowingPublications(view1)
    }

}