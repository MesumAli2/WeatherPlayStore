package com.mesum.weather.Database.favdb

import androidx.annotation.WorkerThread
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FavCityRepository @Inject constructor(private val dao: FavDao) {



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