package com.example.weather.activity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.weather.RetrofitClient
import com.example.weather.adapter.HourlyAdapter
import com.example.weather.adapter.OtherCityAdapter
import com.example.weather.adapter.SharedViewModel
import com.example.weather.databinding.FragmentTodayBinding
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

class TodayFragment : Fragment() {

    private var _binding: FragmentTodayBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: SharedViewModel by activityViewModels()

    private var lastCity: String? = "Minsk"

    // Списки данных
    private val hourlyList = ArrayList<HourlyModel>()
    private val otherCityList = ArrayList<CityModel>()

    // Адаптеры
    private lateinit var hourlyAdapter: HourlyAdapter
    private lateinit var otherCityAdapter: OtherCityAdapter

    // Города
    private val otherCities = listOf("Gomel", "Brest", "Vitebsk", "Grodno", "Bobruisk")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTodayBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.getString(ARG_CITY)?.let { city ->
            sharedViewModel.updateMainCity(city)
        }

        sharedViewModel.mainCity.observe(viewLifecycleOwner) { newCity ->
            if (newCity != lastCity) {
                fetchCurrentCityWeather(newCity) {
                    fetchHourlyForecast(newCity)
                    refreshOtherCityList()
                }
            }
        }

        setupAdapters()
        fetchCurrentCityWeather(sharedViewModel.mainCity.value) {
            fetchHourlyForecast(sharedViewModel.mainCity.value)
            fetchOtherCitiesWeather()
        }
    }

    companion object {
        private const val ARG_CITY = "city_name"

        fun newInstance(city: String): TodayFragment {
            val fragment = TodayFragment()
            val args = Bundle().apply {
                putString(ARG_CITY, city)
            }
            fragment.arguments = args
            return fragment
        }
    }

    private fun setupAdapters() {
        // Hourly — горизонтальный список
        hourlyAdapter = HourlyAdapter(hourlyList)
        binding.view1.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.view1.adapter = hourlyAdapter

        otherCityAdapter = OtherCityAdapter(requireContext(), otherCityList) { selectedCity ->
            if (selectedCity.cityName != sharedViewModel.mainCity.value) {
                sharedViewModel.updateMainCity(selectedCity.cityName)
            }
        }
        // Cities — горизонтальный список
        binding.view2.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.view2.adapter = otherCityAdapter
    }

    private fun refreshOtherCityList() {

        val newCityIndex = otherCityList.indexOfFirst { it.cityName == sharedViewModel.mainCity.value }

        if (newCityIndex != -1) {
            otherCityList.removeAt(newCityIndex)
        }

        if (!otherCityList.any { it.cityName == lastCity}) {
            fetchCityAndAddToOtherList(lastCity) {
                otherCityAdapter.notifyDataSetChanged()
            }
        } else {
            otherCityAdapter.notifyDataSetChanged()
        }

        lastCity = sharedViewModel.mainCity.value
    }

    private fun fetchCityAndAddToOtherList(cityName: String?, onSuccess: () -> Unit) {
        val apiKey = "6eef7a036bd4da72c7ead398dfb6774d"

        if (cityName != null) {
            RetrofitClient.apiService.getWeather(cityName, apiKey).enqueue(object :
                Callback<WeatherResponse> {
                override fun onResponse(
                    call: Call<WeatherResponse>,
                    response: Response<WeatherResponse>
                ) {
                    if (response.isSuccessful) {
                        val data = response.body() ?: return

                        val temp = data.main.temp.toInt()
                        val icon = data.weather.firstOrNull()?.icon ?: "01d"
                        val wind = data.wind.speed.toInt()
                        val humidity = data.main.humidity
                        val rain = data.rain?.last1Hour ?: 0.0

                        if (!otherCityList.any { it.cityName == cityName }) {
                            otherCityList.add(0, CityModel(cityName, temp, icon, wind, humidity, rain))
                        }
                        onSuccess()
                    }
                }

                override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                    // Обработка ошибок
                }
            })
        }
    }

    fun Long.toHour(): String {
        val date = Date(this * 1000L)
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(date)
    }

    private fun fetchHourlyForecast(cityName: String?) {
        val apiKey = "6eef7a036bd4da72c7ead398dfb6774d"

        if (cityName != null) {
            RetrofitClient.apiService.getHourlyForecast(cityName, apiKey).enqueue(object :
                Callback<ForecastResponse> {
                override fun onResponse(
                    call: Call<ForecastResponse>,
                    response: Response<ForecastResponse>
                ) {
                    if (response.isSuccessful) {
                        val forecastList = response.body()?.list ?: return

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
                    Toast.makeText(context, "Ошибка загрузки по 3ч прогноза", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun fetchCurrentCityWeather(cityName: String?, onSuccessCallback: () -> Unit) {
        val apiKey = "6eef7a036bd4da72c7ead398dfb6774d"

        if (cityName != null) {
            RetrofitClient.apiService.getWeather(cityName, apiKey).enqueue(object :
                Callback<WeatherResponse> {
                override fun onResponse(
                    call: Call<WeatherResponse>,
                    response: Response<WeatherResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val data = response.body() ?: return
                        val temp = (data.main.temp).toInt()
                        val icon = data.weather.firstOrNull()?.icon ?: "01d"
                        val windSpeed = data.wind.speed.toInt()
                        val humidity = data.main.humidity
                        val rain = data.rain?.last1Hour ?: 0.0
                        updateMainWeatherUI(icon, temp, windSpeed, humidity, rain)
                        onSuccessCallback()
                    } else {
                        Toast.makeText(context, "Город не найден", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                    Toast.makeText(context, "Ошибка загрузки главной погоды", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }


    private fun updateMainWeatherUI(iconCode: String, temp: Int, wind: Int, humidity: Int, rain: Double) {
        // Обновляем картинку
        /*        val iconResourceName = getWeatherIcon(iconCode)
                val drawableId = resources.getIdentifier(iconResourceName, "drawable", packageName)
                binding.imageViewMain.setImageResource(drawableId)*/
        binding.imageViewMain.loadWeatherIcon(iconCode)

        // Обновляем тексты
        binding.tvTempMain.text = "$temp°C"
        binding.tvWindSpeed.text = "$wind m/s"
        binding.tvHumidity.text = "$humidity%"
        binding.tvRain.text = "$rain mm"
    }

    private fun ImageView.loadWeatherIcon(iconCode: String) {
        val url = "https://openweathermap.org/img/wn/$iconCode@2x.png"
        Glide.with(this)
            .load(url)
            .into(this)
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
                    Toast.makeText(context, "Ошибка загрузки прогноза других городов", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}