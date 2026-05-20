package com.lenicon.floriledia.models

import java.io.Serializable

data class PlantPhoto(
    val path: String,
    val organ: String
) : Serializable