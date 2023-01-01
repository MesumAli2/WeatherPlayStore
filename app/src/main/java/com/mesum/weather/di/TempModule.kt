package com.mesum.weather.di

import android.content.Context
import com.mesum.weather.Database.CitysDao
import com.mesum.weather.Database.CitysRoomDatabase
import com.mesum.weather.Database.favdb.FavCityDatabase
import com.mesum.weather.Database.favdb.FavDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object TempModule {

    @Singleton
    @Provides
    fun getTemp(@ApplicationContext context : Context) : CitysRoomDatabase {
        return CitysRoomDatabase.getDatabase(context)
    }

    @Singleton
    @Provides
    fun getTempDao(appDb : CitysRoomDatabase) : CitysDao {
        return appDb.CitysDao()
    }


//    @InstallIn(SingletonComponent::class)
//    @Module
//    abstract class LoggingModule {
//
//        @Singleton
//        @Binds
//        abstract fun bindLogDao(impl: FavCityRepository): FavDao
//
//    }

}