package com.mesum.weather.model

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.mesum.weather.Database.Citys
import com.mesum.weather.Database.CitysRepository
import com.mesum.weather.Database.CitysRoomDatabase
import com.mesum.weather.forecastModel.Interval
import com.mesum.weather.forecastModel.NewForeccast
import com.mesum.weather.network.WeatherForecastObject
import com.mesum.weather.network.WeatherObject
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.IllegalArgumentException
import java.lang.StringBuilder

class WeatherViewModel(val repository: CitysRepository) : ViewModel() {
     val weatherResponse : MutableLiveData<ForecastModel> = MutableLiveData<ForecastModel>()
    var futureWeatherResponse : MutableLiveData<NewForeccast> = MutableLiveData<NewForeccast>()



 //   private val repository = CitysRepository(CitysRoomDatabase.getDatabase(context = ctc).CitysDao())

    val allCitys : LiveData<List<Citys>> = repository.allCitys.asLiveData()

     fun insert(city: Citys) {
         viewModelScope.launch {
             repository.insert(city)
         }
     }

    fun deleteAll(){
        viewModelScope.launch {
            repository.deleteAll()
        }
    }


    fun fetchResponse(city : String ){
        Log.d("weatherModel", "Network Initiated")

        val weatherRp = WeatherObject.weatherRequest.getWeather(cityName = city)
        weatherRp.enqueue(object  : Callback<ForecastModel> {
            override fun onResponse(
                call: Call<ForecastModel>,
                response: Response<ForecastModel>
            ) {
              //  Log.d("weatherModel", response.body()?.forecast?.forecastday?.get(0)?.date.toString())
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
    init {
        getLocation()
    }
    fun getLocation(){
        val lp = WeatherObject.weatherRequest.getLocationResult()
        lp.enqueue(object : Callback<List<SearchDataClass>>{
            override fun onResponse(call: Call<List<SearchDataClass>>, response: Response<List<SearchDataClass>>) {
                Log.d("LocationLookUpResponse", response.body().toString())
            }

            override fun onFailure(call: Call<List<SearchDataClass>>, t: Throwable) {
                Log.d("LocationLookUpResponse", "Location Error is  : " +t.message.toString())

            }

        })
    }



    fun fetchFutureResponse(lat : Double, Long : Double) {
        var latLong = "${lat.toString()},${Long.toString()}"
        Log.d("weatherModel", "Network Initiated")
        viewModelScope.launch {
            val weatherRp = WeatherForecastObject.weatherForecastRequest.getWeather(latLong)
            try {
                futureWeatherResponse.value = weatherRp
            }catch (e : java.lang.Exception){
                Log.d("exceptionFound", e.toString())
            }


        }


        //val weatherRp = WeatherForecastObject.weatherForecastRequest.getWeather(latLong)
       /* weatherRp.enqueue(object  : Callback<NewForeccast> {
            override fun onResponse(
                call: Call<NewForeccast>,
                response: Response<NewForeccast>
            ) {
                Log.d("weatherModel", "Network Initiated inside callback ")
                if (response.body() != null){
                    futureWeatherResponse.value = response.body()
                    Log.d("weatherModelForecast", response.body().toString())


                }else{
                    Log.d("weatherModelForecast", response.body().toString())

                }


                try {
                    if (response.isSuccessful){
                        futureWeatherResponse.value = response.body()


                    }
                }catch (e : Exception){
                    Log.d("weatherModel", "No response error is :${e.message.toString()}")
                }
            }

            override fun onFailure(call: Call<NewForeccast>, t: Throwable) {
                Log.d("weatherModel", "Network Failure ${t.toString()}")
            }

        })*/
    }

    class WeatherViewModelFactory(private val repository: CitysRepository ): ViewModelProvider.Factory{
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WeatherViewModel::class.java)){
                return WeatherViewModel(repository = repository) as T
            }
            throw  IllegalArgumentException("Unknown ViewModel class")
        }

    }


}