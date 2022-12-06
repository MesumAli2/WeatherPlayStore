package com.mesum.weather.favourites.favdb

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mesum.weather.Database.Citys
import kotlinx.coroutines.flow.Flow

@Dao
interface FavDao {
    @Query("Select * from FavouriteCitys")
    fun getCities(): Flow<List<FavCitys>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cityName : FavCitys)

    @Query("Delete from favouritecitys")
    suspend fun deleteAll()

}