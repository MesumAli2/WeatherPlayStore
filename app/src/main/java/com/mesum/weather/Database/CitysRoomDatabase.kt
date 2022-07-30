package com.mesum.weather.Database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Citys::class], version = 1 , exportSchema = false)
  abstract class CitysRoomDatabase : RoomDatabase(){
    abstract fun CitysDao(): CitysDao
    companion object{
        @Volatile
        private var INSTANCE : CitysRoomDatabase? = null

        fun getDatabase(context : Context): CitysRoomDatabase{
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CitysRoomDatabase::class.java,
                    "citys_database"
                ).build()

                INSTANCE = instance
                //return instance
             return instance
            }
        }
    }
}