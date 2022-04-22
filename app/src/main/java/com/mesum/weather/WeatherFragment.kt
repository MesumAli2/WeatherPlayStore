package com.mesum.weather

import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.mesum.weather.databinding.FragmentWeatherBinding
import com.mesum.weather.model.WeatherNetworkModel
import org.json.JSONObject
import java.io.IOException
import java.util.*


class WeatherFragment : Fragment() {

    private var _binding : FragmentWeatherBinding? = null
    private val binding get() = _binding!!
    private lateinit var homeRl : RelativeLayout
    private lateinit var loadingPB : ProgressBar
    private lateinit var weatherRV : RecyclerView
    private lateinit var backIcon: ImageView
    private lateinit var cityName: TextView
    private lateinit var temperature : TextView
    private lateinit var cityEdt : EditText
    private lateinit var condition : TextView
    private lateinit var search : ImageView
    private lateinit var iconIv : ImageView
    private lateinit var weatherRvModelArray : ArrayList<WeatherNetworkModel>
    private lateinit var weatherRvAdapter: WeatherRvAdapter
    private lateinit var locationManager : LocationManager
    private var permissionCode = 1
    private lateinit var cityNamen: String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentWeatherBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
         initVariable()
    }

    private fun initVariable() {
        homeRl = binding.home
        loadingPB = binding.Loading
        cityName = binding.cityName
        temperature = binding.tempTextview
        condition = binding.idCondition
        weatherRV = binding.RvWeather
        cityEdt = binding.edtCity
        search = binding.search
        weatherRvModelArray = ArrayList<WeatherNetworkModel>()
        weatherRvAdapter = WeatherRvAdapter(requireActivity().applicationContext, weatherRvModelArray)
        weatherRV.adapter = weatherRvAdapter
        locationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(requireActivity(),android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION )
            != PackageManager.PERMISSION_GRANTED){
ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION), permissionCode
            )
        }
        val location : Location? = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        if (location != null) {
            cityNamen = getCityName(long = location.longitude, lat = location.latitude)
        }
        getWeatherInfo("Karachi")
        binding.search.setOnClickListener {
            var city = cityEdt.text.toString()
            if (city.isEmpty()){
                Toast.makeText(activity, "Please Enter City", Toast.LENGTH_SHORT).show()

            }else{
                cityName.text = cityName.toString()
                getWeatherInfo(city)
            }
        }

}
    private fun getCityName(long : Double, lat : Double) : String{
        var cityName = "Not Found"
        val gcd = Geocoder(activity, Locale.getDefault())
        try {
            var address = gcd.getFromLocation(lat, long, 10)
            for (i in address){
                if (i != null){
                    var city = i.locality
                    if (city!=null && !city.equals("")){
                        cityName = city
                    }else{
                        Log.e("TAG", "city not found")
                        Toast.makeText(activity, "user city not found", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }catch ( e : IOException){
            e.printStackTrace()
        }
        return cityName

    }
    private fun getWeatherInfo( cityName : String){
        var url = "http://api.weatherapi.com/v1/forecast.json?key=eee62f028be24b1390a222114211209&q=$cityName&days=1&aqi=yes&alerts=yes\n"
        binding.cityName.text = cityName
        var requestQueue =Volley.newRequestQueue(activity)


        val jsonObjectREquest = JsonObjectRequest(
            Request.Method.GET, url, null,
            {
                loadingPB.visibility = View.GONE
            homeRl.visibility =View.VISIBLE
                weatherRvModelArray.clear()

                var temp : String = it.getJSONObject("current").getString("temp_c")
temperature.text = "$temp C"

                var isDay = it.getJSONObject("current").getInt("is_day")
                var condition = it.getJSONObject("current").getJSONObject("condition").getString("text")
                var cdnicon = it.getJSONObject("current").getJSONObject("condition").getString("icon")
                var forecast = it.getJSONObject("forecast")
                var forecasr0 = forecast.getJSONArray("forecastday").getJSONObject(0)
                var hourarray = forecasr0.getJSONArray("hour")

            }
        ) {
            Toast.makeText(activity, "Please enter valid city", Toast.LENGTH_SHORT).show()
        }

        requestQueue.add(jsonObjectREquest)
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == permissionCode){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(activity, "Permission Granted", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(activity, "Please Provide permission", Toast.LENGTH_SHORT).show()

            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}