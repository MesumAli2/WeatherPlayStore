package com.mesum.weather.Database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CitysDao {
    @Query("Select * from citys")
    fun getCities(): Flow<List<Citys>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cityName : Citys)

    @Query("Delete from citys")
    suspend fun deleteAll()
}