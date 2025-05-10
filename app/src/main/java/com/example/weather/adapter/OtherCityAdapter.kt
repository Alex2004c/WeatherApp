package com.example.weather.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.weather.databinding.ViewholderCityBinding
import com.example.weather.model.weatherApi.CityModel

class OtherCityAdapter(
    private val context: Context,
    private val items: ArrayList<CityModel>,
    private val onItemClickListener: (CityModel) -> Unit
) :
    RecyclerView.Adapter<OtherCityAdapter.Viewholder>() {

    class Viewholder(val binding: ViewholderCityBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder {
        val binding = ViewholderCityBinding.inflate(LayoutInflater.from(context), parent, false)
        return Viewholder(binding)
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        val item = items[position]
        holder.binding.apply {
            cityTxt.text = item.name
            windTxt.text = "${item.windKph.toInt()} км/ч"
            humidityTxt.text = "${item.humidity}%"
            tempTxt.text = "${item.temp}°"
            rainTxt.text = "${item.rainMm} мм"

            /*            val iconResourceName = getWeatherIcon(item.picPath)
                        val drawableResourceId = holder.itemView.resources.getIdentifier (
                            iconResourceName,
                            "drawable",
                            context.packageName
                        )
                        Glide.with(context)
                            .load(drawableResourceId)
                            .into(pic)*/
            val iconUrl = "https:${item.icon}"
            Glide.with(context)
                .load(iconUrl)
                .into(pic)
        }
        holder.itemView.setOnClickListener {
            onItemClickListener(item)
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