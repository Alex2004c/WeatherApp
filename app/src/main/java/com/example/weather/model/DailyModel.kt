package com.example.weather.model

data class DailyModel(
    val date: String,
    val tempMin: Int,
    val tempMax: Int,
    val icon: String,
    val weatherDesc: String,
    val hourlyList: List<HourlyModel>,
    val windSpeed: Double,
    val humidity: Int,
    val uvi: Int,
    val sunrise: Long,
    val sunset: Long,
    var isExpanded: Boolean = false
)