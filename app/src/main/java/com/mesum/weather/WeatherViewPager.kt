package com.mesum.weather

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.UserManager
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener
import coil.load
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.mesum.weather.favourites.FavouriteInterface
import com.mesum.weather.model.ForecastModel
import com.mesum.weather.model.Forecastday
import com.mesum.weather.model.Hour
import com.mesum.weather.model.WeatherViewModel
import java.text.DecimalFormat
import java.text.SimpleDateFormat


val diif  =  object  : DiffUtil.ItemCallback<ForecastModel>(){
        override fun areItemsTheSame(oldItem: ForecastModel, newItem: ForecastModel): Boolean {
            return oldItem == newItem
        }

    override fun areContentsTheSame(oldItem: ForecastModel, newItem: ForecastModel): Boolean {
        return oldItem.location.name == newItem.location.name
    }

}
  class WeatherViewPager(
      val weatherlist: List<ForecastModel>,
      val viewModel: WeatherViewModel,
      val ctx: Context,
      val childFragmentManager: FragmentManager,
      val activity: MainActivity,
      val findNanControlle: NavController,
      val callback: FavouriteInterface,
      val sharedPref: SharedPreferences,
         val  tempvalue: String

  ) : ListAdapter<ForecastModel , WeatherViewPager.RvPagerViewHolder>(  diif ) {

    private var weather =ArrayList<ForecastModel>()
  //  private lateinit var viewPager : ViewPager


    class RvPagerViewHolder(val view: View) : RecyclerView.ViewHolder(view)



    val diif  =  object  : DiffUtil.ItemCallback<Hour>(){
        override fun areItemsTheSame(oldItem: Hour, newItem: Hour): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Hour, newItem: Hour): Boolean {
            return oldItem.time == newItem.time
        }

    }


    private fun setUi(it: ForecastModel, binding: View, sharedPref: SharedPreferences, ctx: Context, tempvalue: String) {


        if (tempvalue == ctx.getString(R.string.celsius)){
        val recyclerViewAdapterforecasat = object : ListAdapter<Hour, WeatherFragment.RvViewHolder>(diif ){

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherFragment.RvViewHolder {
                return WeatherFragment.RvViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.weather_rv_item, parent, false)
                )
            }




            override fun onBindViewHolder(holder: WeatherFragment.RvViewHolder, position: Int) {

                val result = getItem(position)

                holder.itemView.findViewById<ImageView>(R.id.cdn).load("http:" + result.condition.icon)

                val input = SimpleDateFormat("yyyy-MM-DD hh:mm")
                val output = SimpleDateFormat("h aa")
                val display =  input.parse(result.time)
                holder.itemView.findViewById<TextView>(R.id.timenow).text = output.format(display)
                holder.itemView.findViewById<TextView>(R.id.temp).text = "${trimLeadingZeros(result.temp_c)}°"
                holder.itemView.findViewById<TextView>(R.id.wind_speed).text = "${trimLeadingZeros(result.wind_kph)} km/h"
            }

        }
        binding.findViewById<ImageView>(R.id.ivOption).setOnClickListener {
            val popup = PopupMenu(ctx, it)
            val inflater: MenuInflater = popup.menuInflater
            inflater.inflate(R.menu.change_preference, popup.menu)
            popup.show()
            popup.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener{
                override fun onMenuItemClick(item: MenuItem?): Boolean {
                     if (item?.itemId == R.id.iv_celcius) {
                        with (sharedPref!!.edit()) {
                            putString("tempType","Celsius")
                            apply()
                        }

                         notifyDataSetChanged()
                       return true
                    }
                    if (item?.itemId == R.id.iv_farhentie) {
                        with (sharedPref!!.edit()) {
                            putString("tempType","Celsius")
                            apply()
                            notifyDataSetChanged()
                        }
                       return false
                    }    else{
                        return false
                    }
                }

            })
        }

        setForecast(it.forecast.forecastday, binding)
        val weatherRvHourly = arrayListOf<Hour>()
        binding.findViewById<ImageView>(R.id.add_weathera).setOnClickListener {
            findNanControlle.navigate(R.id.addFragment)
        }

        binding.findViewById<TextView>(R.id.temp_textview).setOnClickListener {
            viewModel.deleteAll()
        }

        binding.findViewById<TextView>(R.id.city_name).text = it.location.name
        binding.findViewById<TextView>(R.id.text_down).text = "L:${trimLeadingZeros(it.forecast.forecastday[0].day.mintemp_c)}°"
        binding.findViewById<TextView>(R.id.text_up).text = "H:${trimLeadingZeros(it.forecast.forecastday[0].day.maxtemp_c)}°"
        binding.findViewById<ImageView>(R.id.fav_screen).setOnClickListener(object : View.OnClickListener{
            override fun onClick(p0: View?) {
                callback.favClicked(it.location.name)
            }

        })

        binding.findViewById<TextView>(R.id.temp_textview).text = "${trimLeadingZeros(it.current.temp_c)}°"
      binding.findViewById<ImageView>(R.id.id_ivicon).load( "http:" + it.current.condition.icon)
        binding.findViewById<TextView>(R.id.id_condition).text = it.current.condition.text
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
        for (i in index + 1 until it.forecast.forecastday[0].hour.size){
            weatherRvHourly.add(it.forecast.forecastday[0].hour[i])
        }
        if (weatherRvHourly.size > 9){
            recyclerViewAdapterforecasat.submitList(weatherRvHourly)


        }else{
            weatherRvHourly.addAll(it.forecast.forecastday[1].hour)
            recyclerViewAdapterforecasat.submitList(weatherRvHourly)

        }
        setGraph(it, binding, index)

        recyclerViewAdapterforecasat.notifyDataSetChanged()
        //  weatherRvHourly.addAll(it.forecast.forecastday[0].hour)
        binding.findViewById<TextView>(R.id.sunrise).text = it.forecast.forecastday[0].astro.sunrise.toString()
        binding.findViewById<TextView>(R.id.sunset).text = it.forecast.forecastday[0].astro.sunset.toString()
        binding.findViewById<TextView>(R.id.uv_index).text = "${trimLeadingZeros(it.current.uv)}"
        binding.findViewById<TextView>(R.id.humidity).text = "${it.current.humidity}%"
        binding.findViewById<TextView>(R.id.feelikelltextview).text = "${trimLeadingZeros(it.current.feelslike_c)}°"

        binding.findViewById<TextView>(R.id.visibilitytextview).text = "${trimLeadingZeros(it.current.vis_km)}km"

        // binding.findViewById<LinearLayout>(R.id.feelsikell).text = "${trimLeadingZeros(it.current.feelslike_c)}°"

       buildGraph(weatherRvHourly, binding)


       val rvWeather = binding.findViewById<RecyclerView>(R.id.RvWeather)
        rvWeather.adapter = recyclerViewAdapterforecasat
        rvWeather.addOnItemTouchListener(object : OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                val action = e.action
                when (action) {
                    MotionEvent.ACTION_MOVE -> rv.parent.requestDisallowInterceptTouchEvent(true)
                }
                return false
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        })
       setBackGround(it.current.is_day, binding, it.current.condition.text.toString(), it)
        Log.d("WeatherResponse", it.toString())

     //  setMap(it.location.lat, it.location.lon, it.current.is_day, binding)
        binding.findViewById<TextView>(R.id.wind_speed).text = "${trimLeadingZeros(it.current.wind_kph)} kmh"
        binding.findViewById<TextView>(R.id.wind_direction).text = "${it.current.wind_dir.toString()} "
        binding.findViewById<TextView>(R.id.wind_degree).text = it.current.wind_degree.toString()
        binding.findViewById<TextView>(R.id.wing_gust).text = "${trimLeadingZeros(it.current.gust_kph)} kmh"

        }else{
            val recyclerViewAdapterforecasat = object : ListAdapter<Hour, WeatherFragment.RvViewHolder>(diif ){

                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherFragment.RvViewHolder {
                    return WeatherFragment.RvViewHolder(
                        LayoutInflater.from(parent.context).inflate(R.layout.weather_rv_item, parent, false)
                    )
                }




                override fun onBindViewHolder(holder: WeatherFragment.RvViewHolder, position: Int) {

                    val result = getItem(position)

                    holder.itemView.findViewById<ImageView>(R.id.cdn).load("http:" + result.condition.icon)

                    val input = SimpleDateFormat("yyyy-MM-DD hh:mm")
                    val output = SimpleDateFormat("h aa")
                    val display =  input.parse(result.time)
                    holder.itemView.findViewById<TextView>(R.id.timenow).text = output.format(display)
                    holder.itemView.findViewById<TextView>(R.id.temp).text = "${trimLeadingZeros(result.temp_f)}°"
                    holder.itemView.findViewById<TextView>(R.id.wind_speed).text = "${trimLeadingZeros(result.wind_mph)} km/h"
                }

            }
            binding.findViewById<ImageView>(R.id.ivOption).setOnClickListener {
                val popup = PopupMenu(ctx, it)
                val inflater: MenuInflater = popup.menuInflater
                inflater.inflate(R.menu.change_preference, popup.menu)
                popup.show()
                popup.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener{
                    override fun onMenuItemClick(item: MenuItem?): Boolean {
                        if (item?.itemId == R.id.iv_celcius) {
                            with (sharedPref!!.edit()) {
                                putString("tempType",ctx.getString(R.string.celsius))
                                apply()
                            }

                            notifyDataSetChanged()
                            return true
                        }
                        if (item?.itemId == R.id.iv_farhentie) {
                            with (sharedPref!!.edit()) {
                                putString("tempType",ctx.getString(R.string.fahrenheit))
                                apply()
                                notifyDataSetChanged()
                            }
                            return false
                        }    else{
                            return false
                        }
                    }

                })
            }

            setForecast(it.forecast.forecastday, binding)
            val weatherRvHourly = arrayListOf<Hour>()
            binding.findViewById<ImageView>(R.id.add_weathera).setOnClickListener {
                findNanControlle.navigate(R.id.addFragment)
            }

            binding.findViewById<TextView>(R.id.temp_textview).setOnClickListener {
                viewModel.deleteAll()
            }

            binding.findViewById<TextView>(R.id.city_name).text = it.location.name
            binding.findViewById<TextView>(R.id.text_down).text = "L:${trimLeadingZeros(it.forecast.forecastday[0].day.mintemp_f)}°"
            binding.findViewById<TextView>(R.id.text_up).text = "H:${trimLeadingZeros(it.forecast.forecastday[0].day.mintemp_f)}°"
            binding.findViewById<ImageView>(R.id.fav_screen).setOnClickListener(object : View.OnClickListener{
                override fun onClick(p0: View?) {
                    callback.favClicked(it.location.name)
                }

            })

            binding.findViewById<TextView>(R.id.temp_textview).text = "${trimLeadingZeros(it.current.temp_f)}°"
            binding.findViewById<ImageView>(R.id.id_ivicon).load( "http:" + it.current.condition.icon)
            binding.findViewById<TextView>(R.id.id_condition).text = it.current.condition.text
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
            for (i in index + 1 until it.forecast.forecastday[0].hour.size){
                weatherRvHourly.add(it.forecast.forecastday[0].hour[i])
            }
            if (weatherRvHourly.size > 9){
                recyclerViewAdapterforecasat.submitList(weatherRvHourly)


            }else{
                weatherRvHourly.addAll(it.forecast.forecastday[1].hour)
                recyclerViewAdapterforecasat.submitList(weatherRvHourly)

            }
            setGraph(it, binding, index)

            recyclerViewAdapterforecasat.notifyDataSetChanged()
            //  weatherRvHourly.addAll(it.forecast.forecastday[0].hour)
            binding.findViewById<TextView>(R.id.sunrise).text = it.forecast.forecastday[0].astro.sunrise.toString()
            binding.findViewById<TextView>(R.id.sunset).text = it.forecast.forecastday[0].astro.sunset.toString()
            binding.findViewById<TextView>(R.id.uv_index).text = "${trimLeadingZeros(it.current.uv)}"
            binding.findViewById<TextView>(R.id.humidity).text = "${it.current.humidity}%"
            binding.findViewById<TextView>(R.id.feelikelltextview).text = "${trimLeadingZeros(it.current.feelslike_f)}°"

            binding.findViewById<TextView>(R.id.visibilitytextview).text = "${trimLeadingZeros(it.current.vis_km)}km"

            // binding.findViewById<LinearLayout>(R.id.feelsikell).text = "${trimLeadingZeros(it.current.feelslike_c)}°"

            buildGraph(weatherRvHourly, binding)


            val rvWeather = binding.findViewById<RecyclerView>(R.id.RvWeather)
            rvWeather.adapter = recyclerViewAdapterforecasat
            rvWeather.addOnItemTouchListener(object : OnItemTouchListener {
                override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                    val action = e.action
                    when (action) {
                        MotionEvent.ACTION_MOVE -> rv.parent.requestDisallowInterceptTouchEvent(true)
                    }
                    return false
                }

                override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
                override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
            })
            setBackGround(it.current.is_day, binding, it.current.condition.text.toString(), it)
            Log.d("WeatherResponse", it.toString())

            //  setMap(it.location.lat, it.location.lon, it.current.is_day, binding)
            binding.findViewById<TextView>(R.id.wind_speed).text = "${trimLeadingZeros(it.current.wind_kph)} kmh"
            binding.findViewById<TextView>(R.id.wind_direction).text = "${it.current.wind_dir.toString()} "
            binding.findViewById<TextView>(R.id.wind_degree).text = it.current.wind_degree.toString()
            binding.findViewById<TextView>(R.id.wing_gust).text = "${trimLeadingZeros(it.current.gust_kph)} kmh"
        }




    }

    private fun setGraph(forecastModel: ForecastModel, binding: View, index: Int) {

        val mChart : LineChart = binding.findViewById(R.id.temp_forecast)
        mChart.setBackgroundColor(Color.TRANSPARENT)
        mChart.axisLeft.setDrawGridLines(false);
        mChart.xAxis.setDrawGridLines(false);
        mChart.setTouchEnabled(true);
        mChart.setClickable(false);
        mChart.isDoubleTapToZoomEnabled = false;
        mChart.isDoubleTapToZoomEnabled = false;

        mChart.setDrawBorders(false);
        mChart.setDrawGridBackground(false);


        mChart.description.isEnabled = false;
        mChart.legend.isEnabled = false;

        mChart.axisLeft.setDrawGridLines(false);
        mChart.axisLeft.setDrawLabels(false);
        mChart.axisLeft.setDrawAxisLine(false);

        mChart.xAxis.setDrawGridLines(false);
        mChart.xAxis.setDrawLabels(false);
        mChart.xAxis.setDrawAxisLine(false);

        mChart.axisRight.setDrawGridLines(false);
        mChart.axisRight.setDrawLabels(false);
        mChart.axisRight.setDrawAxisLine(false);
        mChart.setDrawGridBackground(false)
        mChart.setDrawBorders(false)
        mChart.description.isEnabled = false
        mChart.setPinchZoom(true)

        val l : Legend  = mChart.legend
        l.isEnabled = true
        l.textColor = Color.WHITE

        setData( forecastModel, mChart, index)



    }

    private fun setData(
        forecastData: ForecastModel,
        mChart: LineChart,
        index: Int
    ) {

        val arrayListY = arrayListOf<Entry>()
        val data =forecastData.forecast.forecastday[0].hour


        for ((c, i) in (index + 1 until data.size).withIndex()) {
                if (c < 5){
                    // trimLeadingZeros(data[i].temp_c).toFloat()
                    arrayListY.add(Entry(c.toFloat(), trimLeadingZeros(data[i].temp_c).toFloat()))

                }
        }
        val mFillColor = Color.argb(150, 51, 181,229)


        val lineDataset = LineDataSet(arrayListY, "Weather")
        lineDataset.axisDependency = YAxis.AxisDependency.LEFT
        lineDataset.color = mFillColor
        lineDataset.setDrawCircles(true)
        lineDataset.setDrawCircleHole(false)
        lineDataset.lineWidth = 3f
        lineDataset.fillAlpha = 255
        lineDataset.setDrawFilled(false)
        val lineData = LineData(lineDataset)

        lineData.setDrawValues(true)
        lineData.setValueTextColor(Color.WHITE)
        lineData.setValueTextSize(12f)
        mChart.data = lineData



    }


    /* private fun setMap(lat: Double, lon: Double, isDay: Int, view: View) {

         val mapView = view.findViewById<MapView>(R.id.mapView)
         mapView.getMapAsync(object : OnMapReadyCallback{
             override fun onMapReady(map: GoogleMap?) {
                 if (isDay == 1){
                     map?.setMapStyle(
                         MapStyleOptions.loadRawResourceStyle(ctx, R.raw.styleday
                         ))

                 }else{
                     map?.setMapStyle(MapStyleOptions.loadRawResourceStyle(ctx, R.raw.styledark))

                 }
                 val latlong : LatLng = LatLng(lat, lon)
                 //  map?.addMarker(MarkerOptions().position(latlong))
                 val cameraUpdateFactory =   CameraUpdateFactory.newLatLngZoom(
                     latlong, 10f
                 )
                 map?.moveCamera(cameraUpdateFactory)            }

         })


        // val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        // val mapFragment =         activity.supportFragmentManager.findFragmentById(R.id.map)as SupportMapFragment


             /* mapFragment.getMapAsync(object : OnMapReadyCallback {
             override fun onMapReady(map: GoogleMap?) {
                 if (isDay == 1){
                     map?.setMapStyle(
                         MapStyleOptions.loadRawResourceStyle(ctx, R.raw.styleday
                     ))

                 }else{
                     map?.setMapStyle(MapStyleOptions.loadRawResourceStyle(ctx, R.raw.styledark))

                 }
                 val latlong : LatLng = LatLng(lat, lon)
                 //  map?.addMarker(MarkerOptions().position(latlong))
                 val cameraUpdateFactory =   CameraUpdateFactory.newLatLngZoom(
                     latlong, 10f
                 )
                 map?.moveCamera(cameraUpdateFactory)
             }

         })*/

     }*/

    fun trimLeadingZeros(source: Double): String {
        val price = source
        val format = DecimalFormat("0")
        return format.format(price)

    }
    class RvViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    private fun buildGraph(weatherRvHourly: MutableList<Hour>, binding: View) {
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
            binding.findViewById<TextView>(R.id.rain_text).visibility = View.VISIBLE
            binding.findViewById<RecyclerView>(R.id.rain_recyclerView).visibility = View.VISIBLE
        }
        else{
            binding.findViewById<TextView>(R.id.rain_text).visibility = View.GONE
            binding.findViewById<RecyclerView>(R.id.rain_recyclerView).visibility = View.GONE
        }
        recyclerViewAdapter.submitList(newlist)
        binding.findViewById<RecyclerView>(R.id.rain_recyclerView).adapter = recyclerViewAdapter

    }


    fun addWeather(list: ArrayList<ForecastModel>){
        weather.clear()
        weather.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RvPagerViewHolder {
        return RvPagerViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.weather_item, parent, false))
    }

    override fun onBindViewHolder(holder: RvPagerViewHolder, position: Int) {
       // holder.itemView.findViewById<TextView>(R.id.temp_textview).text = weatherlist.get(position).current.temp_c.toString()

        setUi(getItem(position), holder.itemView, sharedPref, ctx, tempvalue)
    }

    override fun getItemCount(): Int {
       return weatherlist.size
    }



    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setBackGround(
        day: Int,
        binding: View,
        condition: String,
        it: ForecastModel
    ) {
        if (day == 1){

            ///day
            if (condition.contains("rain")) {
                //   Glide.with(ctx).load(R.drawable.livegif).asGif().diskCacheStrategy(DiskCacheStrategy.SOURCE).crossFade().into(binding.findViewById(R.id.back_ground_image));
            /*    Glide.with(binding).asGif().load(R.drawable.raining)
                    .into(binding.findViewById(R.id.id_ivicon))
*/

                Glide.with(binding).load(R.drawable.day4).fitCenter()
                    .apply(RequestOptions().override(500,1500))
                    .into(binding.findViewById(R.id.back_ground_image))
            }
            else if (condition.contains("cloudy")){
                Glide.with(binding).load(R.drawable.day4).optionalCenterInside()
                    .apply(RequestOptions().override(500,1500))
                    .into(binding.findViewById(R.id.back_ground_image))
            }

            else{

                Glide.with(binding).load(ctx.getDrawable(R.drawable.day4)).into(binding.findViewById(R.id.back_ground_image))
             //   binding.findViewById<ImageView>(R.id.back_ground_image).setImageDrawable(ctx.resources.getDrawable(R.drawable.day5))
            }
           //
        }else{

                //Night
            if (condition.contains("cloudy")) {
                Glide.with(binding).load(R.drawable.nightt).centerCrop()
                    .apply(RequestOptions().override(500,1500))

                    .into(binding.findViewById(R.id.back_ground_image))
            }
            else if (condition.contains("rain")){
              /*  Glide.with(binding).asGif().load(R.drawable.raining)
                    .into(binding.findViewById(R.id.id_ivicon))
*/
                Glide.with(binding).asGif().load(R.drawable.rainnight).centerCrop()
                    .apply(RequestOptions().override(500,1500))
                    .into(binding.findViewById(R.id.back_ground_image))
            }
            else{

                Glide.with(binding).load(ctx.getDrawable(R.drawable.nightt)).into(binding.findViewById(R.id.back_ground_image))

            }


            //   binding.findViewById<RelativeLayout>(R.id.main_layout).setBackgroundDrawable(ctx.resources.getDrawable(R.drawable.nightt))
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

    private fun setForecast(forecastday: List<Forecastday>, binding: View) {
    //    val listInterval = mutableListOf<Forecastday>()
        val rvAdapter = object : ListAdapter<Forecastday, WeatherFragment.RvViewHolder>(diif2 ){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherFragment.RvViewHolder {
                return WeatherFragment.RvViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_forecast, parent, false)
                )
            }
            override fun onBindViewHolder(holder: WeatherFragment.RvViewHolder, position: Int) {
                var forecastDays = true
                val result = getItem(position)
                val input = SimpleDateFormat( "yyyy-MM-dd")
                val output = SimpleDateFormat("EEEE")
                val display =  input.parse(result.date.toString())
                holder.itemView.findViewById<TextView>(R.id.curr_day).text = output.format(display)
                // result.day.condition.
                //   holder.itemView.findViewById<TextView>(R.id.curr_day).text = result.date.toString()
                //holder.itemView.findViewById<TextView>(R.id.curr_day).text = result.startTime
//                holder.itemView.findViewById<TextView>(R.id.time).text = output.format(display)

                holder.itemView.findViewById<TextView>(R.id.curr_temp_low).text = "${trimLeadingZeros(result.day.mintemp_c)}°"
                holder.itemView.findViewById<TextView>(R.id.curr_temp_high).text = "${trimLeadingZeros(result.day.maxtemp_c)}°"
                holder.itemView.findViewById<ImageView>(R.id.weather_image).load("http:" + result.day.condition.icon)
                val rvFutureForecastAdapter = object : ListAdapter<Hour, WeatherFragment.RvViewHolder>(diif ){

                    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherFragment.RvViewHolder {
                        return WeatherFragment.RvViewHolder(
                            LayoutInflater.from(parent.context).inflate(R.layout.future_items, parent, false)
                        )
                    }

                    override fun onBindViewHolder(holder: WeatherFragment.RvViewHolder, position: Int) {
                        val result = getItem(position)
//                        holder.itemView.findViewById<ImageView>(R.id.cdn).load("http:" + result.condition.icon)
                        val input = SimpleDateFormat("yyyy-MM-DD hh:mm")
                        val output = SimpleDateFormat("h aa")
                        val display =  input.parse(result.time)
                        holder.itemView.findViewById<TextView>(R.id.time_future).text = output.format(display)
                        holder.itemView.findViewById<TextView>(R.id.temp_future).text = "${trimLeadingZeros(result.temp_c)}°"
                        holder.itemView.findViewById<ImageView>(R.id.icon_future).load("http:" + result.condition.icon)
                    //    holder.itemView.findViewById<TextView>(R.id.wind_speed).text = "${trimLeadingZeros(result.wind_kph)} km/h"
                    }



                }
                holder.itemView.setOnClickListener {
                    if (forecastDays){
                        holder.itemView.findViewById<RecyclerView>(R.id.recycler_forecast).visibility = View.VISIBLE
                        rvFutureForecastAdapter.submitList(forecastday[position].hour)
                      //  Toast.makeText(ctx, forecastday[position].hour.toString(), Toast.LENGTH_LONG).show()
                        holder.itemView.findViewById<RecyclerView>(R.id.recycler_forecast).adapter = rvFutureForecastAdapter
                        holder.itemView.findViewById<ImageView>(R.id.curr_item_statues).setImageDrawable(ctx.resources.getDrawable(
                            me.relex.circleindicator.R.drawable.mtrl_ic_arrow_drop_up))

                        forecastDays = false

                    }else{
                        holder.itemView.findViewById<RecyclerView>(R.id.recycler_forecast).visibility = View.GONE
                        holder.itemView.findViewById<ImageView>(R.id.curr_item_statues).setImageDrawable(ctx.resources.getDrawable(
                            me.relex.circleindicator.R.drawable.mtrl_ic_arrow_drop_down))
                        forecastDays = true

                    }


                   // Toast.makeText(ctx, "Forecast clicked", Toast.LENGTH_SHORT).show()
                }
            }
        }
        binding.findViewById<CardView>(R.id.weatherForecast).visibility = View.VISIBLE
       // binding.weatherForecast.visibility = View.VISIBLE

        rvAdapter.submitList(forecastday)
       val rvForecast = binding.findViewById<RecyclerView>(R.id.rv_forecast)
        rvForecast.isNestedScrollingEnabled = false
        rvForecast.adapter = rvAdapter
        rvForecast.addOnItemTouchListener(object : OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                val action = e.action
                when (action) {
                    MotionEvent.ACTION_MOVE -> rv.parent.requestDisallowInterceptTouchEvent(true)
                }
                return false
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        })
       // ViewCompat.setNestedScrollingEnabled(rvForecast, false);



    }

}