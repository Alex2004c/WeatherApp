package com.example.weather.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.weather.databinding.ViewholderHourlyBinding
import com.example.weather.model.HourlyModel

class HourlyAdapter(private val items: List<HourlyModel>)
    : RecyclerView.Adapter<HourlyAdapter.Viewholder>() {

    private lateinit var context: Context

    class Viewholder(val binding: ViewholderHourlyBinding)
        : RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourlyAdapter.Viewholder {
        context = parent.context
        val binding = ViewholderHourlyBinding.inflate(LayoutInflater.from(context), parent, false)
        return Viewholder(binding)
    }

    override fun onBindViewHolder(holder: HourlyAdapter.Viewholder, position: Int) {
        val item = items[position]
        holder.binding.apply {
            hourTxt.text = item.hour
            tempTxt.text = "${item.temp}"

                // Свои иконки
/*            val iconResourceName = getWeatherIcon(item.picPath)
            var drawableResourceId = holder.itemView.resources.getIdentifier(
                iconUrl, "drawable",
                context.packageName
           )
           Glide.with(context)
               .load(drawableResourceId)
               .into(pic)*/

            val iconUrl = "https://openweathermap.org/img/wn/${item.picPath}@2x.png"
            Glide.with(context)
                .load(iconUrl)
                .into(pic)

        }
    }

    override fun getItemCount(): Int = items.size

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