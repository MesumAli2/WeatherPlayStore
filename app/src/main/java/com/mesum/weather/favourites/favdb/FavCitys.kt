package com.mesum.weather.favourites.favdb

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "FavouriteCitys")
data class FavCitys (
    @PrimaryKey(autoGenerate = false) val cityNmid : String,
    @ColumnInfo(name = "city") val cityName : String
    )