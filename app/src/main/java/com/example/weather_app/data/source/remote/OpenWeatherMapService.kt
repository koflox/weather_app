package com.example.weather_app.data.source.remote

import com.example.weather_app.data.response.open_weather_map.current_weather.CurrentWeatherResponse
import com.example.weather_app.data.response.open_weather_map.forecast.ForecastWeatherResponse
import kotlinx.coroutines.Deferred
import retrofit2.http.GET
import retrofit2.http.Query

@Suppress("DeferredIsResult")
interface OpenWeatherMapService {

    companion object {
        const val ICON_EXTENSION = "png"
    }

    @GET("weather")
    fun getCurrentWeather(
        @Query("q") query: String,
        @Query("units") units: String
    ): Deferred<CurrentWeatherResponse>

    @GET("weather")
    fun getCurrentWeather(
        @Query("id") cityId: Int,
        @Query("units") units: String
    ): Deferred<CurrentWeatherResponse>

    @GET("forecast")
    fun getForecast(
        @Query("q") query: String,
        @Query("units") units: String
    ): Deferred<ForecastWeatherResponse>

    @GET("forecast")
    fun getForecast(
        @Query("id") cityId: Int,
        @Query("units") units: String
    ): Deferred<ForecastWeatherResponse>

}