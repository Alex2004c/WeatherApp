package com.example.weather.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.weather.databinding.ItemHourlyBinding
import com.example.weather.model.weatherApi.Hourly
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class ThreeHourAdapter(private val items: List<Hourly>)
    : RecyclerView.Adapter<ThreeHourAdapter.Viewholder>() {

    private lateinit var context: Context

    class Viewholder(val binding: ItemHourlyBinding)
        : RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder {
        context = parent.context
        val binding = ItemHourlyBinding.inflate(LayoutInflater.from(context), parent, false)
        return Viewholder(binding)
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        val item = items[position]

        holder.binding.apply {
            hourTxt.text = item.time.extractTime()
            tempTxt.text = "${item.temp_c.toInt()}°C"

                // Свои иконки
/*            val iconResourceName = getWeatherIcon(item.picPath)
            var drawableResourceId = holder.itemView.resources.getIdentifier(
                iconUrl, "drawable",
                context.packageName
           )
           Glide.with(context)
               .load(drawableResourceId)
               .into(pic)*/

            val iconUrl = "https:${item.condition.icon}"
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
    private fun String.extractTime(): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        return try {
            val date = inputFormat.parse(this) ?: return this
            outputFormat.format(date)
        } catch (e: ParseException) {
            this
        }
    }
}