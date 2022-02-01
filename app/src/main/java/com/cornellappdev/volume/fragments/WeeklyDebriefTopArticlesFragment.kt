package com.cornellappdev.volume.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.cornellappdev.volume.R
import com.cornellappdev.volume.databinding.FragmentDebriefCheckoutBinding
import com.cornellappdev.volume.databinding.FragmentDebriefMainBinding
import com.cornellappdev.volume.databinding.FragmentHomeBinding
import com.cornellappdev.volume.models.Article
import com.cornellappdev.volume.models.WeeklyDebrief
import com.cornellappdev.volume.util.ActivityForResultConstants
import com.cornellappdev.volume.util.GraphQlUtil
import com.cornellappdev.volume.util.PrefUtils
import com.google.gson.Gson
import io.reactivex.disposables.CompositeDisposable
import java.util.*

class WeeklyDebriefTopArticlesFragment(private val article: Article) : Fragment() {

    private lateinit var prefUtils: PrefUtils
    private lateinit var disposables: CompositeDisposable
    private lateinit var graphQlUtil: GraphQlUtil
    private var _binding: FragmentDebriefCheckoutBinding? = null
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
        _binding = FragmentDebriefCheckoutBinding.inflate(inflater, container, false)
        disposables = CompositeDisposable()
        graphQlUtil = GraphQlUtil()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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