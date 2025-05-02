package com.example.weather.activity

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.weather.R
import com.example.weather.RetrofitClient
import com.example.weather.adapter.HourlyAdapter
import com.example.weather.adapter.OtherCityAdapter
import com.example.weather.adapter.SharedViewModel
import com.example.weather.adapter.ViewPagerAdapter
import com.example.weather.databinding.ActivityMainBinding
import com.example.weather.model.CityModel
import com.example.weather.model.ForecastResponse
import com.example.weather.model.HourlyModel
import com.example.weather.model.WeatherResponse
import com.google.android.material.internal.ViewUtils.hideKeyboard
import com.google.android.material.tabs.TabLayoutMediator
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val sharedViewModel: SharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        /*        supportFragmentManager.beginTransaction()
                    .add(R.id.fragment_container, TodayFragment.newInstance("Minsk"))
                    .commit()*/

        binding.viewPager.adapter = ViewPagerAdapter(this)

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                ViewPagerAdapter.TODAY_FRAGMENT -> tab.text = "Сегодня"
                ViewPagerAdapter.FIVE_DAYS_FRAGMENT -> tab.text = "5 дней"
            }
        }.attach()


        sharedViewModel.mainCity.observe(this) { city ->
            binding.editTextText.setText(city)
        }

        binding.button.setOnClickListener {
            val cityName = binding.editTextText.text.toString().trim()

            binding.editTextText.clearFocus()
            val inputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(binding.editTextText.windowToken, 0)

            if (cityName.isNotEmpty()) {
                if (cityName != sharedViewModel.mainCity.value) {
                    sharedViewModel.updateMainCity(cityName)
                }
            } else {
                Toast.makeText(this@MainActivity, "Введите название города", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        /*        binding.chipNavigator.setItemSelected(R.id.home, true)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )*/
    }
}