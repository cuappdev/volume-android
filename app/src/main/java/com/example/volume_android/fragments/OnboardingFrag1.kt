package com.example.volume_android.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.volume_android.R

class OnboardingFrag1 : Fragment() {

    companion object {
        fun newInstance(): OnboardingFrag1 = OnboardingFrag1()
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.onboardingfrag1, container, false)
    }
}