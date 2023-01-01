package com.mesum.weather.model.forecastModel

data class Timeline(
    val endTime: String,
    val intervals: List<Interval>,
    val startTime: String,
    val timestep: String
)