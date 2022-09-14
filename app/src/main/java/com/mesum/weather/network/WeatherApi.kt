package com.mesum.weather.network

import com.mesum.weather.model.Alerts
import com.mesum.weather.model.ForecastModel
import com.mesum.weather.model.SearchDataClass
import com.mesum.weather.model.WeatherNetworkModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private const val BASE_URL ="http://api.weatherapi.com/v1/"


    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    interface WeatherInterface {
        @GET("forecast.json")
        fun getWeather(@Query("key") key : String = "eee62f028be24b1390a222114211209", @Query("q") cityName: String, @Query("days") days: String = "10", @Query("aqi") aqi : String = "air_quality", @Query("alerts") alerts: String = "yes" ): Call<ForecastModel>



        @GET("search.json ")
        fun getLocationResult(@Query("key") key : String = "eee62f028be24b1390a222114211209", @Query("ip") cityName: String = "66.249.70.29" ) : Call<List<SearchDataClass>>
        @GET("")
       fun gunGetWeatherRP() : Call<ForecastModel>
    }


     object WeatherObject {
        val weatherRequest = retrofit.create(WeatherInterface::class.java)
    }


