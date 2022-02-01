package com.cornellappdev.volume.fragments

import android.app.Fragment
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.cornellappdev.volume.MainActivity
import com.cornellappdev.volume.R
import com.cornellappdev.volume.analytics.NavigationSource
import com.cornellappdev.volume.analytics.NavigationSource.Companion.putParcelableExtra
import com.cornellappdev.volume.databinding.FragmentDebriefMainBinding
import com.cornellappdev.volume.databinding.FragmentDebriefShareBinding
import com.cornellappdev.volume.databinding.FragmentHomeBinding
import com.cornellappdev.volume.models.Article
import com.cornellappdev.volume.models.WeeklyDebrief
import com.cornellappdev.volume.util.ActivityForResultConstants
import com.cornellappdev.volume.util.GraphQlUtil
import com.cornellappdev.volume.util.PrefUtils
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import io.reactivex.disposables.CompositeDisposable
import java.util.*

class WeeklyDebriefShareFragment(private val article: Article) : Fragment(), View.OnClickListener {

    private lateinit var prefUtils: PrefUtils
    private lateinit var disposables: CompositeDisposable
    private lateinit var graphQlUtil: GraphQlUtil
    private var _binding: FragmentDebriefShareBinding? = null
    private val binding get() = _binding!!

    override fun onAttach(context: Context) {
        super.onAttach(context)
        prefUtils = PrefUtils(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDebriefShareBinding.inflate(inflater, container, false)
        disposables = CompositeDisposable()
        graphQlUtil = GraphQlUtil()
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Picasso.get().load(article.imageURL).fit().centerCrop()
            .into(binding.ivArticleImage)
        context?.let { Article.setCorrectDateText(article, binding.tvTimePosted, it) }
        binding.tvShoutoutCount.text =
            context?.resources?.getQuantityString(
                R.plurals.shoutout_count,
                article.shoutouts.toInt(),
                article.shoutouts.toInt()
            )
        binding.tvArticleTitle.text = article.title
        binding.tvPublicationName.text = article.publication?.name
        binding.ivArticleImage.setOnClickListener(this)
        binding.tvPublicationName.setOnClickListener(this)
        binding.tvArticleTitle.setOnClickListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClick(p0: View?) {
        val intent = Intent(activity, MainActivity::class.java)
        intent.putExtra(Article.INTENT_KEY, article)
//        intent.putParcelableExtra(
//            NavigationSource.INTENT_KEY,
//            NavigationSource.TRENDING_ARTICLES
//        )
        startActivity(intent)
    }
}
