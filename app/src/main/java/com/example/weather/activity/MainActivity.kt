package com.example.weather.activity

import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.weather.adapter.SharedViewModel
import com.example.weather.adapter.ViewPagerAdapter
import com.example.weather.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val sharedViewModel: SharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        binding.viewPager.adapter = ViewPagerAdapter(this)

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                ViewPagerAdapter.TODAY_FRAGMENT -> tab.text = "Сегодня"
                ViewPagerAdapter.TOMORROW_FRAGMENT -> tab.text = "Завтра"
                ViewPagerAdapter.TEN_DAYS_FRAGMENT -> tab.text = "10 дней"
            }
        }.attach()

        sharedViewModel.mainCity.observe(this) { city ->
            binding.editTextText.setText(city)
        }

        binding.editTextText.setOnClickListener {
            binding.editTextText.setCursorVisible(true)
        }

        binding.button.setOnClickListener {
            val cityName = binding.editTextText.text.toString().trim()

            binding.editTextText.clearFocus()
            binding.editTextText.setCursorVisible(false)

            // Скрываем клавиатуру
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.editTextText.windowToken, 0)

            if (cityName.isNotEmpty()) {
                if (cityName != sharedViewModel.mainCity.value) {
                    sharedViewModel.updateMainCity(cityName)
                }
            } else {
                Toast.makeText(this@MainActivity, "Введите название города", Toast.LENGTH_SHORT)
                    .show()
            }
        }

    }
}