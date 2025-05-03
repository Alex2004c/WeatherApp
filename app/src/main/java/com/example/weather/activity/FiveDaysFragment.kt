package com.example.weather.activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weather.R
import com.example.weather.RetrofitClient
import com.example.weather.adapter.DailyAdapter
import com.example.weather.adapter.SharedViewModel
import com.example.weather.databinding.FragmentFiveDaysBinding
import com.example.weather.model.DailyModel
import com.example.weather.model.ForecastResponse
import com.example.weather.model.HourlyModel
import com.ismaeldivita.chipnavigation.BuildConfig
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FiveDaysFragment : Fragment(R.layout.fragment_five_days) {

    private lateinit var binding: FragmentFiveDaysBinding
    private val sharedViewModel: SharedViewModel by activityViewModels()

    private val dailyList = mutableListOf<DailyModel>()
    private lateinit var dailyAdapter: DailyAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFiveDaysBinding.bind(view)

        // Подписываемся на главный город
        sharedViewModel.mainCity.observe(viewLifecycleOwner) { city ->
            fetchFiveDaysForecast(city)
        }

        // Инициализируем RecyclerView
        dailyAdapter = DailyAdapter(dailyList) { position ->
            dailyAdapter.toggleExpand(position)
        }

        binding.recyclerView.adapter = dailyAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun fetchFiveDaysForecast(cityName: String) {
        val apiKey = "6eef7a036bd4da72c7ead398dfb6774d"

        RetrofitClient.apiService.getFiveDaysForecast(cityName, apiKey).enqueue(object :
            Callback<ForecastResponse> {
            override fun onResponse(
                call: Call<ForecastResponse>,
                response: Response<ForecastResponse>
            ) {
                if (response.isSuccessful) {
                    val forecastList = response.body()?.list ?: return

                    // Группируем по дням
                    //val groupedByDate = groupByDay(forecastList)

                    dailyList.clear()
                    //dailyList.addAll(groupedByDate)
                    dailyAdapter.notifyDataSetChanged()
                }
            }

            override fun onFailure(call: Call<ForecastResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Ошибка загрузки прогноза", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun Long.toDate(): String {
        val sdf = SimpleDateFormat("EEE, dd MMM", Locale.getDefault())
        return sdf.format(Date(this * 1000L))
    }

/*    fun groupByDay(forecastList: List<HourlyModel>): List<DailyModel> {
        val dailyGroup = forecastList.groupBy { it.dt.toDate() }

        return dailyGroup.map { (date, hourly) ->
            DailyModel(
                date = date,
                tempMin = hourly.minOfOrNull { it.main.temp.toInt() }!!,
                tempMax = hourly.maxOfOrNull { it.main.temp.toInt() }!!,
                icon = hourly.first().weather.firstOrNull()?.icon ?: "01d",
                weatherDesc = hourly.first().weather.firstOrNull()?.description ?: "Неизвестно",
                hourlyList = hourly.filter {
                    val hour = it.dt.hour.toInt()
                    hour in 7..22 // показываем только с 7 до 22 часов
                },
                windSpeed = hourly.first().wind.speed,
                humidity = hourly.first().main.humidity,
                uvi = hourly.first().uvi ?: 0,
                sunrise = hourly.first().city.sunrise,
                sunset = hourly.last().city.sunset
            )
        }
    }*/
}