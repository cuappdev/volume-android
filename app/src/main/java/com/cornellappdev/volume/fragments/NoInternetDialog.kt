package com.cornellappdev.volume.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.cornellappdev.volume.databinding.FragmentNoInternetBinding

/**
 * Dialog for displaying that the user has no internet.
 *
 * @see {@link com.cornellappdev.volume.R.layout#no_internet}
 */
class NoInternetDialog : DialogFragment() {

    private var _binding: FragmentNoInternetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentNoInternetBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        const val TAG = "no_internet_dialog"
    }
}