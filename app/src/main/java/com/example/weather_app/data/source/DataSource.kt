package com.example.weather_app.data.source

import androidx.lifecycle.LiveData
import com.example.weather_app.data.Result
import com.example.weather_app.data.entity.FavoriteCity
import com.example.weather_app.data.response.open_weather_map.current_weather.CurrentWeatherResponse
import com.example.weather_app.data.response.open_weather_map.forecast.ForecastWeatherResponse

interface DataSource {

    //todo change units to StringDef
    suspend fun getCurrentWeather(query: String, units: String): Result<CurrentWeatherResponse>

    suspend fun getForecast(query: String, units: String): Result<ForecastWeatherResponse>

    fun observeFavoriteCities(): LiveData<Result<List<FavoriteCity>>>

    suspend fun getFavoriteCities(): Result<List<FavoriteCity>>

    suspend fun getFavoriteCity(latitude: Double, longitude: Double): Result<FavoriteCity>

    suspend fun insert(city: FavoriteCity)

    suspend fun update(city: FavoriteCity)

    suspend fun delete(city: FavoriteCity)

    suspend fun deleteFavoriteCities()

}