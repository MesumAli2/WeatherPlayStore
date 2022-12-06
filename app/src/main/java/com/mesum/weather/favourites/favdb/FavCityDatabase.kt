package com.mesum.weather.favourites.favdb

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [FavCitys::class], version = 2 , exportSchema = false)
  abstract class FavCityDatabase : RoomDatabase(){
    abstract fun FavDao(): FavDao
    companion object{
        @Volatile
        private var INSTANCE : FavCityDatabase? = null

        fun getDatabase(context : Context): FavCityDatabase{
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FavCityDatabase::class.java,
                    "fav_database"
                ).fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                //return instance
             return instance
            }
        }
    }
}