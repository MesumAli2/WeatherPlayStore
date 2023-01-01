package com.mesum.weather.di

import android.content.Context
import com.mesum.weather.Database.favdb.FavCityDatabase
import com.mesum.weather.Database.favdb.FavCityRepository
import com.mesum.weather.Database.favdb.FavDao
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object FavModule {

    @Singleton
    @Provides
    fun getFavDb(@ApplicationContext context : Context) : FavCityDatabase {
        return FavCityDatabase.getDatabase(context)
    }

    @Singleton
    @Provides
    fun getDao(appDb : FavCityDatabase) : FavDao {
        return appDb.FavDao()
    }



}