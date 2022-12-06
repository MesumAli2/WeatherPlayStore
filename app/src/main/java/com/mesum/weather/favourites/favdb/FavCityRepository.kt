package com.mesum.weather.favourites.favdb

import androidx.annotation.WorkerThread
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

class FavCityRepository(private val dao: FavDao ) {



    val allFavCity : Flow<List<FavCitys>> = dao.getCities()

    @WorkerThread
    suspend fun insertFav(city : FavCitys){
        dao.insert(city)
    }

    @WorkerThread
    suspend fun deleteAllFav(){
        dao.deleteAll()
    }
}