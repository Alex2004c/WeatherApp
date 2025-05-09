package com.example.weather.model

import com.google.gson.annotations.SerializedName

data class ForecastItem(
    val dt: Long,
    @SerializedName("main")
    val mainData: MainDataForecast,
    val weather: List<Weather>
)