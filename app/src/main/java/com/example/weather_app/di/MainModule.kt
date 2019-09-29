package com.example.weather_app.di

import com.example.weather_app.ui.base.shared.WeatherSharedViewModel
import com.example.weather_app.ui.favorites.FavoritesCitiesViewModel
import com.example.weather_app.ui.weather.WeatherViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { WeatherViewModel(get(), get()) }
    viewModel { WeatherSharedViewModel(get()) }
    viewModel { FavoritesCitiesViewModel(get(), get()) }
}

val weatherModules = listOf(viewModelModule, networkModule, dataModule)