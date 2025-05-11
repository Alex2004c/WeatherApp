package com.example.weather.model.weatherApi

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    // Текущая погода
    @GET("current.json")
    fun getCurrentWeather(
        @Query("q") city: String,
        @Query("key") apiKey: String,
        @Query("lang") lang: String = "ru",
        @Query("aqi") aqi: Boolean = false
    ): Call<ForecastResponse>

    // Прогноз на 2 дня
    @GET("forecast.json")
    fun getTwoDaysForecast(
        @Query("q") city: String,
        @Query("key") apiKey: String,
        @Query("lang") lang: String = "ru",
        @Query("days") days: Int = 2,
        @Query("aqi") aqi: Boolean = false,
        @Query("alerts") alerts: Boolean = false
    ): Call<ForecastResponse>

    // Прогноз на 10 дней
    @GET("forecast.json")
    fun getTenDaysForecast(
        @Query("q") city: String,
        @Query("key") apiKey: String,
        @Query("lang") lang: String = "ru",
        @Query("days") days: Int = 10,
        @Query("aqi") aqi: Boolean = false,
        @Query("alerts") alerts: Boolean = false
    ): Call<ForecastResponse>

    // Поиск города
    @GET("search.json")
    fun searchCity(
        @Query("key") apiKey: String,
        @Query("q") query: String,
        @Query("lang") lang: String = "ru"
    ): Call<List<Location>>

    @GET("search.json")
    fun searchCitiesByCoordinates(
        @Query("key") apiKey: String,
        @Query("q") lat: Double,
        @Query("q") lon: Double
    ): Call<List<Location>>

    @GET("ip.json")
    fun getLocationByIP(@Query("key") apiKey: String, @Query("q") q: String = "auto:ip"
    ): Call<LocationResponse>

    // Астрономия (восход/закат)
    @GET("astronomy.json")
    fun getAstronomy(
        @Query("key") apiKey: String,
        @Query("q") city: String,
        @Query("lang") lang: String = "ru"
    ): Call<ForecastResponse>
}