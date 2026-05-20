package com.lenicon.floriledia.models

import java.io.Serializable

data class PlantResult(
    val id: String = System.currentTimeMillis().toString(),
    var nickname: String,
    var notes: String = "",
    val imagePaths: List<String>,
    val scientificName: String,
    val authorship: String,
    val family: String,
    val commonNames: List<String>,
    var wikiSummary: String = "",
    var wikiImageURL: String = ""
) : Serializable