package com.example.weather.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.weather.R
import com.example.weather.RetrofitClient
import com.example.weather.adapter.DailyAdapter
import com.example.weather.adapter.SharedViewModel
import com.example.weather.databinding.FragmentFiveDaysBinding
import com.example.weather.databinding.FragmentTodayBinding
import com.example.weather.model.DailyModel
import com.example.weather.model.ForecastItem
import com.example.weather.model.ForecastResponse
import com.example.weather.model.HourlyModel
import com.ismaeldivita.chipnavigation.BuildConfig
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class FiveDaysFragment : Fragment(R.layout.fragment_five_days) {

    private var _binding: FragmentFiveDaysBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: SharedViewModel by activityViewModels()

    private val dailyList = mutableListOf<DailyModel>()
    private lateinit var dailyAdapter: DailyAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFiveDaysBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dailyAdapter = DailyAdapter(dailyList)
/*        dailyAdapter = DailyAdapter(dailyList) { position ->
            dailyAdapter.toggleExpand(position)
        }*/


        binding.recyclerView.adapter = dailyAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Подписываемся на изменение города
        sharedViewModel.mainCity.observe(viewLifecycleOwner) { city ->
            fetchFiveDaysForecast(city)
        }
    }

    private fun fetchFiveDaysForecast(cityName: String) {
        val apiKey = "6eef7a036bd4da72c7ead398dfb6774d"

        RetrofitClient.apiService.getHourlyForecast(cityName, apiKey).enqueue(object :
            Callback<ForecastResponse> {
            override fun onResponse(
                call: Call<ForecastResponse>,
                response: Response<ForecastResponse>
            ) {
                if (response.isSuccessful) {
                    val forecastList = response.body()?.list ?: return

                    // Группируем по дням
                    val groupedByDate = groupByDay(forecastList)

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

    // Группировка по дням
    fun groupByDay(response: List<ForecastItem>): List<DailyModel> {
        val dailyGroup = response.groupBy { it.dt.toDate() }

        return dailyGroup.map { (date, itemsForDay) ->
            DailyModel(
                date = date,
                tempMin = itemsForDay.minOfOrNull { it.mainData.temp.toInt() } ?: 0,
                tempMax = itemsForDay.maxOfOrNull { it.mainData.temp.toInt() } ?: 0,
                icon = itemsForDay.firstOrNull()?.weather?.firstOrNull()?.icon ?: "01d",
                weatherDesc = /*itemsForDay.firstOrNull()?.weather?.firstOrNull()?.description ?:*/ "Неизвестно",
                hourlyList = itemsForDay.filter { forecast ->
                    val calendar = Calendar.getInstance().apply {
                        timeInMillis = forecast.dt * 1000L
                    }
                    val hour = calendar.get(Calendar.HOUR_OF_DAY)
                    /*hour in 7..22 // фильтруем по часам*/
                    hour in 0..25
                },
                isExpanded = false
            )
        }.take(5) // только 5 дней
    }

    // Extension методы
    private fun Long.toDate(): String {
        val sdf = SimpleDateFormat("EEE, dd MMM", Locale.getDefault())
        return sdf.format(Date(this * 1000L))
    }

    val ForecastItem.hour: String
        get() {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            return sdf.format(Date(dt * 1000L)) // dt в секундах → переводим в миллисекунды
        }

    val ForecastItem.date: String
        get() {
            val sdf = SimpleDateFormat("EEEE, dd MMMM", Locale.getDefault())
            return sdf.format(Date(dt * 1000L))
        }

    private val Long.hour: String
        get() {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            return sdf.format(Date(this * 1000L))
        }

    private val Long.date: String
        get() {
            val sdf = SimpleDateFormat("EEEE, dd MMM", Locale.getDefault())
            return sdf.format(Date(this * 1000L))
        }
}