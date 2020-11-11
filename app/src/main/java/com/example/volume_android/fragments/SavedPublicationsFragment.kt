package com.example.volume_android.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.volume_android.MainActivity
import com.example.volume_android.R

class SavedPublicationsFragment: Fragment() {

    private lateinit var toArticleButton: Button

    companion object{
        fun newInstance(): SavedPublicationsFragment = SavedPublicationsFragment()
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.saved_fragment, container, false)
        toArticleButton = view?.findViewById(R.id.article_view)!!
        toArticleButton.setOnClickListener {
            val intent = Intent(activity, MainActivity::class.java)
            activity?.startActivity(intent)
        }
        return view
    }
}