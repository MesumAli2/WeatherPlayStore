package com.mesum.weather.Database

import androidx.annotation.WorkerThread
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CitysRepository @Inject constructor(private val dao: CitysDao ) {



    val allCitys : Flow<List<Citys>> = dao.getCities()

    @WorkerThread
    suspend fun insert(city : Citys){
        dao.insert(city)
    }

    @WorkerThread
    suspend fun deleteAll(){
        dao.deleteAll()
    }
}