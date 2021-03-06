package com.example.weather_app.ui.weather

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.weather_app.R
import com.example.weather_app.data.Result
import com.example.weather_app.data.data.FavoriteCity
import com.example.weather_app.data.data.Unit
import com.example.weather_app.data.displayed.DisplayedWeatherItem
import com.example.weather_app.data.displayed.WeatherData
import com.example.weather_app.data.response.open_weather_map.current_weather.CurrentWeatherResponse
import com.example.weather_app.data.response.open_weather_map.current_weather.toDetailsWeatherData
import com.example.weather_app.data.response.open_weather_map.current_weather.toFavoriteCity
import com.example.weather_app.data.response.open_weather_map.current_weather.toMainWeatherData
import com.example.weather_app.data.response.open_weather_map.forecast.ForecastWeatherResponse
import com.example.weather_app.data.response.open_weather_map.forecast.toForecastWeatherData
import com.example.weather_app.data.response.open_weather_map.forecast.toHourlyWeatherData
import com.example.weather_app.data.response.open_weather_map.forecast.toPrecipitationWeatherData
import com.example.weather_app.data.source.AppDataRepository
import com.example.weather_app.ui.base.BaseViewModel
import com.example.weather_app.util.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WeatherViewModel(
    private val app: Application,
    private val dataRepository: AppDataRepository
) : BaseViewModel(app) {

    companion object {
        private const val TIME_PATTERN_HOURLY_WEATHER_DATA = "ha"
        private const val TIME_PATTERN_PRECIPITATION_WEATHER_DATA = "ha"
        private const val TIME_PATTERN_MAIN_WEATHER_DATA = "EEE, h:mm a"
        private const val TIME_PATTERN_FORECAST_DATA = "EEE"

        private const val SEGMENT_COUNT_HOURLY_WEATHER_DATA = 8
        private const val SEGMENT_COUNT_PRECIPITATION_WEATHER_DATA = 8
        private const val SEGMENT_COUNT_FORECAST_WEATHER_DATA = 5
    }

    private val dataUnit = Unit.METRIC

    private lateinit var cityFromSearch: FavoriteCity

    private val _weatherData = MutableLiveData<List<WeatherData>>()
    private val _displayedToolbarInfo = MutableLiveData<Pair<String, String>>()
    private val _message = MutableLiveData<Event<String>>()
    private val _isCityAddedToFavorite = MutableLiveData<Event<Boolean>>()

    val weatherData: LiveData<List<WeatherData>> = _weatherData
    val displayedToolbarInfo: LiveData<Pair<String, String>> = _displayedToolbarInfo
    val message: LiveData<Event<String>> = _message
    val isCityAddedToFavorite: LiveData<Event<Boolean>> = _isCityAddedToFavorite

    fun getCurrentWeather(
        searchQuery: String? = null,
        cityIdToSearch: Int = -1
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _loading.postValue(true)

            var forecastResponse: Result<ForecastWeatherResponse>? = null
            var currentWeatherResponse: Result<CurrentWeatherResponse>? = null
            when {
                searchQuery != null -> {
                    forecastResponse = dataRepository.getForecast(searchQuery, dataUnit.value)
                    currentWeatherResponse = dataRepository.getCurrentWeather(searchQuery, dataUnit.value)
                }
                cityIdToSearch != -1 -> {
                    forecastResponse = dataRepository.getForecast(cityIdToSearch, dataUnit.value)
                    currentWeatherResponse = dataRepository.getCurrentWeather(cityIdToSearch, dataUnit.value)
                }
            }
            when {
                forecastResponse is Result.Success && currentWeatherResponse is Result.Success -> {
                    displayWeather(currentWeatherResponse.data, forecastResponse.data)
                    cityFromSearch = currentWeatherResponse.data.toFavoriteCity().also {
                        checkFavoriteCity(it)
                    }
                }
                forecastResponse is Result.Error -> displayError(forecastResponse.exception)
                currentWeatherResponse is Result.Error -> displayError(currentWeatherResponse.exception)
                forecastResponse == null || currentWeatherResponse == null -> displayError()
            }
        }
    }

    fun onAddDeleteOptionClick() {
        viewModelScope.launch(Dispatchers.IO) {
            val isCityAdded = dataRepository.isCityAdded(cityFromSearch.id)
            when {
                isCityAdded -> dataRepository.delete(cityFromSearch)
                else -> dataRepository.insert(cityFromSearch)
            }
            _isCityAddedToFavorite.postValue(Event(!isCityAdded))
        }
    }

    private fun displayWeather(
        currentWeather: CurrentWeatherResponse,
        forecast: ForecastWeatherResponse
    ) {
        //create current weather data
        val currentWeatherData = currentWeather.toMainWeatherData(TIME_PATTERN_MAIN_WEATHER_DATA, dataUnit)

        //create hourly weather data
        val hourlyDataTitle = app.getString(R.string.title_hourly_weather_data)
        val currentHourWeatherData =
            DisplayedWeatherItem(app.getString(R.string.text_time_now), currentWeather.main.temp.toInt())
        val hourlyWeatherData = forecast.toHourlyWeatherData(
            TIME_PATTERN_HOURLY_WEATHER_DATA,
            SEGMENT_COUNT_HOURLY_WEATHER_DATA,
            hourlyDataTitle,
            currentHourWeatherData
        )

        //create details weather data
        val detailsWeatherDataTitle = app.getString(R.string.title_details_weather_data)
        val detailsWeatherData = currentWeather.toDetailsWeatherData(detailsWeatherDataTitle, dataUnit)

        //create precipitation weather data
        val precipitationValue = when {
            currentWeather.rain != null -> currentWeather.rain.h.toInt()
            currentWeather.snow != null -> currentWeather.snow.h.toInt()
            else -> 0
        }
        val currentPrecipitationWeatherData = DisplayedWeatherItem(app.getString(R.string.text_time_now), precipitationValue)
        val precipitationWeatherDataTitle = app.getString(R.string.title_precipitation_weather_data)
        val precipitationWeatherData = forecast.toPrecipitationWeatherData(
            TIME_PATTERN_PRECIPITATION_WEATHER_DATA,
            SEGMENT_COUNT_PRECIPITATION_WEATHER_DATA,
            precipitationWeatherDataTitle,
            currentPrecipitationWeatherData
        )

        //create forecast data
        val forecastDataTitle = app.getString(R.string.title_forecast_weather_data)
        val forecastData = forecast.toForecastWeatherData(TIME_PATTERN_FORECAST_DATA, forecastDataTitle)

        val displayedWeather = listOf(
            currentWeatherData,
            hourlyWeatherData,
            detailsWeatherData,
            precipitationWeatherData,
            forecastData
        )
        _loading.postValue(false)
        _weatherData.postValue(displayedWeather)
        _displayedToolbarInfo.postValue(Pair(currentWeather.cityName, currentWeatherData.dayName))
    }

    private fun checkFavoriteCity(favoriteCity: FavoriteCity) {
        viewModelScope.launch(Dispatchers.IO) {
            val isCityAdded = dataRepository.isCityAdded(favoriteCity.id)
            _isCityAddedToFavorite.postValue(Event(isCityAdded))
        }
    }

    private fun displayError(ex: Exception? = null) {
        ex?.run { _error.postValue(Event(this)) }
        _displayedToolbarInfo.postValue(Pair(app.getString(R.string.text_common_error), ""))
        _loading.postValue(false)
        _weatherData.postValue(listOf())
    }

}
