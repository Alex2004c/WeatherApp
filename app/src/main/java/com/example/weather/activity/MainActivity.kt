package com.example.weather.activity

import android.os.Bundle
import android.view.WindowManager
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
import com.example.weather.model.HourlyModel
import com.example.weather.model.WeatherResponse
import retrofit2.Call

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

/*        initRecyclerviewHourly()
        initRecyclerviewOtherCity()*/

        setupAdapters()

       fetchCurrentCityWeather()
       fetchOtherCitiesWeather()
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

    private fun fetchCurrentCityWeather() {
        val apiKey = "6eef7a036bd4da72c7ead398dfb6774d"

        RetrofitClient.apiService.getWeather(mainCity, apiKey).enqueue(object :
            retrofit2.Callback<WeatherResponse> {
            override fun onResponse(
                call: Call<WeatherResponse>,
                response: retrofit2.Response<WeatherResponse>
            ) {
                if (response.isSuccessful) {
                    val data = response.body() ?: return
                    val temp = (data.main.temp).toInt()
                    val icon = data.weather.firstOrNull()?.icon ?: "01d"

                    // Добавляем один элемент (можно расширить на несколько часов позже)
                    hourlyList.clear()
                    hourlyList.add(HourlyModel("Now", temp, icon))
                    hourlyAdapter.notifyDataSetChanged()
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                // Обработка ошибок
            }
        })
    }

    private fun fetchOtherCitiesWeather() {
        val apiKey = "6eef7a036bd4da72c7ead398dfb6774d"

        for (city in otherCities) {
            RetrofitClient.apiService.getWeather(city, apiKey).enqueue(object :
                retrofit2.Callback<WeatherResponse> {
                override fun onResponse(
                    call: Call<WeatherResponse>,
                    response: retrofit2.Response<WeatherResponse>
                ) {
                    if (response.isSuccessful) {
                        val data = response.body() ?: return
                        val temp = (data.main.temp).toInt()
                        val icon = data.weather.firstOrNull()?.icon ?: "01d"

                        // Добавляем в список и обновляем адаптер
                        otherCityList.add(CityModel(city, temp, icon, 0, 0, 0))
                        otherCityAdapter.notifyDataSetChanged()
                    }
                }

                override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                    // Можно показать Toast или Snackbar
                }
            })
        }
    }


    private fun initRecyclerviewOtherCity() {
        val items: ArrayList<CityModel> = ArrayList()
        items.add(CityModel("Paris",28,"cloudy",12,20,30))
        items.add(CityModel("Berlin",29,"sunny",5,22,12))
        items.add(CityModel("Rome",30,"windy",30,25,50))
        items.add(CityModel("London",31,"cloudy_2",20,20,35))
        items.add(CityModel("NewYork",10,"snowy",8,5,7))

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