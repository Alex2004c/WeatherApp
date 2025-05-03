package com.example.weather.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.weather.databinding.ItemDailyBinding
import com.example.weather.model.DailyModel

class DailyAdapter(
    private val items: MutableList<DailyModel>,
    private val onItemClickListener: (Int) -> Unit
) : RecyclerView.Adapter<DailyAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemDailyBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemDailyBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.binding.apply {
            dayText.text = item.date
            tempText.text = "${item.tempMin}°C - ${item.tempMax}°C"
            Glide.with(root).load("https://openweathermap.org/img/w/${item.icon}.png").into(iconView)

            detailContainer.visibility = if (item.isExpanded) View.VISIBLE else View.GONE

            root.setOnClickListener {
                item.isExpanded = !item.isExpanded
                onItemClickListener(position)
                notifyItemChanged(position)
            }

            hourlyRecyclerView.layoutManager =
                LinearLayoutManager(root.context, LinearLayoutManager.HORIZONTAL, false)
            hourlyRecyclerView.adapter = HourlyAdapter(item.hourlyList)
        }
    }

    fun toggleExpand(position: Int) {
        items[position] = items[position].copy(isExpanded = !items[position].isExpanded)
        notifyItemChanged(position)
    }
}