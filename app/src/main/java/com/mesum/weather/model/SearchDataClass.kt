package com.mesum.weather.model

data class SearchDataClass (val ip : String,val  type : String, val continent_code : String, val country_code : String
           ,val country_name: String, val is_eu : Boolean,  val geoname_id : String,
val city: String, val region: String,val lat : Double, val lon: Double, val tz_id: String )


