package com.example.volume_android.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.volume_android.OnboardingFragHolder
import com.example.volume_android.R

class HomeFragment : Fragment() {

    lateinit var button: Button

    companion object {
        fun newInstance(): HomeFragment = HomeFragment()
        private const val TAG = "HomeFragment"
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        Log.d("HOMEFRAGMENT", "oncreateview")
        val view1 = inflater.inflate(R.layout.home_fragment, container, false)
        button = view1?.findViewById(R.id.button12)!!
        button.setOnClickListener {
            Log.d(TAG,"button clicked")
        }

        return view1
    }
}