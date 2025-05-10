package com.example.weather.model.weatherApi

data class CurrentWeather(
    val last_updated: String,
    val temp_c: Double,
    val is_day: Int,
    val condition: CurrentCondition,
    val wind_kph: Double,
    val pressure_mb: Double,      // Давление в миллибарах
    val precip_mm: Double,        // Количество осадков в мм
    val humidity: Int,
    val feelslike_c: Double,
    val uv: Double,
    val gust_kph: Double
)
