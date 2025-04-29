package com.example.weather.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.weather.databinding.ViewholderHourlyBinding
import com.example.weather.model.HourlyModel

class HourlyAdapter(private val items: ArrayList<HourlyModel>)
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

            var drawableResourceId = holder.itemView.resources.getIdentifier(
                item.picPath,
                "drawable",
                context.packageName
            )
            Glide.with(context)
                .load(drawableResourceId)
                .into(pic)

        }
    }

    override fun getItemCount(): Int = items.size

    fun getWeatherIcon(iconCode: String): String {
        return when (iconCode.take(2)) {
            "01" -> "sunny"
            "02" -> "few_clouds"
            "03", "04" -> "cloudy"
            "09", "10" -> "rainy"
            "11" -> "stormy"
            "13" -> "snowy"
            "50" -> "foggy"
            else -> "unknown"
        }
    }
}