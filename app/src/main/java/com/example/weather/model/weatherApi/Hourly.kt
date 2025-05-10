package com.example.weather.model.weatherApi

data class Hourly(
    val time: String,
    val temp_c: Double,
    val is_day: Int,
    val condition: CurrentCondition,
    val cloud: Int
)