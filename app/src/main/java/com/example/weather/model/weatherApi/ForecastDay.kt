package com.example.weather.model.weatherApi

data class ForecastDay(
    val date: String,
    val hour: List<Hourly>,
    val day: DayCondition,
    val astro: DayAstro
)