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
import com.example.weather.model.DailyModel

class DailyAdapter(
    private val items: MutableList<DailyModel>

/*
    private val onItemClickListener: (Int) -> Unit
*/
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

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.binding.apply {
            dayText.text = item.date
            dayStatus.text = item.weatherDesc
            tempMax.text = "${item.tempMax}°C"
            tempMin.text = "${item.tempMin}°C"
            detailContainer.visibility = if (item.isExpanded) View.VISIBLE else View.GONE

            // Загружаем иконку погоды
            val iconUrl = "https://openweathermap.org/img/w/${item.icon}.png"
            Glide.with(context).load(iconUrl).into(iconView)

            // По часам
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

            // Клик по карточке дня
            cardView.setOnClickListener {
                item.isExpanded = !item.isExpanded
                notifyItemChanged(position)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    // Переключение состояния
/*    fun toggleExpand(position: Int) {
        items[position] = items[position].copy(isExpanded = !items[position].isExpanded)
        notifyItemChanged(position)
    }*/
}