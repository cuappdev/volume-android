package com.cornellappdev.volume.fragments

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.cornellappdev.volume.R
import com.cornellappdev.volume.databinding.FragmentNoInternetBinding
import com.cornellappdev.volume.util.GraphQlUtil.Companion.hasInternetConnection
import io.reactivex.disposables.CompositeDisposable


class NoInternetDialog : DialogFragment() {

    private var _binding: FragmentNoInternetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentNoInternetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
//        while (true) {
//            disposables.add(hasInternetConnection().subscribe { hasInternet ->
//                if (hasInternet) {
//                    Log.d("hi", "hi")
//                } else {
//                    Log.d("no", "no")
//                }
//            })
//        }

//        val dialog: Dialog? = dialog
//        if (dialog != null) {
//            val width = ViewGroup.LayoutParams.MATCH_PARENT
//            val height = ViewGroup.LayoutParams.MATCH_PARENT
//            dialog.window?.setLayout(width, height)
////            dialog.getWindow().setWindowAnimations(R.style.AppTheme_Slide)
//        }
//        while (true) {
//            disposables.add(hasInternetConnection().subscribe { hasInternet ->
//                if (hasInternet) {
//                    Log.d("hi", "hi")
//                } else {
//                    Log.d("no", "no")
//                }
//            })
//        }
    }

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        while (true) {
//            disposables.add(hasInternetConnection().subscribe { hasInternet ->
//                if (hasInternet) {
//                    Log.d("hi", "hi")
//                } else {
//                    Log.d("no", "no")
//                }
//            })
//        }
//    }

    companion object {
        const val TAG = "no_internet_dialog"
    }
}