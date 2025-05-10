package com.example.weather.model.weatherApi

data class CityModel(
    val name: String,
    val temp: Int,
    val icon: String,
    val windKph: Double,
    val humidity: Int,
    val rainMm: Double
)