package com.cornellappdev.volume.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Social(
        val social: String,
        val URL: String
) : Parcelable