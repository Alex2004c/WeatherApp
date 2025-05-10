package com.example.weather.activity

import android.os.Bundle
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
import com.example.weather.adapter.ThreeHourAdapter
import com.example.weather.adapter.OtherCityAdapter
import com.example.weather.adapter.SharedViewModel
import com.example.weather.databinding.FragmentTodayBinding
import com.example.weather.model.weatherApi.CityModel
import com.example.weather.model.weatherApi.ForecastResponse
import com.example.weather.model.weatherApi.Hourly
import com.example.weather.model.weatherApi.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

class TodayFragment : Fragment(R.layout.fragment_today) {

    private var _binding: FragmentTodayBinding? = null
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
        _binding = FragmentTodayBinding.inflate(inflater, container, false)
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
        RetrofitClient.apiService.getCurrentWeather(cityName, apiKey).enqueue(object :
            Callback<ForecastResponse> {
            override fun onResponse(
                call: Call<ForecastResponse>,
                response: Response<ForecastResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!

                    val temp = data.current.temp_c.toInt()
                    val icon = data.current.condition.icon
                    val windKph = data.current.wind_kph
                    val humidity = data.current.humidity
                    val rainMm = data.current.precip_mm

                    if (!otherCityList.any { it.name == cityName }) {
                        otherCityList.add(0, CityModel(
                            name = cityName,
                            temp = temp,
                            icon = icon,
                            windKph = windKph,
                            humidity = humidity,
                            rainMm = rainMm
                        ))
                    }
                    otherCityAdapter.notifyItemInserted(0)
                    onSuccess()
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

                        val allDays = response.body()!!.forecast.forecastday

                        val allHours = allDays.flatMap { it.hour }

                        val now = System.currentTimeMillis() / 1000L
                        val next24hours = now + 24 * 3600L

                        val filtered = allHours.filter {
                            it.time.toUnixTimestamp() in now..next24hours
                        }.sortedBy { it.time.toUnixTimestamp() }

                        hourlyList.clear()
                        hourlyList.addAll(filtered)
                        hourlyAdapter.notifyDataSetChanged()
                    }
                }

                override fun onFailure(call: Call<ForecastResponse>, t: Throwable) {
                    Toast.makeText(context, "Ошибка загрузки по 3ч прогноза", Toast.LENGTH_SHORT).show()
                }
            })
    }

    fun String.toUnixTimestamp(): Long {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val date = sdf.parse(this) ?: return 0L
        return date.time / 1000L
    }

    private fun fetchCurrentCityWeather(cityName: String?, onSuccessCallback: () -> Unit) {
        val apiKey = "0f82c0cff9e84ddcb1c92155251005"

        if (cityName.isNullOrEmpty()) return

        RetrofitClient.apiService.getCurrentWeather(cityName, apiKey).enqueue(object :
            Callback<ForecastResponse> {
            override fun onResponse(
                call: Call<ForecastResponse>,
                response: Response<ForecastResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!.current
                    val location = response.body()!!.location

                    val cityN = location.name
                    binding.tvCity.text = cityN

                    val temp = data.temp_c.toInt()
                    val feelsLike = data.feelslike_c

                    val condition = data.condition.text
                    binding.tvCondition.text = condition
                    val lastUpdated = data.last_updated
                    binding.tvLastUpdated.text = lastUpdated.toFormattedDate()

                    val windKph = data.wind_kph
                    val humidity = data.humidity
                    val rain = data.precip_mm
                    val gustKph = data.gust_kph
                    val pressureMb = data.pressure_mb
                    val uv = data.uv

                    val icon = data.condition.icon

                    updateMainWeatherUI(
                        iconCode = icon,
                        temp = temp,
                        feelsLike = feelsLike,
                        windKph = windKph,
                        gustKph = gustKph,
                        humidity = humidity,
                        pressureMb = pressureMb,
                        precipMm = rain,
                        uv = uv
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

    fun String.toFormattedDate(): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMMM, HH:mm", Locale.getDefault())

        return try {
            val date = inputFormat.parse(this) ?: return this
            outputFormat.format(date)
        } catch (e: Exception) {
            this
        }
    }


    private fun updateMainWeatherUI(iconCode: String, temp: Int, feelsLike: Double, windKph: Double, gustKph: Double,
                                    humidity: Int, pressureMb: Double, precipMm: Double, uv: Double
    ) {
        binding.imageViewMain.loadWeatherIcon(iconCode)
        binding.tvTempMain.text = "$temp°C"
        binding.tvFeeling.text = "$feelsLike°C"
        binding.tvWindSpeed.text = "${windKph.toInt()} км/ч"
        binding.tvGust.text = "${gustKph.toInt()} км/ч"
        binding.tvHumidity.text = "$humidity%"
        binding.tvPressure.text = "${pressureMb} мбар"
        binding.tvRain.text = "$precipMm мм"
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

        for (city in otherCities) {
            RetrofitClient.apiService.getCurrentWeather(city, apiKey).enqueue(object :
                Callback<ForecastResponse> {
                override fun onResponse(
                    call: Call<ForecastResponse>,
                    response: Response<ForecastResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val data = response.body()!!

                        val temp = data.current.temp_c.toInt()
                        val icon = data.current.condition.icon
                        val windKph = data.current.wind_kph
                        val humidity = data.current.humidity
                        val rainMm = data.current.precip_mm

                        otherCityList.add(CityModel(city, temp, icon, windKph, humidity, rainMm))
                        otherCityAdapter.notifyDataSetChanged()
                    }
                }

                override fun onFailure(call: Call<ForecastResponse>, t: Throwable) {
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