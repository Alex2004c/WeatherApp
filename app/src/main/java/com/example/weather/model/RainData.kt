package com.example.weather.model

import com.google.gson.annotations.SerializedName

data class RainData(
    @SerializedName("1h")
    val last1Hour: Double
)
