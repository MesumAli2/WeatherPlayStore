package com.mesum.weather.network

import androidx.lifecycle.LiveData
import com.mesum.weather.forecastModel.NewForeccast
import com.mesum.weather.model.ForecastModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private const val BASE_URL ="https://api.tomorrow.io/v4/"


private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .build()

interface WeatherForecastInterface {
    @GET("timelines")
  suspend fun getWeather(@Query("location") location : String  , @Query("fields") field : String = "temperature", @Query("timesteps") timesteps: String = "1d", @Query("units") unit : String = "metric", @Query("apikey") alerts: String = "od6ZYat32hV2oXG4OudNyiB2SCMJcISI" ): NewForeccast
}


object WeatherForecastObject {
    val weatherForecastRequest = retrofit.create(WeatherForecastInterface::class.java)
}


