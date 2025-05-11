package com.example.weather.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.weather.activity.TenDaysFragment
import com.example.weather.activity.TodayFragment
import com.example.weather.activity.TomorrowFragment

class ViewPagerAdapter(
    fragmentActivity: FragmentActivity,
) : FragmentStateAdapter(fragmentActivity) {

    private val fragments = listOf(
        TodayFragment::class.java,
        TomorrowFragment::class.java,
        TenDaysFragment::class.java
    )

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> TodayFragment()
            1 -> TomorrowFragment()
            2 -> TenDaysFragment()
            else -> throw IllegalArgumentException("Неизвестный фрагмент")
        }
    }

    companion object {
        const val TODAY_FRAGMENT = 0
        const val TOMORROW_FRAGMENT = 1
        const val TEN_DAYS_FRAGMENT = 2
    }
}