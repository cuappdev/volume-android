package com.cornellappdev.volume.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cornellappdev.volume.R
import com.cornellappdev.volume.adapters.FollowingHorizontalAdapter
import com.cornellappdev.volume.adapters.MorePublicationsAdapter
import com.cornellappdev.volume.databinding.FragmentPublicationsBinding
import com.cornellappdev.volume.models.Article
import com.cornellappdev.volume.models.Publication
import com.cornellappdev.volume.models.Social
import com.cornellappdev.volume.util.GraphQlUtil
import com.cornellappdev.volume.util.GraphQlUtil.Companion.hasInternetConnection
import com.cornellappdev.volume.util.PrefUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers


class PublicationsFragment : Fragment() {

    private lateinit var followpublicationRV: RecyclerView
    private lateinit var morepublicationRV: RecyclerView
    private val graphQlUtil = GraphQlUtil()
    private val disposables = CompositeDisposable()
    private val prefUtils = PrefUtils()
    private var _binding: FragmentPublicationsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentPublicationsBinding.inflate(inflater, container, false)
        val volumeOrange: Int? = context?.let { ContextCompat.getColor(it, R.color.volumeOrange) }
        if (volumeOrange != null) {
            binding.srlQuery.setColorSchemeColors(volumeOrange, volumeOrange, volumeOrange)
        }
        binding.srlQuery.setOnRefreshListener {
            getFollowingPublications(binding,
                    isRefreshing = this::followpublicationRV.isInitialized)
            if (!this::morepublicationRV.isInitialized) {
                getMorePublications(binding)
            }
            binding.srlQuery.isRefreshing = false
        }

        disposables.add(hasInternetConnection().subscribe { hasInternet ->
            if (hasInternet) {
                getFollowingPublications(binding, isRefreshing = false)
                getMorePublications(binding)
            } else {
                binding.clPublicationPage.visibility = View.GONE
                val ft = childFragmentManager.beginTransaction()
                val dialog = NoInternetDialog()
                ft.replace(binding.fragmentContainer.id, dialog, NoInternetDialog.TAG).commit()
            }
        })
        return binding.root
    }

    private fun getMorePublications(binding: FragmentPublicationsBinding) {
        val moreObs = graphQlUtil
                .getAllPublications()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        disposables.add(hasInternetConnection().subscribe { hasInternet ->
            if (hasInternet) {
                childFragmentManager.findFragmentByTag(NoInternetDialog.TAG).let { dialogFrag ->
                    (dialogFrag as? DialogFragment)?.dismiss()
                }
                binding.clPublicationPage.visibility = View.VISIBLE
                disposables.add(moreObs.subscribe { response ->
                    val morePublications = mutableListOf<Publication>()
                    val publications = response.data?.getAllPublications
                    if (publications != null) {
                        publications.mapTo(morePublications, { publication ->
                            val article = publication.mostRecentArticle
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
                                    article?.let {
                                        Article(
                                                title = article.title,
                                                articleURL = article.articleURL,
                                                date = article.date.toString(),
                                                id = article.id,
                                                imageURL = article.imageURL,
                                                shoutouts = article.shoutouts,
                                                nsfw = article.nsfw)
                                    },
                                    publication.socials.toList().map { Social(it.social, it.uRL) })
                        })
                        morepublicationRV = binding.rvMorePublications
                        morepublicationRV.adapter =
                                MorePublicationsAdapter(morePublications, prefUtils, null)
                        morepublicationRV.layoutManager = LinearLayoutManager(context)
                        morepublicationRV.setHasFixedSize(true)
                    }
                })
            } else {
                if (childFragmentManager.findFragmentByTag(NoInternetDialog.TAG) == null) {
                    binding.clPublicationPage.visibility = View.GONE
                    val ft = childFragmentManager.beginTransaction()
                    val dialog = NoInternetDialog()
                    ft.replace(binding.fragmentContainer.id, dialog, NoInternetDialog.TAG).commit()
                }
            }
        })
    }

    private fun getFollowingPublications(binding: FragmentPublicationsBinding, isRefreshing: Boolean) {
        val followingPublicationsIDs =
                prefUtils.getStringSet(PrefUtils.FOLLOWING_KEY, mutableSetOf())?.toMutableList()
        val followingObs = followingPublicationsIDs?.let {
            graphQlUtil
                    .getPublicationsByIDs(it)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
        }
        if (followingObs != null) {
            disposables.add(hasInternetConnection().subscribe { hasInternet ->
                if (hasInternet) {
                    childFragmentManager.findFragmentByTag(NoInternetDialog.TAG).let { dialogFrag ->
                        (dialogFrag as? DialogFragment)?.dismiss()
                    }
                    binding.clPublicationPage.visibility = View.VISIBLE
                    disposables.add(followingObs.subscribe { response ->
                        val followingPublications = mutableListOf<Publication>()
                        val publications = response.data?.getPublicationsByIDs
                        if (publications != null) {
                            publications.mapTo(followingPublications, { publication ->
                                val article = publication.mostRecentArticle
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
                                        article?.let {
                                            Article(
                                                    title = article.title,
                                                    articleURL = article.articleURL,
                                                    date = article.date.toString(),
                                                    id = article.id,
                                                    imageURL = article.imageURL,
                                                    shoutouts = article.shoutouts,
                                                    nsfw = article.nsfw)
                                        },
                                        publication.socials.toList().map { Social(it.social, it.uRL) })
                            })
                            if (!isRefreshing) {
                                followpublicationRV = binding.rvFollowing
                                followpublicationRV.adapter = FollowingHorizontalAdapter(followingPublications)
                                val linearLayoutManager = LinearLayoutManager(context)
                                linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
                                followpublicationRV.layoutManager = linearLayoutManager
                                followpublicationRV.setHasFixedSize(true)
                            } else {
                                val adapter = followpublicationRV.adapter as FollowingHorizontalAdapter
                                adapter.clear()
                                adapter.addAll(followingPublications)
                            }
                        }
                    })
                } else {
                    if (childFragmentManager.findFragmentByTag(NoInternetDialog.TAG) == null) {
                        binding.clPublicationPage.visibility = View.GONE
                        val ft = childFragmentManager.beginTransaction()
                        val dialog = NoInternetDialog()
                        ft.replace(binding.fragmentContainer.id, dialog, NoInternetDialog.TAG).commit()
                    }
                }
            })
        }
        if (followingPublicationsIDs?.isEmpty() == true) {
            binding.groupNotFollowing.visibility = View.VISIBLE
        } else {
            binding.groupNotFollowing.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        binding.let {
            getFollowingPublications(it, isRefreshing = this::followpublicationRV.isInitialized)
            getMorePublications(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}