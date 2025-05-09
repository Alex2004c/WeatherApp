package com.example.weather.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.weather.databinding.ItemHourlyBinding
import com.example.weather.model.ForecastItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HourlyAdapter(private val items: List<ForecastItem>)
    : RecyclerView.Adapter<HourlyAdapter.Viewholder>() {

    private lateinit var context: Context

    class Viewholder(val binding: ItemHourlyBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder {
        context = parent.context
        val binding = ItemHourlyBinding.inflate(LayoutInflater.from(context), parent, false)
        return Viewholder(binding)
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        val item = items[position]

        holder.binding.apply {
            hourTxt.text = item.hour
            tempTxt.text = "${item.mainData.temp.toInt()}°C"

            val iconUrl = "https://openweathermap.org/img/wn/${item.weather.firstOrNull()?.icon}@2x.png"
            Glide.with(context).load(iconUrl).into(pic)

        }
    }


    override fun getItemCount(): Int = items.size

    val ForecastItem.hour: String
        get() {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            return sdf.format(Date(dt * 1000L)) // dt в секундах → переводим в миллисекунды
        }

    // Метод для иконок можно оставить как есть
    private fun getWeatherIcon(iconCode: String): String {
        return when (iconCode.take(2)) {
            "01" -> "sunny"
            "02" -> "cloudy_2"
            "03", "04" -> "cloudy"
            "09", "10" -> "rainy2"
            "11" -> "windy"
            "13" -> "snowy"
            "50" -> "foggy"
            else -> "unknown"
        }
    }
}
