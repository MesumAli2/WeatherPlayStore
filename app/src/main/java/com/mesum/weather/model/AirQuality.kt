package com.mesum.weather.model

import com.google.gson.annotations.SerializedName

data class AirQuality(
    val co: Double,
    val ind: Int,
    val no2: Double,
    val o3: Double,
    val pm10: Double,
    val pm2_5: Double,
    val so2: Double,
    val epa: Int
)