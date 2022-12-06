package com.mesum.weather

import com.mesum.weather.Database.Citys
import com.mesum.weather.model.ForecastModel
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun add_city() {
        val city = Citys(cityName = "Dubai")
        assertTrue("The value of city added was inCorrect", city.cityName == "Dubai")
    }
}