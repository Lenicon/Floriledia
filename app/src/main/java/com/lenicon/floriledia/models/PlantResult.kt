package com.lenicon.floriledia.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PlantResult(
    val id: String = System.currentTimeMillis().toString(),
    var nickname: String,
    var notes: String = "",
    var imagePaths: List<String>,
    val scientificName: String,
    val authorship: String,
    val family: String,
    val commonNames: List<String>,
    var wikiSummary: String = "",
    var wikiImageURL: String = ""
) : Parcelable