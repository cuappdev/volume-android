package com.cornellappdev.volume.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.cornellappdev.volume.R
import com.cornellappdev.volume.databinding.FragmentDebriefMainBinding
import com.cornellappdev.volume.models.WeeklyDebrief
import com.cornellappdev.volume.util.GraphQlUtil
import com.cornellappdev.volume.util.PrefUtils
import io.reactivex.disposables.CompositeDisposable
import java.text.SimpleDateFormat
import java.util.*

class WeeklyDebriefIntroFragment(private val weeklyDebrief: WeeklyDebrief) : Fragment() {

    private lateinit var prefUtils: PrefUtils
    private lateinit var disposables: CompositeDisposable
    private lateinit var graphQlUtil: GraphQlUtil
    private var _binding: FragmentDebriefMainBinding? = null
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
        _binding = FragmentDebriefMainBinding.inflate(inflater, container, false)
        disposables = CompositeDisposable()
        graphQlUtil = GraphQlUtil()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val createdAtDate = Date(weeklyDebrief.createdAt)
        val expiration = Date(weeklyDebrief.expiration)
        val formatter = SimpleDateFormat("MM/dd")

        binding.tvYourDebrief.text = resources.getString(
            R.string.your_weekly_debrief,
            formatter.format(createdAtDate),
            formatter.format(expiration)
        )
        binding.tvNumberArticlesRead.text =
            resources.getString(R.string.number_read_articles, weeklyDebrief.numReadArticles)
        binding.tvNumberArticlesShouted.text =
            resources.getString(R.string.number_shouted_articles, weeklyDebrief.numShoutouts)
        binding.tvNumberArticlesBookmarked.text =
            resources.getString(R.string.number_bookmarked_articles, weeklyDebrief.numBookmarkedArticles)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}