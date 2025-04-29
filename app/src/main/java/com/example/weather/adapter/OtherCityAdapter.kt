package com.example.weather.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.weather.databinding.ViewholderCityBinding
import com.example.weather.model.CityModel

class OtherCityAdapter (private val items: ArrayList<CityModel>):
    RecyclerView.Adapter<OtherCityAdapter.Viewholder>() {

    private lateinit var context: Context

    class Viewholder(val binding: ViewholderCityBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OtherCityAdapter.Viewholder {
        context = parent.context
        val binding = ViewholderCityBinding.inflate(LayoutInflater.from(context), parent, false)
        return Viewholder (binding)
    }

    override fun onBindViewHolder(holder: OtherCityAdapter.Viewholder, position: Int) {
        val item = items [position]
        holder.binding.apply {
            cityTxt.text = item.cityName
            windTxt.text = "${item.wind} Km/h"
            humidityTxt.text = "${item.humidity}%"
            tempTxt.text = "${item.temp}°"

            val iconResourceName = getWeatherIcon(item.picPath)
            val drawableResourceId = holder.itemView.resources.getIdentifier (
                iconResourceName,
                "drawable",
                context.packageName
            )
            Glide.with(context)
                .load(drawableResourceId)
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