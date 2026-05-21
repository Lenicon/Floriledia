package com.lenicon.floriledia.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PlantPhoto(
    val path: String,
    val organ: String
) : Parcelable