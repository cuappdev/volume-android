package com.cornellappdev.volume.fragments

import PrefUtils
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cornellappdev.volume.R
import com.cornellappdev.volume.adapters.FollowPublicationsAdapter
import com.cornellappdev.volume.adapters.FollowingHorizontalAdapter
import com.cornellappdev.volume.models.Article
import com.cornellappdev.volume.models.Publication
import com.cornellappdev.volume.util.GraphQlUtil
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


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view =  inflater.inflate(R.layout.all_publications, container, false)
        getFollowingPublications(view)
        getMorePublications(view)
        view1 = view
        return view
    }

    fun getMorePublications(view: View) {
        val moreObs = graphQlUtil
                .getAllPublications()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        disposables.add(moreObs.subscribe { response ->
            val morePublications = mutableListOf<Publication>()
            val publications = response.data?.getAllPublications
            if(publications != null) {
                publications.mapTo(morePublications, { publication ->
                    Publication(
                            publication.id,
                            publication.backgroundImageURL,
                            publication.bio,
                            publication.name,
                            publication.profileImageURL,
                            publication.rssName,
                            publication.rssURL,
                            publication.slug,
                            publication.shoutouts,
                            publication.websiteURL,
                            Article(
                                    publication.mostRecentArticle?.id,
                                    publication.mostRecentArticle?.title,
                                    publication.mostRecentArticle?.articleURL,
                                    publication.mostRecentArticle?.imageURL,
                                    nsfw = publication.mostRecentArticle?.nsfw))
                })
                morepublicationRV = view.findViewById(R.id.follwing_more_publications_rv)
                morepublicationRV.adapter = FollowPublicationsAdapter(morePublications, view.context)
                morepublicationRV.layoutManager = LinearLayoutManager(view.context)
                morepublicationRV.setHasFixedSize(true)
            }
        })
    }

    fun getFollowingPublications(view: View){
        val followingPublicationsIDs =
                prefUtils.getStringSet("following", mutableSetOf())?.toMutableList()
        val followingObs = followingPublicationsIDs?.let {
            graphQlUtil
                .getPublicationsByIDs(it)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        }
        if (followingObs != null) {
            disposables.add(followingObs.subscribe { response ->
                val followingPublications = mutableListOf<Publication>()
                val publications = response.data?.getPublicationsByIDs
                if(publications != null) {
                    publications.mapTo(followingPublications, { publication ->
                        Publication(
                                publication.id,
                                publication.backgroundImageURL,
                                publication.bio,
                                publication.name,
                                publication.profileImageURL,
                                publication.rssName,
                                publication.rssURL,
                                publication.slug,
                                publication.shoutouts,
                                publication.websiteURL,
                                Article(
                                        publication.mostRecentArticle?.id,
                                        publication.mostRecentArticle?.title,
                                        publication.mostRecentArticle?.articleURL,
                                        publication.mostRecentArticle?.imageURL))
                    })
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

    override fun onResume() {
        super.onResume()
        getMorePublications(view1)
        getFollowingPublications(view1)
    }

}