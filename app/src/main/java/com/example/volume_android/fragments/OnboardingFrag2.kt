package com.example.volume_android.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.volume_android.R

class OnboardingFrag2: Fragment() {

    companion object{
        fun newInstance(): OnboardingFrag2 = OnboardingFrag2()
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.onboardingfrag2, container, false)
    }
}