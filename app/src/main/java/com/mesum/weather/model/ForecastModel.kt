package com.mesum.weather.model

data class ForecastModel(
    val alerts: Alerts,
    val current: Current,
    val forecast: Forecast,
    val location: Location
)