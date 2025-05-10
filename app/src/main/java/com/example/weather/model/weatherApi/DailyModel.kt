package com.example.weather.model.weatherApi

data class DailyModel(
    val date: String,
    val tempMin: Int,
    val tempMax: Int,
    val icon: String,
    val weatherDesc: String,
    val sunrise: String,
    val sunset: String,
    val humidity: Int,
    val uv: Double,
    val rainChance: Int,
    val hourlyList: List<Hourly>,
    var isExpanded: Boolean = false
)