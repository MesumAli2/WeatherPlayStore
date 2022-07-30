package com.mesum.weather.Database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Citys")
data class Citys (
    @PrimaryKey(autoGenerate = true) val id : Int? = null,
    @ColumnInfo(name = "city_name") val cityName : String
    )