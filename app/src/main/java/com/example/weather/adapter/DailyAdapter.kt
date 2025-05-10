package com.example.weather.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.weather.R
import com.example.weather.activity.MainActivity
import com.example.weather.databinding.ItemDailyBinding
import com.example.weather.model.weatherApi.DailyModel
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DailyAdapter(
    private val items: MutableList<DailyModel>
) : RecyclerView.Adapter<DailyAdapter.ViewHolder>() {

    private lateinit var context: Context
    private lateinit var viewPager: ViewPager2

    class ViewHolder(val binding: ItemDailyBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val binding = ItemDailyBinding.inflate(LayoutInflater.from(context), parent, false)
        if (context is MainActivity) {
            viewPager = (context as MainActivity).findViewById(R.id.viewPager)
        }
        return ViewHolder(binding)
    }

    private fun String.to24HourFormat(): String {
        val inputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        return try {
            val date = inputFormat.parse(this) ?: return this
            outputFormat.format(date)
        } catch (e: Exception) {
            this // если не получилось — возвращаем исходную строку
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.binding.apply {
            dayText.text = if (item.date.isToday()) {
                "Сегодня"
            } else {
                item.date.toFormattedDate()
            }
            dayStatus.text = item.weatherDesc
            tempMax.text = "${item.tempMax}°C"
            tempMin.text = "${item.tempMin}°C"
            tvRainValue.text = "${item.rainChance}%"
            tvHumidityValue.text = "${item.humidity}%"
            tvUVValue.text = "${item.uv}"
            tvSunriseValue.text = item.sunrise.to24HourFormat()
            tvSunsetValue.text = item.sunset.to24HourFormat()
            detailContainer.visibility = if (item.isExpanded) View.VISIBLE else View.GONE

            val iconUrl = "https:${item.icon}"
            Glide.with(context).load(iconUrl).into(iconView)

            hourlyRecyclerView.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            hourlyRecyclerView.adapter = HourlyAdapter(item.hourlyList)

            hourlyRecyclerView.addOnItemTouchListener(object : RecyclerView.SimpleOnItemTouchListener() {
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

            cardView.setOnClickListener {
                item.isExpanded = !item.isExpanded
                notifyItemChanged(position)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    private fun String.toFormattedDate(): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("EEEE, dd MMMM", Locale.getDefault())

        return try {
            val date = inputFormat.parse(this) ?: return this
            outputFormat.format(date)
        } catch (e: ParseException) {
            this
        }
    }

    private fun String.isToday(): Boolean {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = Calendar.getInstance().time
        val itemDate = inputFormat.parse(this) ?: return false

        val calendarItem = Calendar.getInstance().apply {
            time = itemDate
        }

        val calendarToday = Calendar.getInstance().apply {
            time = today
        }

        return calendarItem.get(Calendar.DAY_OF_YEAR) == calendarToday.get(Calendar.DAY_OF_YEAR) &&
                calendarItem.get(Calendar.YEAR) == calendarToday.get(Calendar.YEAR)
    }

}
