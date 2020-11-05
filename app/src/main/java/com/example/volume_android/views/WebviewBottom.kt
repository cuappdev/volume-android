package com.example.volume_android.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.volume_android.R

class WebviewBottom @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet,
        defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.bottom_webview_actions, this, true)
    }

    fun minimize(b: Boolean){

        if (b){
            this.visibility = View.GONE
        }
        else{
            this.visibility = View.VISIBLE
        }
    }
}