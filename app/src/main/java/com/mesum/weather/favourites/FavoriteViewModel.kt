package com.mesum.weather.favourites

import android.util.Log
import androidx.lifecycle.*
import com.mesum.weather.Database.Citys
import com.mesum.weather.Database.CitysRepository
import com.mesum.weather.favourites.favdb.FavCityRepository
import com.mesum.weather.favourites.favdb.FavCitys
import com.mesum.weather.forecastModel.NewForeccast
import com.mesum.weather.model.ForecastModel
import com.mesum.weather.model.SearchDataClass
import com.mesum.weather.model.WeatherViewModel
import com.mesum.weather.network.WeatherForecastObject
import com.mesum.weather.network.WeatherObject
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.IllegalArgumentException

class FavoriteViewModel(val repository: FavCityRepository) : ViewModel() {
    val weatherResponseFav : MutableLiveData<ForecastModel> = MutableLiveData<ForecastModel>()
    var futureWeatherResponse : MutableLiveData<NewForeccast> = MutableLiveData<NewForeccast>()



    //   private val repository = CitysRepository(CitysRoomDatabase.getDatabase(context = ctc).CitysDao())

    val allCitys : LiveData<List<FavCitys>> = repository.allFavCity.asLiveData()

    fun insert(city: FavCitys) {
        viewModelScope.launch {
            repository.insertFav(city)
        }
    }

    fun deleteAll(){
        viewModelScope.launch {
            repository.deleteAllFav()
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
                weatherResponseFav.value = response.body()

                try {
                    if (response.isSuccessful){
                        weatherResponseFav.value = response.body()

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
        lp.enqueue(object : Callback<List<SearchDataClass>> {
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

    class FavouriteViewModelFactory(private val repository: FavCityRepository): ViewModelProvider.Factory{
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FavoriteViewModel::class.java)){
                return FavoriteViewModel(repository = repository) as T
            }
            throw  IllegalArgumentException("Unknown ViewModel class")
        }

    }


}
