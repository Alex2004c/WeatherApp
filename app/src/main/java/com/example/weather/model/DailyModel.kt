package com.example.weather.model

data class DailyModel(
    val date: String,
    val tempMin: Int,
    val tempMax: Int,
    val icon: String,
    val weatherDesc: String,
    val hourlyList: List<ForecastItem>,
    var isExpanded: Boolean = false
)