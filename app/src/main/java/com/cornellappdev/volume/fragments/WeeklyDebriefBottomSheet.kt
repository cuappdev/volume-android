package com.cornellappdev.volume.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cornellappdev.volume.adapters.WeeklyDebriefPagerAdapter
import com.cornellappdev.volume.databinding.FragmentWeeklyDebriefBinding
import com.cornellappdev.volume.models.WeeklyDebrief
import com.cornellappdev.volume.util.GraphQlUtil
import com.cornellappdev.volume.util.PrefUtils
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.reactivex.disposables.CompositeDisposable

class WeeklyDebriefBottomSheet(val weeklyDebrief: WeeklyDebrief) : BottomSheetDialogFragment() {

    private lateinit var prefUtils: PrefUtils
    private lateinit var disposables: CompositeDisposable
    private lateinit var graphQlUtil: GraphQlUtil
    private var _binding: FragmentWeeklyDebriefBinding? = null
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
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentWeeklyDebriefBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        graphQlUtil = GraphQlUtil()
        disposables = CompositeDisposable()

        binding.vpFragments.isUserInputEnabled = true
        binding.vpFragments.adapter = WeeklyDebriefPagerAdapter(requireActivity(), 2 + weeklyDebrief.readArticles.size + weeklyDebrief.randomArticles.size, weeklyDebrief) { this.dismiss() }
        binding.dotsIndicator.setViewPager2(binding.vpFragments)
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