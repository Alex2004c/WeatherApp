package com.example.weather.model.weatherApi

data class DayCondition(
    val mintemp_c: Double,
    val maxtemp_c: Double,
    val avghumidity: Int,
    val uv: Double,
    val daily_chance_of_rain: Int,
    val condition: CurrentCondition
)