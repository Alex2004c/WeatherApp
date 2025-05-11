package com.example.weather.activity

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.weather.adapter.SharedViewModel
import com.example.weather.adapter.ViewPagerAdapter
import com.example.weather.databinding.ActivityMainBinding
import com.example.weather.model.weatherApi.Location
import com.example.weather.model.weatherApi.LocationResponse
import com.example.weather.model.weatherApi.RetrofitClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.tabs.TabLayoutMediator
import com.ismaeldivita.chipnavigation.BuildConfig
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val sharedViewModel: SharedViewModel by viewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (checkLocationPermission()) {
            fetchCurrentLocation()
        } else {
            requestLocationPermission()
        }

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

    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            1001
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchCurrentLocation()
            } else {
                // Разрешение отклонено → показываем Минск
                sharedViewModel.updateMainCity("Минск")
            }
        }
    }


    private fun fetchCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val lat = location.latitude
                val lon = location.longitude
                fetchCityByCoordinates(lat, lon)
            } else {
                Toast.makeText(this, "Не удалось получить местоположение", Toast.LENGTH_SHORT).show()
                sharedViewModel.updateMainCity("Минск")
            }
        }.addOnFailureListener {
            sharedViewModel.updateMainCity("Минск")
        }
    }

    private fun fetchCityByCoordinates(lat: Double, lon: Double) {
        val apiKey = "0f82c0cff9e84ddcb1c92155251005"
        RetrofitClient.apiService.searchCitiesByCoordinates(apiKey, lat, lon).enqueue(object :
            Callback<List<Location>> {
            override fun onResponse(
                call: Call<List<Location>>,
                response: Response<List<Location>>
            ) {
                if (response.isSuccessful && response.body()!!.isNotEmpty()) {
                    val city = response.body()!![0].name
                    sharedViewModel.updateMainCity(city)
                } else {
                    sharedViewModel.updateMainCity("Минск")
                }
            }

            override fun onFailure(call: Call<List<Location>>, t: Throwable) {
                sharedViewModel.updateMainCity("Минск")
            }
        })
    }
}