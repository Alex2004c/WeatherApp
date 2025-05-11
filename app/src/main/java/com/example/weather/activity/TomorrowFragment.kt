package com.example.weather.activity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.weather.R
import com.example.weather.adapter.HourlyAdapter
import com.example.weather.adapter.OtherCityAdapter
import com.example.weather.adapter.SharedViewModel
import com.example.weather.adapter.ThreeHourAdapter
import com.example.weather.databinding.FragmentTodayBinding
import com.example.weather.databinding.FragmentTomorrowBinding
import com.example.weather.model.weatherApi.CityModel
import com.example.weather.model.weatherApi.ForecastResponse
import com.example.weather.model.weatherApi.Hourly
import com.example.weather.model.weatherApi.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale

class TomorrowFragment : Fragment(R.layout.fragment_tomorrow) {

    private var _binding: FragmentTomorrowBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: SharedViewModel by activityViewModels()

    private var lastCity: String? = "Минск"

    private val hourlyList = ArrayList<Hourly>()
    private val otherCityList = ArrayList<CityModel>()

    private lateinit var hourlyAdapter: ThreeHourAdapter
    private lateinit var otherCityAdapter: OtherCityAdapter

    private val otherCities = listOf("Гомель", "Брест", "Витебск", "Гродно", "Могилев")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTomorrowBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewPager = requireActivity().findViewById<ViewPager2>(R.id.viewPager)

        binding.view1.addOnItemTouchListener(object : RecyclerView.SimpleOnItemTouchListener() {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                when (e.action) {
                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                        viewPager.isUserInputEnabled = false
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        viewPager.isUserInputEnabled = true
                    }
                }
                return false
            }
        })

        binding.view2.addOnItemTouchListener(object : RecyclerView.SimpleOnItemTouchListener() {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                when (e.action) {
                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                        viewPager.isUserInputEnabled = false
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        viewPager.isUserInputEnabled = true
                    }
                }
                return false
            }
        })

        sharedViewModel.mainCity.observe(viewLifecycleOwner) { newCity ->
            if (newCity != lastCity) {
                fetchTomorrowCityWeather(newCity) {
                    fetchHourlyForecast(newCity)
                    refreshOtherCityList()
                }
            }
        }

        setupAdapters()
        setupSwipeRefresh()
        fetchTomorrowCityWeather(sharedViewModel.mainCity.value) {
            fetchHourlyForecast(sharedViewModel.mainCity.value)
            fetchOtherCitiesWeather()
        }
    }

    private fun setupSwipeRefresh() {

        binding.swipeRefresh.setOnRefreshListener {
            val city = sharedViewModel.mainCity.value ?: "Минск"
            fetchTomorrowCityWeather(city) {
                fetchHourlyForecast(city)
            }
            if (city != lastCity) {
                refreshOtherCityList()
            }
            otherCityAdapter.notifyDataSetChanged()
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun setupAdapters() {

        hourlyAdapter = ThreeHourAdapter(hourlyList)
        binding.view1.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.view1.adapter = hourlyAdapter

        otherCityAdapter = OtherCityAdapter(requireContext(), otherCityList) { selectedCity ->
            if (selectedCity.name != sharedViewModel.mainCity.value) {
                sharedViewModel.updateMainCity(selectedCity.name)
            }
        }

        binding.view2.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.view2.adapter = otherCityAdapter
    }

    private fun refreshOtherCityList() {

        val newCityIndex = otherCityList.indexOfFirst { it.name == sharedViewModel.mainCity.value }

        if (newCityIndex != -1) {
            otherCityList.removeAt(newCityIndex)
        }

        if (!otherCityList.any { it.name == lastCity}) {
            fetchCityAndAddToOtherList(lastCity) {
                otherCityAdapter.notifyDataSetChanged()
            }
        } else {
            otherCityAdapter.notifyDataSetChanged()
        }

        lastCity = sharedViewModel.mainCity.value
    }

    private fun fetchCityAndAddToOtherList(cityName: String?, onSuccess: () -> Unit) {
        val apiKey = "0f82c0cff9e84ddcb1c92155251005"

        if (cityName.isNullOrEmpty()) return
        RetrofitClient.apiService.getTwoDaysForecast(cityName, apiKey).enqueue(object :
            Callback<ForecastResponse> {
            override fun onResponse(
                call: Call<ForecastResponse>,
                response: Response<ForecastResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {

                    val forecastData = response.body()!!.forecast
                    val tomorrowDay = forecastData.forecastday.getOrNull(1)

                    if (tomorrowDay != null) {
                        val temp = tomorrowDay.day.maxtemp_c.toInt()
                        val icon = tomorrowDay.day.condition.icon
                        val windKph = tomorrowDay.day.maxwind_kph
                        val humidity = tomorrowDay.day.avghumidity
                        val rainChance = "${tomorrowDay.day.daily_chance_of_rain}" + "%"

                        if (!otherCityList.any { it.name == cityName }) {
                            otherCityList.add(0, CityModel(
                                name = cityName,
                                temp = temp,
                                icon = icon,
                                windKph = windKph,
                                humidity = humidity,
                                rainMm = rainChance
                            )
                            )
                        }
                        otherCityAdapter.notifyItemInserted(0)
                        onSuccess()
                    }
                }
            }

            override fun onFailure(call: Call<ForecastResponse>, t: Throwable) {
                Toast.makeText(context, "Ошибка загрузки города при замене карточки", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchHourlyForecast(cityName: String?) {
        val apiKey = "0f82c0cff9e84ddcb1c92155251005"

        if (cityName.isNullOrBlank()) return

        RetrofitClient.apiService.getTwoDaysForecast(cityName, apiKey).enqueue(object :
            Callback<ForecastResponse> {
            override fun onResponse(
                call: Call<ForecastResponse>,
                response: Response<ForecastResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {

                    val tomorrowDay = response.body()!!.forecast.forecastday.getOrNull(1)
                    if (tomorrowDay != null) {
                        hourlyList.clear()
                        hourlyList.addAll(tomorrowDay.hour)
                        hourlyAdapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(context, "Нет данных на завтра", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<ForecastResponse>, t: Throwable) {
                Toast.makeText(context, "Ошибка загрузки почасового прогноза на завтра", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchTomorrowCityWeather(cityName: String?, onSuccessCallback: () -> Unit) {
        val apiKey = "0f82c0cff9e84ddcb1c92155251005"

        if (cityName.isNullOrEmpty()) return

        RetrofitClient.apiService.getTwoDaysForecast(cityName, apiKey).enqueue(object :
            Callback<ForecastResponse> {
            override fun onResponse(
                call: Call<ForecastResponse>,
                response: Response<ForecastResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val forecastDays = response.body()!!.forecast.forecastday
                    val tomorrowDay = forecastDays.getOrNull(1) ?: return

                    val condition = tomorrowDay.day.condition.text
                    binding.tvCondition.text = condition
                    val date = tomorrowDay.date
                    binding.tvDate.text = date.toFormattedDate()

                    updateMainWeatherUI(
                        iconCode = tomorrowDay.day.condition.icon,
                        windKph = tomorrowDay.day.maxwind_kph,
                        sunrise = tomorrowDay.astro.sunrise,
                        humidity = tomorrowDay.day.avghumidity,
                        sunset = tomorrowDay.astro.sunset,
                        precipChance = tomorrowDay.day.daily_chance_of_rain,
                        uv = tomorrowDay.day.uv
                    )
                    onSuccessCallback()
                } else {
                    Toast.makeText(context, "Город не найден", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ForecastResponse>, t: Throwable) {
                Toast.makeText(context, "Ошибка загрузки главной погоды", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun String.to24HourFormat(): String {
        val inputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        return try {
            val date = inputFormat.parse(this) ?: return this
            outputFormat.format(date)
        } catch (e: Exception) {
            this
        }
    }

    fun String.toFormattedDate(): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("EEEE, dd MMMM", Locale.getDefault())

        return try {
            val date = inputFormat.parse(this) ?: return this
            outputFormat.format(date)
        } catch (e: Exception) {
            this
        }
    }


    private fun updateMainWeatherUI(iconCode: String, windKph: Double, sunrise: String,
                                    humidity: Int, sunset: String, precipChance: Int, uv: Double
    ) {
        binding.imageViewMain.loadWeatherIcon(iconCode)
        binding.tvSunrise.text = sunrise.to24HourFormat()
        binding.tvGust.text = "${windKph.toInt()} км/ч"
        binding.tvHumidity.text = "$humidity%"
        binding.tvSunset.text = sunset.to24HourFormat()
        binding.tvRain.text = "$precipChance%"
        binding.tvUV.text = "$uv"
    }

    private fun ImageView.loadWeatherIcon(iconCode: String) {
        val url = "https:$iconCode"
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
        val apiKey = "0f82c0cff9e84ddcb1c92155251005"

        CoroutineScope(Dispatchers.IO).launch {
            for ((index, city) in otherCities.withIndex()) {
                try {
                    val response = RetrofitClient.apiService.getTwoDaysForecast(city, apiKey).execute()

                    if (response.isSuccessful && response.body() != null) {
                        val forecastData = response.body()!!.forecast
                        val tomorrowDay = forecastData.forecastday.getOrNull(1)

                        if (tomorrowDay != null) {
                            val temp = tomorrowDay.day.maxtemp_c.toInt()
                            val icon = tomorrowDay.day.condition.icon
                            val windKph = tomorrowDay.day.maxwind_kph
                            val humidity = tomorrowDay.day.avghumidity
                            val rainChance = "${tomorrowDay.day.daily_chance_of_rain}%"

                            withContext(Dispatchers.Main) {
                                if (!otherCityList.any { it.name == city }) {
                                    otherCityList.add(CityModel(city, temp, icon, windKph, humidity, rainChance))
                                    otherCityAdapter.notifyItemInserted(otherCityList.size - 1)
                                } else {
                                    val existingIndex = otherCityList.indexOfFirst { it.name == city }
                                    otherCityList[existingIndex] = CityModel(city, temp, icon, windKph, humidity, rainChance)
                                    otherCityAdapter.notifyItemChanged(existingIndex)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    for (attempt in 1..3) {
                        delay(1000L * attempt)
                        try {
                            val retryResponse = RetrofitClient.apiService.getTwoDaysForecast(city, apiKey).execute()
                            if (retryResponse.isSuccessful && retryResponse.body() != null) {
                                val forecastData = retryResponse.body()!!.forecast
                                val tomorrowDay = forecastData.forecastday.getOrNull(1)

                                if (tomorrowDay != null) {
                                    val temp = tomorrowDay.day.maxtemp_c.toInt()
                                    val icon = tomorrowDay.day.condition.icon
                                    val windKph = tomorrowDay.day.maxwind_kph
                                    val humidity = tomorrowDay.day.avghumidity
                                    val rainChance = "${tomorrowDay.day.daily_chance_of_rain}%"

                                    withContext(Dispatchers.Main) {
                                        if (!otherCityList.any { it.name == city }) {
                                            otherCityList.add(CityModel(city, temp, icon, windKph, humidity, rainChance))
                                            otherCityAdapter.notifyItemInserted(otherCityList.size - 1)
                                        } else {
                                            val existingIndex = otherCityList.indexOfFirst { it.name == city }
                                            otherCityList[existingIndex] = CityModel(city, temp, icon, windKph, humidity, rainChance)
                                            otherCityAdapter.notifyItemChanged(existingIndex)
                                        }
                                    }
                                    break
                                }
                            }
                        } catch (e: Exception) {
                            if (attempt == 3) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Ошибка загрузки прогноза для $city", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }

                if (index < otherCities.size - 1) {
                    delay(50L)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}