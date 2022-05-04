package com.mesum.weather.model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mesum.weather.network.WeatherObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WeatherViewModel : ViewModel() {
     val weatherResponse : MutableLiveData<ForecastModel> = MutableLiveData<ForecastModel>()

    fun fetchResponse(){
        Log.d("weatherModel", "Network Initiated")

        val weatherRp = WeatherObject.weatherRequest.getWeather(cityName = "Dubai")
        weatherRp.enqueue(object  : Callback<ForecastModel> {
            override fun onResponse(
                call: Call<ForecastModel>,
                response: Response<ForecastModel>
            ) {
                Log.d("weatherModel", "Network Initiated inside callback ")
                weatherResponse.value = response.body()

                try {
                    if (response.isSuccessful){
                            weatherResponse.value = response.body()

                    }
                }catch (e : Exception){
                    Log.d("weatherModel", "No response error is :${e.message.toString()}")
                }
            }

            override fun onFailure(call: Call<ForecastModel>, t: Throwable) {
            Log.d("weatherModel", "Network Failure ${t.toString()}")
            }

        })
    }
}