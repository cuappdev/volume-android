package com.example.volume_android.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Social(
        val social: String? = "",
        val url: String? = "",
): Parcelable