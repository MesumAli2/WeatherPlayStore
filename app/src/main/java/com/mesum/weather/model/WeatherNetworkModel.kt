package com.mesum.weather.model

data class WeatherNetworkModel(
    var time : String,
    var temperature: String,
    var icon : String,
    var windSpeed : String
)