package com.example.weather.model.weatherApi

data class ForecastResponse(
    val location: Location,
    val current: CurrentWeather,
    val forecast: ForecastData
)

