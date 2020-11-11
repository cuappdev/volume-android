package com.example.volume_android.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.volume_android.R

class SavedPublicationsFragment: Fragment() {

    companion object{
        fun newInstance(): SavedPublicationsFragment = SavedPublicationsFragment()
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.saved_fragment, container, false)
    }
}