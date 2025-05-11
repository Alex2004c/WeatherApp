package com.example.weather.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weather.R
import com.example.weather.adapter.DailyAdapter
import com.example.weather.adapter.SharedViewModel
import com.example.weather.databinding.FragmentTenDaysBinding
import com.example.weather.model.weatherApi.DailyModel
import com.example.weather.model.weatherApi.ForecastResponse
import com.example.weather.model.weatherApi.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TenDaysFragment : Fragment(R.layout.fragment_ten_days) {

    private var _binding: FragmentTenDaysBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: SharedViewModel by activityViewModels()

    private val dailyList = mutableListOf<DailyModel>()
    private lateinit var dailyAdapter: DailyAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTenDaysBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dailyAdapter = DailyAdapter(dailyList)
 /*       dailyAdapter = DailyAdapter(dailyList) { position ->
            dailyAdapter.toggleExpand(position)
        }*/

        binding.recyclerView.adapter = dailyAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        setupSwipeRefresh()

        // Подписываемся на изменение города
        sharedViewModel.mainCity.observe(viewLifecycleOwner) { city ->
            fetchFiveDaysForecast(city)
        }
    }

    private fun setupSwipeRefresh() {

        binding.swipeRefresh.setOnRefreshListener {
            val city = sharedViewModel.mainCity.value ?: "Минск"
            fetchFiveDaysForecast(city)
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun fetchFiveDaysForecast(cityName: String) {
        val apiKey = "0f82c0cff9e84ddcb1c92155251005"

        RetrofitClient.apiService.getTenDaysForecast(cityName, apiKey).enqueue(object :
            Callback<ForecastResponse> {
            override fun onResponse(
                call: Call<ForecastResponse>,
                response: Response<ForecastResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val forecastDays = response.body()!!.forecast.forecastday

                    // ForecastDay → DailyModel
                    val groupedByDate = forecastDays.map { day ->
                        DailyModel(
                            date = day.date,
                            tempMin = day.day.mintemp_c.toInt(),
                            tempMax = day.day.maxtemp_c.toInt(),
                            icon = day.day.condition.icon,
                            weatherDesc = day.day.condition.text,
                            sunrise = day.astro.sunrise,
                            sunset = day.astro.sunset,
                            humidity = day.day.avghumidity,
                            uv = day.day.uv,
                            rainChance = day.day.daily_chance_of_rain,
                            hourlyList = day.hour,
                            isExpanded = false
                        )
                    }

                    dailyList.clear()
                    dailyList.addAll(groupedByDate)
                    dailyAdapter.notifyDataSetChanged()
                }
            }

            override fun onFailure(call: Call<ForecastResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Ошибка прогноза на дни", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
