package com.example.weather.model

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    val name: String,
    @SerializedName("main")
    val main: MainData,
    @SerializedName("weather")
    val weather: List<Weather>
)