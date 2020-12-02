package com.example.volume_android.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.volume_android.R

class OnboardingFragHolder: Fragment() {

    companion object{
        fun newInstance(): OnboardingFragHolder = OnboardingFragHolder()
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.onboarding_holder, container, false)
    }
}