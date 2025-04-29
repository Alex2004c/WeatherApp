package com.example.weather.activity

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weather.R
import com.example.weather.RetrofitClient
import com.example.weather.adapter.HourlyAdapter
import com.example.weather.adapter.OtherCityAdapter
import com.example.weather.databinding.ActivityMainBinding
import com.example.weather.model.CityModel
import com.example.weather.model.ForecastResponse
import com.example.weather.model.HourlyModel
import com.example.weather.model.WeatherResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Списки данных
    private val hourlyList = ArrayList<HourlyModel>()
    private val otherCityList = ArrayList<CityModel>()

    // Адаптеры
    private lateinit var hourlyAdapter: HourlyAdapter
    private lateinit var otherCityAdapter: OtherCityAdapter

    // Города
    private val mainCity = "Minsk"
    private val otherCities = listOf("Gomel", "Brest", "Vitebsk", "Grodno", "Bobruisk")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.chipNavigator.setItemSelected(R.id.home, true)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        setupAdapters()
        fetchCurrentCityWeather(mainCity)
        fetchHourlyForecast(mainCity)
        fetchOtherCitiesWeather()

        binding.button.setOnClickListener {
            val cityName = binding.editTextText.text.toString().trim()
            if (cityName.isNotEmpty()) {
                fetchCurrentCityWeather(cityName)
                fetchHourlyForecast(cityName)
            } else {
                Toast.makeText(this, "Введите название города", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupAdapters() {
        // Hourly — горизонтальный список
        hourlyAdapter = HourlyAdapter(hourlyList)
        binding.view1.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.view1.adapter = hourlyAdapter

        // Cities — горизонтальный список
        otherCityAdapter = OtherCityAdapter(otherCityList)
        binding.view2.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.view2.adapter = otherCityAdapter
    }
    fun Long.toHour(): String {
        val date = Date(this * 1000L)
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(date)
    }

    private fun fetchHourlyForecast(cityName: String) {
        val apiKey = "6eef7a036bd4da72c7ead398dfb6774d"

        RetrofitClient.apiService.getHourlyForecast(cityName, apiKey).enqueue(object :
            Callback<ForecastResponse> {
            override fun onResponse(
                call: Call<ForecastResponse>,
                response: Response<ForecastResponse>
            ) {
                if (response.isSuccessful) {
                    val forecastList = response.body()?.list ?: return
                    Log.d("Forecast", "Received ${forecastList.size} items")

                    hourlyList.clear()
                    for (item in forecastList.take(9)) {
                        val hour = item.dt.toHour()
                        val temp = item.mainData.temp.toInt()
                        val icon = item.weather.firstOrNull()?.icon ?: "01d"

                        hourlyList.add(HourlyModel(hour, temp, icon))
                    }

                    hourlyAdapter.notifyDataSetChanged()
                }
            }

            override fun onFailure(call: Call<ForecastResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Ошибка загрузки прогноза", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchCurrentCityWeather(cityName: String) {
        val apiKey = "6eef7a036bd4da72c7ead398dfb6774d"

        RetrofitClient.apiService.getWeather(cityName, apiKey).enqueue(object :
            Callback<WeatherResponse> {
            override fun onResponse(
                call: Call<WeatherResponse>,
                response: Response<WeatherResponse>
            ) {
                if (response.isSuccessful) {
                    val data = response.body() ?: return
                    val temp = (data.main.temp).toInt()
                    val icon = data.weather.firstOrNull()?.icon ?: "01d"
                    val windSpeed = data.wind.speed.toInt()
                    val humidity = data.main.humidity
                    val rain = data.rain?.last1Hour ?: 0.0
                    updateMainWeatherUI(icon, temp, windSpeed, humidity, rain)
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Ошибка загрузки прогноза", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun updateMainWeatherUI(iconCode: String, temp: Int, wind: Int, humidity: Int, rain: Double) {
        // Обновляем картинку
        val iconResourceName = getWeatherIcon(iconCode)
        val drawableId = resources.getIdentifier(iconResourceName, "drawable", packageName)
        binding.imageViewMain.setImageResource(drawableId)

        // Обновляем тексты
        binding.tvTempMain.text = "$temp°C"
        binding.tvWindSpeed.text = "$wind m/s"
        binding.tvHumidity.text = "$humidity%"
        binding.tvRain.text = "$rain mm"
    }

    private fun getWeatherIcon(iconCode: String): String {
        return when (iconCode.take(2)) {
            "01" -> "sunny"
            "02" -> "cloudy_2"
            "03", "04" -> "cloudy"
            "09", "10" -> "rainy"
            "11" -> "windy"
            "13" -> "snowy"
            "50" -> "foggy"
            else -> "unknown"
        }
    }

    private fun fetchOtherCitiesWeather() {
        val apiKey = "6eef7a036bd4da72c7ead398dfb6774d"

        for (city in otherCities) {
            RetrofitClient.apiService.getWeather(city, apiKey).enqueue(object :
                Callback<WeatherResponse> {
                override fun onResponse(
                    call: Call<WeatherResponse>,
                    response: Response<WeatherResponse>
                ) {
                    if (response.isSuccessful) {
                        val data = response.body() ?: return
                        val temp = (data.main.temp).toInt()
                        val icon = data.weather.firstOrNull()?.icon ?: "01d"
                        val windSpeed = data.wind.speed.toInt()
                        val humidity = data.main.humidity
                        val rain = data.rain?.last1Hour ?: 0.0

                        otherCityList.add(CityModel(city, temp, icon, windSpeed, humidity, rain))
                        otherCityAdapter.notifyDataSetChanged()
                    }
                }

                override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                    Toast.makeText(this@MainActivity, "Ошибка загрузки прогноза", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }


    private fun initRecyclerviewOtherCity() {
        val items: ArrayList<CityModel> = ArrayList()
        items.add(CityModel("Paris",28,"cloudy",12,20,30.0))
        items.add(CityModel("Berlin",29,"sunny",5,22,12.0))
        items.add(CityModel("Rome",30,"windy",30,25,50.0))
        items.add(CityModel("London",31,"cloudy_2",20,20,35.0))
        items.add(CityModel("NewYork",10,"snowy",8,5,7.0))

        binding.view2.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.view2.adapter = OtherCityAdapter(items)
    }

    private fun initRecyclerviewHourly() {
        val items: ArrayList<HourlyModel> = ArrayList()
        items.add(HourlyModel("9 pm",28,"cloudy"))
        items.add(HourlyModel("10 pm",29,"sunny"))
        items.add(HourlyModel("11 pm",30,"windy"))
        items.add(HourlyModel("12 pm",31,"cloudy_2"))
        items.add(HourlyModel("1 am",10,"snowy"))

        binding.view1.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.view1.adapter = HourlyAdapter(items)
    }
}