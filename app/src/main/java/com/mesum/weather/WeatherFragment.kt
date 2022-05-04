package com.mesum.weather

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.TextView.OnEditorActionListener
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.*
import com.mesum.weather.databinding.FragmentWeatherBinding
import com.mesum.weather.model.*
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class WeatherFragment : Fragment() {

    private var _binding : FragmentWeatherBinding? = null
    private val binding get() = _binding!!
    private lateinit var homeRl : RelativeLayout
    private lateinit var loadingPB : ProgressBar
    private lateinit var mFusedLocationClient: FusedLocationProviderClient;
    private lateinit var cityName: TextView
    private var weatherRvModelArray : ArrayList<WeatherNetworkModel> = ArrayList<WeatherNetworkModel>()
    private var weatherRvHourly : MutableList<Hour> = mutableListOf<Hour>()
    private val viewModel : WeatherViewModel by activityViewModels<WeatherViewModel>()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentWeatherBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity as  AppCompatActivity)

        homeRl = binding.home
        loadingPB = binding.Loading
        cityName = binding.cityName

        getLocationPermission()

        if (mLocationPermissionsGranted){
          // getLocation()
        }
          //  setupUIInteraction()
            //setupUIInteraction()
        viewModel.fetchResponse()
        viewModel.weatherResponse.observe(viewLifecycleOwner){
            if (it != null){
                setUi(it)
            }
        }

    }

    private fun setUi(it: ForecastModel) {
        binding.cityName.text = it.location.name
        binding.tempTextview.text = "${it.current.temp_c}°C"
        binding.idIvicon.load( "http:" + it.current.condition.icon)
        binding.idCondition.text = it.current.condition.text
        weatherRvHourly.addAll(it.forecast.forecastday[0].hour)
        setupRvAdapter(weatherRvHourly)
        setBackGround(it.current.is_day)

    }
    class RvViewHolder(val view: View) : RecyclerView.ViewHolder(view){}

    private fun setupRvAdapter(weatherRvHourly: MutableList<Hour>) {
        val recyclerViewAdapter = object : RecyclerView.Adapter<RvViewHolder>(){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RvViewHolder {
                return RvViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.weather_rv_item, parent, false))
                }

            override fun onBindViewHolder(holder: RvViewHolder, position: Int) {
                val result = weatherRvHourly[position]
                holder.itemView.findViewById<ImageView>(R.id.cdn).load("http:" + result.condition.icon)
                val input = SimpleDateFormat("yyyy-MM-DD hh:mm")
                val output = SimpleDateFormat("hh:mm")
                val display =  input.parse(result.time)
                holder.itemView.findViewById<TextView>(R.id.Time).text = output.format(display)
                holder.itemView.findViewById<TextView>(R.id.temp).text = "${result.temp_c} °C"
                holder.itemView.findViewById<TextView>(R.id.wind_speed).text = "${result.wind_kph} km/h"                }

            override fun getItemCount(): Int {
                    return weatherRvHourly.size
            }

        }
        binding.RvWeather.adapter = recyclerViewAdapter
    }


    /* private fun setUpPlacesSearch() {

         // Initialize the AutocompleteSupportFragment.
         val autocompleteFragment =
            childFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                     as AutocompleteSupportFragment

         Places.initialize(context, )
         // Specify the types of place data to return.
         autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME))

         // Set up a PlaceSelectionListener to handle the response.
         autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
             override fun onPlaceSelected(place: Place) {
                 Log.i(TAG, "Place: ${place.name}, ${place.id}")
             }

             override fun onError(p0: Status) {
                 Log.d(TAG, "Fragment not started :  ${p0.toString()}")
             }


         })
     }*/


    private fun setupUIInteraction() {
        val keyboard = (activity as AppCompatActivity).getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager

        binding.edtCity.setOnEditorActionListener(object  : OnEditorActionListener{
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {

                if (event != null && event.keyCode === KeyEvent.KEYCODE_ENTER || actionId == EditorInfo.IME_ACTION_DONE) {

                    Log.i(TAG, "Enter pressed")
                    if (v != null) {
                        getWeatherInfo(v.text.toString())
                    }

                    keyboard.hideSoftInputFromWindow(view?.windowToken, 0)
                }
                return false
            }

        })
        binding.search.setOnClickListener {
            if (binding.edtCity.text!!.isEmpty()){
                Toast.makeText(activity, "Please Enter City", Toast.LENGTH_SHORT).show()
            }else{
                cityName.text = cityName.toString()
                getWeatherInfo(binding.edtCity.text.toString())
                keyboard.hideSoftInputFromWindow(view?.windowToken, 0)

            }
        }
    }



    private fun getCityName(long : Double, lat : Double) : String{
        var cityName : String = "Not Found"
           val gcd = Geocoder(activity as AppCompatActivity, Locale.getDefault())
           try {
               val address = gcd.getFromLocation(lat, long, 10)
               for (i in address){
                   if (i != null){
                       val city = i.locality
                       if (city!=null && !city.equals("")){
                           cityName = city
                       }else{
                           Log.e("TAG", "city not found")

                       }
                   }
               }
           }catch ( e : IOException){
               e.printStackTrace()
           }


        return cityName

        }
        @SuppressLint("NotifyDataSetChanged")
        private fun getWeatherInfo(cityName : String){
            val url = "http://api.weatherapi.com/v1/forecast.json?key=eee62f028be24b1390a222114211209&q=$cityName&days=1&aqi=yes&alerts=yes\n"
            binding.cityName.text = cityName
            val requestQueue =Volley.newRequestQueue(activity)

            val jsonObjectREquest = JsonObjectRequest(
                Request.Method.GET, url, null,
                {
                    loadingPB.visibility = View.GONE
                    homeRl.visibility =View.VISIBLE
                    weatherRvModelArray.clear()
                    val temp : String = it.getJSONObject("current").getString("temp_c")
                    binding.tempTextview.text = "$temp °C"
                    var isDay = it.getJSONObject("current").getInt("is_day")
                    setBackGround(isDay)
                    val condition = it.getJSONObject("current").getJSONObject("condition").getString("text")
                    val cdnicon = it.getJSONObject("current").getJSONObject("condition").getString("icon")
                    val forecast = it.getJSONObject("forecast")
                    val forecasr0 = forecast.getJSONArray("forecastday").getJSONObject(0)
                    val hourarray = forecasr0.getJSONArray("hour")
                    binding.idCondition.text = condition
                    binding.idIvicon.load("http:" + cdnicon)

                    for (i in 0 until hourarray.length()) {
                        val hourObject: JSONObject = hourarray.getJSONObject(i)
                        val time = hourObject.getString("time")
                        val temp = hourObject.getString("temp_c")
                        val img = hourObject.getJSONObject("condition").getString("icon")
                        val wind = hourObject.getString("wind_kph")
                        weatherRvModelArray.add( WeatherNetworkModel(time, temp, img, wind))
                    }
                        setUpAdapter(weatherRvModelArray)
                  }
                ) {
                Toast.makeText(activity, "Please enter valid city", Toast.LENGTH_SHORT).show()
                }

            requestQueue.add(jsonObjectREquest)
        }

    private fun setUpAdapter(weatherRvModelArray: ArrayList<WeatherNetworkModel>)  {
        val adapter = object : RecyclerView.Adapter<RvViewHolder>(){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RvViewHolder {
                return RvViewHolder(LayoutInflater.from(context).inflate(R.layout.weather_rv_item, parent, false))
            }
            override fun onBindViewHolder(holder: RvViewHolder, position: Int) {
                val result = weatherRvModelArray[position]
                holder.itemView.findViewById<ImageView>(R.id.cdn).load("http:" + result.icon)
                val input = SimpleDateFormat("yyyy-MM-DD hh:mm")
                val output = SimpleDateFormat("hh:mm")
                val display =  input.parse(result.time)
                holder.itemView.findViewById<TextView>(R.id.Time).text = output.format(display)
                holder.itemView.findViewById<TextView>(R.id.temp).text = "${result.temperature} °C"
                holder.itemView.findViewById<TextView>(R.id.wind_speed).text = "${result.windSpeed} km/h"
            }
            override fun getItemCount(): Int {
              return  weatherRvModelArray.size
            }
        }
        binding.RvWeather.adapter = adapter
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setBackGround(day: Int) {
        if (day == 1){
            binding.black.setImageDrawable(resources.getDrawable(R.drawable.day))
        }else{
            binding.black.setImageDrawable(resources.getDrawable(R.drawable.night))
        }

    }










    private final val TAG = "MaoActivity"
    private final val FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
    private final val COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION
    private final var mLocationPermissionsGranted = false
    private val LOCATION_PERMISSION_CODE = 1234


    private fun getLocationPermission(){
        val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION)

        if (ContextCompat.checkSelfPermission(requireActivity().applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            Log.d(TAG, "Permission FINE_LOCATION Exists")

            if (ContextCompat.checkSelfPermission(requireActivity().applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionsGranted = true
                Log.d(TAG, "Permission COARSE Exists")


            }else{
                ActivityCompat.requestPermissions(requireActivity() , permissions, LOCATION_PERMISSION_CODE)
            }
        }else{
            ActivityCompat.requestPermissions(requireActivity(), permissions, LOCATION_PERMISSION_CODE)
        }
    }

    //sets the mLocationPermissionsGranted to true if permisssion granted
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Log.d(TAG, "Permission Request Called")
        mLocationPermissionsGranted = false

        when(requestCode){
            LOCATION_PERMISSION_CODE ->{
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                    mLocationPermissionsGranted = true
                    if (ActivityCompat.checkSelfPermission(
                            activity as AppCompatActivity,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            activity as AppCompatActivity,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return
                    }

                    mFusedLocationClient.lastLocation.addOnCompleteListener { task ->
                        val location : Location = task.result
                        if (task.isSuccessful){
                            if (location != null){
                                Log.d("weatherFragment", location.longitude.toString())
                                getWeatherInfo(getCityName(long = location.longitude, lat = location.latitude))
                            }
                        }

                    }

                }else {
                    Toast.makeText(activity, "No permission habibe", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Permission Failed")
                    mLocationPermissionsGranted = false
                    Toast.makeText(activity, "You need to provide location to \n get accurate weather updates", Toast.LENGTH_SHORT).show()
                    return
                }



                }
            }
        }


    private fun getLocation(){
        val mLocationRequest: LocationRequest = LocationRequest.create()
        mLocationRequest.interval = 600000
        mLocationRequest.fastestInterval = 10
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val mLocationCallback: LocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (locationResult == null) {
                    return
                }
                for (location in locationResult.locations) {
                    if (location != null) {
                        getWeatherInfo(getCityName(long = location.longitude, lat = location.latitude))
                        Log.d(TAG, "The Location updates is :${location.longitude.toString()}") }

                }
            }
        }
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(activity as AppCompatActivity, arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION), 10)
        }
        try {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null)
            mFusedLocationClient.lastLocation.addOnSuccessListener { location ->
                getWeatherInfo(getCityName(long = location.longitude, lat = location.latitude))

                //Log.d(TAG, "Start Initial LastLocation is ${it.toString()}")
        } }catch (e : SecurityException){
            Log.d(TAG, "Location not available ${e.message.toString()}")
        }
    }


}