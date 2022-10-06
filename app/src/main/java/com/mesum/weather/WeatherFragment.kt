package com.mesum.weather

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnCompleteListener

import com.google.firebase.messaging.FirebaseMessaging
import com.mesum.weather.Database.CitysRepository
import com.mesum.weather.Database.CitysRoomDatabase
import com.mesum.weather.databinding.FragmentWeatherBinding
import com.mesum.weather.model.*
import java.io.IOException
import java.text.DecimalFormat
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
    var searchstate = false
    private lateinit var repository : CitysRepository
    private lateinit var viewModel: WeatherViewModel
    private lateinit var weatherViewPager : WeatherViewPager
    private var cityNameLocation : String? = null



    private  val TAG = "MaoActivity"

    private  var mLocationPermissionsGranted = false
    private val LOCATION_PERMISSION_CODE = 1234

    class RvViewHolder(val view: View) : RecyclerView.ViewHolder(view)


    val diif  =  object  : DiffUtil.ItemCallback<Hour>(){
        override fun areItemsTheSame(oldItem: Hour, newItem: Hour): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Hour, newItem: Hour): Boolean {
            return oldItem.time == newItem.time
        }

    }

    val diif2  =  object  : DiffUtil.ItemCallback<Forecastday>(){
        override fun areItemsTheSame(oldItem: Forecastday, newItem: Forecastday): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Forecastday, newItem: Forecastday): Boolean {
            return oldItem.day.avgtemp_c == newItem.day.avgtemp_c
        }

    }
    val recyclerViewAdapter = object : ListAdapter<Hour,RvViewHolder>(diif ){

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RvViewHolder {
            return RvViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.weather_rv_item, parent, false))
        }

        override fun onBindViewHolder(holder: RvViewHolder, position: Int) {
            val result = getItem(position)
            holder.itemView.findViewById<ImageView>(R.id.cdn).load("http:" + result.condition.icon)
            val input = SimpleDateFormat("yyyy-MM-DD hh:mm")
            val output = SimpleDateFormat("h aa")
            val display =  input.parse(result.time)
            holder.itemView.findViewById<TextView>(R.id.Time).text = output.format(display)
            holder.itemView.findViewById<TextView>(R.id.temp).text = "${trimLeadingZeros(result.temp_c)}°"
            holder.itemView.findViewById<TextView>(R.id.wind_speed).text = "${trimLeadingZeros(result.wind_kph)} km/h"
        }

    }
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


        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
            val msg = "Firebase token is $token"
            Log.d(TAG, msg)
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        })
        activity?.window?.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

      repository =  CitysRepository(CitysRoomDatabase.getDatabase(context = this.requireContext()).CitysDao())
      viewModel  = ViewModelProvider(this, WeatherViewModel.WeatherViewModelFactory(repository))
                .get(WeatherViewModel::class.java)




        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity as  AppCompatActivity)
       // setUpBottomBar()
      //  loadingPB = binding.Loading
      //  cityName = binding.cityName

        getLocationPermission()

        if (mLocationPermissionsGranted){
            //Toast.makeText(activity, "got permission", Toast.LENGTH_SHORT).show()
          getLocation()
           //Toast.makeText(activity, "Location granted", Toast.LENGTH_SHORT).show()
            mFusedLocationClient.lastLocation.addOnCompleteListener { task ->
                val location : Location? = task.result
                if (task.isSuccessful){
                    if (location != null){
                        Log.d("weatherFragment", location.longitude.toString())
                       viewModel.fetchResponse(getCityName(long = location.longitude, lat = location.latitude))
                        cityNameLocation = getCityName(long = location.longitude, lat = location.latitude)

                    }
                }
            }




        }

     //   setupUIInteraction()

        val viewpager = binding.viewPager
        val arraytemp = arrayListOf<ForecastModel>()

        viewModel.allCitys.observe(viewLifecycleOwner){
            for (i in it){
                if (!arraytemp.isNullOrEmpty()){
                    for (a in arraytemp){
                        //if city dose not exist in array then fetch for the reponse

                        if (a.location.name != i.cityName){
                            viewModel.fetchResponse(i.cityName.toString())

                        }
                    }
                }else{
                    viewModel.fetchResponse(i.cityName.toString())

                }

            }
        }
        viewModel.weatherResponse.observe(viewLifecycleOwner){

             var firstime = false
            if (it != null){

                Log.d("LatLongLog", "${it.location.lat}, ${it.location.lon}")

               if (!arraytemp.contains(it)){
                   if (it.location.name == cityNameLocation){
                       arraytemp.add(0, it)
                   }else{

                       arraytemp.add(it)

                   }
               }

                val distinc = arraytemp.distinct()
                weatherViewPager = WeatherViewPager(weatherlist = arraytemp, viewModel = viewModel, ctx = requireContext(), childFragmentManager = childFragmentManager , activity =  activity as MainActivity, findNanControlle = findNavController())
                weatherViewPager.submitList(distinc)
                viewpager.smoothScrollBy(10,10)


                weatherViewPager.notifyDataSetChanged()
                viewpager.setAdapter(weatherViewPager)
               // viewpager.setLayoutManager(ViewPagerLayoutManager);
              //  val snapHelper = GravitySnapHelper(Gravity.START)
             //   snapHelper.attachToRecyclerView(viewpager)

                if (arguments?.getBoolean("Added") == true){
                    viewpager.scrollToPosition(distinc.size - 1  )

                }
                viewpager.removeItemDecoration(CirclePagerIndicatorDecoration())

                viewpager.addItemDecoration(CirclePagerIndicatorDecoration())
              //  val indicator = binding.indicator
                    //  indicator.setViewPager(viewpager)
               // weatherViewPager.registerAdapterDataObserver(indicator.getAdapterDataObserver());

               // val recyclerIndicator: ScrollingPagerIndicator = binding.indicator
               // recyclerIndicator.attachToRecyclerView(recyclerView)
              //  recyclerIndicator.add(viewpager)
            //    weatherViewPager = this.context?.let { it1 -> WeatherViewPager(it1, arraytemp) }
                Log.d("WeatherResponse", arraytemp.toString())


                //binding.viewPager.setAdapter(weatherViewPager)


                Log.d("WeatherResponse", it.toString())
            }
        }


       /* binding.addWeather.setOnClickListener {
                findNavController().navigate(R.id.addFragment)
          //  showInputMethod(view)

        }*/

    }


    private fun showInputMethod(view: View) {
        val imm: InputMethodManager? = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (imm != null) {
            imm.showSoftInput(view, 0)
        }
    }
   /* private fun setForecast(forecastday: List<Forecastday>) {
        val listInterval = mutableListOf<Forecastday>()
        val rvAdapter = object : ListAdapter<Forecastday,RvViewHolder>(diif2 ){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RvViewHolder {
                return RvViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_forecast, parent, false))
            }
            override fun onBindViewHolder(holder: RvViewHolder, position: Int) {
                val result = getItem(position)
                val input = SimpleDateFormat( "yyyy-MM-dd")
                val output = SimpleDateFormat("EEEE")
                val display =  input.parse(result.date.toString())
               holder.itemView.findViewById<TextView>(R.id.curr_day).text = output.format(display)
               // result.day.condition.
             //   holder.itemView.findViewById<TextView>(R.id.curr_day).text = result.date.toString()
                //holder.itemView.findViewById<TextView>(R.id.curr_day).text = result.startTime
                holder.itemView.findViewById<TextView>(R.id.curr_temp_low).text = "${trimLeadingZeros(result.day.mintemp_c)}°"
                holder.itemView.findViewById<TextView>(R.id.curr_temp_high).text = "${trimLeadingZeros(result.day.maxtemp_c)}°"
                holder.itemView.findViewById<ImageView>(R.id.weather_image).load("http:" + result.day.condition.icon)
            }
        }
        binding.weatherForecast.visibility = View.VISIBLE

        rvAdapter.submitList(forecastday)
        binding.rvForecast.adapter = rvAdapter


      /*  viewModel.futureWeatherResponse.observe(viewLifecycleOwner){
            if (it != null){
              //  val weatherHourly5DayList = it.data.timelines[0].intervals
              //  val adapter = WeatherRvAdapter(weatherHourly5DayList)
             //   binding.rvForecast.adapter = adapter

                var count = 0;
                listInterval.clear()
                for (i in it.data.timelines[0].intervals){
                    if (count > 0){
                        listInterval.add(i)
                    }
                    count++
                }
                binding.weatherForecast.visibility = View.VISIBLE

                rvAdapter.submitList(listInterval)
                binding.rvForecast.adapter = rvAdapter


            }

        }*/

      /*  if (!listInterval.isNullOrEmpty()){
            binding.weatherForecast.visibility = View.VISIBLE
        }
        else{

            binding.weatherForecast.visibility = View.GONE
        }*/

        //binding.rvForecast.adapter = rvAdapter


    }

    private fun buildGraph(weatherRvHourly: MutableList<Hour>) {
        val recyclerViewAdapter = object : ListAdapter<Hour,RvViewHolder>(diif ){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RvViewHolder {
                return RvViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.rain_item, parent, false))
            }
            override fun onBindViewHolder(holder: RvViewHolder, position: Int) {
                val result = getItem(position)
                val input = SimpleDateFormat("yyyy-MM-DD hh:mm")
                val output = SimpleDateFormat("h aa")
                val display =  input.parse(result.time)
                holder.itemView.findViewById<TextView>(R.id.time).text = output.format(display)
                holder.itemView.findViewById<TextView>(R.id.percent).text = "${result.chance_of_rain}%"
                holder.itemView.findViewById<ProgressBar>(R.id.progress_bar).progress = result.chance_of_rain
            }
        }

        val newlist = weatherRvHourly.filter { it.chance_of_rain != 0 }
        if (newlist.isNotEmpty()){
            binding.rainText.visibility = View.VISIBLE
            binding.rainRecyclerView.visibility = View.VISIBLE
        }
        else{
            binding.rainText.visibility = View.GONE
            binding.rainRecyclerView.visibility = View.GONE
        }
        recyclerViewAdapter.submitList(newlist)
        binding.rainRecyclerView.adapter = recyclerViewAdapter

    }


    private fun setUi(it: ForecastModel) {
        setForecast(it.forecast.forecastday)
        var weatherRvHourly : MutableList<Hour> = mutableListOf()
        binding.cityName.text = it.location.name
        binding.textDown.text = "L:${trimLeadingZeros(it.forecast.forecastday[0].day.mintemp_c)}°"
        binding.textUp.text = "H:${trimLeadingZeros(it.forecast.forecastday[0].day.maxtemp_c)}°"

        binding.tempTextview.text = "${trimLeadingZeros(it.current.temp_c)}°"
        binding.idIvicon.load( "http:" + it.current.condition.icon)
        binding.idCondition.text = it.current.condition.text
        val timelist = mutableListOf<String>()

        Log.d("WeatherResponse", it.location.localtime.toString())
        //add time to weatherlist array
        for (i in it.forecast.forecastday[0].hour){
            val input = SimpleDateFormat("yyyy-MM-DD hh:mm")
            val output = SimpleDateFormat("hh aa")
            val display =  input.parse(i.time)
            val weatherTimes =  output.format(display)

            timelist.add(weatherTimes)

        }
        val input = SimpleDateFormat("yyyy-MM-DD hh:mm")
        val output = SimpleDateFormat("hh aa")
        val display =  input.parse(it.location.localtime)
        val currentTime =  output.format(display)

        val index = timelist.indexOf(currentTime)
        for (i in index+1 until it.forecast.forecastday[0].hour.size){
            weatherRvHourly.add(it.forecast.forecastday[0].hour[i])
        }

      //  weatherRvHourly.addAll(it.forecast.forecastday[0].hour)
        binding.sunrise.text = it.forecast.forecastday[0].astro.sunrise.toString()
        binding.sunset.text = it.forecast.forecastday[0].astro.sunset.toString()
        binding.uvIndex.text = "${trimLeadingZeros(it.current.uv)}"
        binding.humidity.text = "${it.current.humidity}%"
        binding.feelikelltextview.text = "${trimLeadingZeros(it.current.feelslike_c)}°"
        binding.visibilitytextview.text = "${trimLeadingZeros(it.current.vis_km)}km"
        recyclerViewAdapter.submitList(weatherRvHourly)
        buildGraph(weatherRvHourly)


        binding.RvWeather.adapter = recyclerViewAdapter
        setBackGround(it.current.is_day)
        Log.d("WeatherResponse", it.toString())

        setMap(it.location.lat, it.location.lon, it.current.is_day)
        binding.windSpeed.text = "${trimLeadingZeros(it.current.wind_kph)} kmh"
        binding.windDirection.text = "${it.current.wind_dir.toString()} "
        binding.windDegree.text = it.current.wind_degree.toString()
        binding.wingGust.text = "${trimLeadingZeros(it.current.gust_kph)} kmh"



    }

    private fun setMap(lat: Double, lon: Double, isDay: Int) {


        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(object : OnMapReadyCallback {
            override fun onMapReady(map: GoogleMap?) {
                if (isDay == 1){
                    map?.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireActivity(), R.raw.styleday
                    ))

                }else{
                    map?.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireActivity(), R.raw.styledark))

                }
                val latlong : LatLng = LatLng(lat, lon)
              //  map?.addMarker(MarkerOptions().position(latlong))
                val cameraUpdateFactory =   CameraUpdateFactory.newLatLngZoom(
                    latlong, 10f
                )
                map?.moveCamera(cameraUpdateFactory)
            }

        })

    }
    */

    fun trimLeadingZeros(source: Double): String {

        val price = source
        val format = DecimalFormat("0")
        return format.format(price)

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

 /*   private fun setupUIInteraction() {
        val keyboard = (activity as AppCompatActivity).getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        binding.edtCity.setOnEditorActionListener(object  : OnEditorActionListener{
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {

                if (event != null && event.keyCode === KeyEvent.KEYCODE_ENTER || actionId == EditorInfo.IME_ACTION_DONE) {

                    Log.i(TAG, "Enter pressed")
                    if (v != null) {
                        viewModel.fetchResponse(v.text.toString())
                    }
                    binding.tilCity.visibility = View.INVISIBLE
                    binding.edtCity.setText("")
                    searchstate = false

                    keyboard.hideSoftInputFromWindow(view?.windowToken, 0)
                }
                return false
            }

        })

        binding.search.setOnClickListener {

             if (searchstate == false){
                 binding.tilCity.visibility = View.VISIBLE


                 binding.edtCity.requestFocus()
                 val imm = context?.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager?
                 imm!!.showSoftInput(binding.edtCity, InputMethodManager.SHOW_IMPLICIT)
                    searchstate = true
                }else{
                 val imm = context?.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager?
                 imm!!.hideSoftInputFromWindow(binding.edtCity.getWindowToken(), 0)
                    searchstate = false
                 binding.tilCity.visibility = View.INVISIBLE



             }

            if (binding.edtCity.text!!.isEmpty()){
             //   Toast.makeText(activity, "Please Enter City", Toast.LENGTH_SHORT).show()
            }else{
                cityName.text = cityName.toString()
                viewModel.fetchResponse(binding.edtCity.text.toString())
                keyboard.hideSoftInputFromWindow(view?.windowToken, 0)
                binding.edtCity.setText("")

            }
        }
    }


*/
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
      /*  private fun getWeatherInfo(cityName : String){
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
        }*/

   /* private fun setUpAdapter(weatherRvModelArray: ArrayList<WeatherNetworkModel>)  {
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

            binding.mainLayout.setBackgroundDrawable(resources.getDrawable(R.drawable.day5
            ))
        }else{
            binding.mainLayout.setBackgroundDrawable(resources.getDrawable(R.drawable.nightt))
        }

    }
*/

    private fun getLocationPermission(){
        val permissions = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION)

        if (ContextCompat.checkSelfPermission(requireActivity().applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            Log.d(TAG, "Permission FINE_LOCATION Exists")
            mLocationPermissionsGranted = true

        }else{
            activity?.let {
                Log.d(TAG, "requesting FINE_LOCATION permission")
             //   Toast.makeText(activity, "requesting permission", Toast.LENGTH_SHORT).show()
               // findNavController().navigate(R.id.addFragment)

               // viewModel.fetchResponse("New York")
                Log.d("Mac", "MLKit")
                val requestPermissionLauncher =
                    registerForActivityResult(
                        ActivityResultContracts.RequestPermission()
                    ) { isGranted: Boolean ->
                        if (isGranted) {
                            Log.d("IamGranted", "its true")
                          //  Toast.makeText(activity, "Permission registered from activity", Toast.LENGTH_SHORT).show()
                            // Permission is granted. Continue the action or workflow in your
                            // app.
                            mFusedLocationClient.lastLocation.addOnCompleteListener { task ->
                                val location : Location? = task.result
                                if (task.isSuccessful){
                                    Log.d("IamGranted", "got location")
                                    if (location != null){
                                        Log.d("IamGranted", "location is not null")

                                        Log.d("weatherFragment", location.longitude.toString())
                                        viewModel.fetchResponse(getCityName(long = location.longitude, lat = location.latitude))
                                        cityNameLocation = getCityName(long = location.longitude, lat = location.latitude)

                                    }else{
                                        viewModel.fetchResponse("New York")

                                    }
                                }
                            }
                                .addOnFailureListener {
                                    Log.d("IamGranted", "now its a failure")
                                    (it as ResolvableApiException).startResolutionForResult(activity as MainActivity, 6)

                                }
                           // Toast.makeText(activity, "finally permssion granted", Toast.LENGTH_SHORT).show()
                        } else {
                            // Explain to the user that the feature is unavailable because the
                            // features requires a permission that the user has denied. At the
                            // same time, respect the user's decision. Don't link to system
                            // settings in an effort to convince the user to change their
                            // decision.
                            viewModel.fetchResponse("New York")

                            Toast.makeText(activity, "Location permission required to get current location weather", Toast.LENGTH_SHORT).show()
                      }
                    }
                     requestPermissionLauncher.launch(permissions[0])
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
                        viewModel.fetchResponse(getCityName(long = location.longitude, lat = location.latitude))
                     //   getWeatherInfo(getCityName(long = location.longitude, lat = location.latitude))
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
               // getWeatherInfo(getCityName(long = location.longitude, lat = location.latitude))
                cityNameLocation = getCityName(long = location.longitude, lat = location.latitude)

                //Log.d(TAG, "Start Initial LastLocation is ${it.toString()}")
        } }catch (e : SecurityException){
            Log.d(TAG, "Location not available ${e.message.toString()}")
        }
    }


   /* private fun setUpBottomBar() {
        val bottomNavigationView : BottomNavigationView = binding.bottomNavigationView
        bottomNavigationView.setOnNavigationItemSelectedListener(object: BottomNavigationView.OnNavigationItemSelectedListener{
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                when(item.itemId){
                    R.id.menu_current ->{
                        Toast.makeText(activity, "current", Toast.LENGTH_SHORT).show()
                    }

                    R.id.menu_facourite -> {
                        Toast.makeText(activity, "favourite", Toast.LENGTH_SHORT).show()
                    }
                }
                return true
            }

        })
    }*/

}



