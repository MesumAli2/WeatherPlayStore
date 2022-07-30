package com.mesum.weather

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.Nullable
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import coil.load
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.ImageViewTarget
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
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
        return oldItem.location == newItem.location
    }

}
class WeatherViewPager ( val viewPager: ViewPager2,val


weatherlist : ArrayList<ForecastModel>, val viewModel : WeatherViewModel, val viewLifecycleOwner : LifecycleOwner, val ctx : Context,
                         val childFragmentManager: FragmentManager,val  activity: MainActivity, val findNanControlle: NavController
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


    private fun setUi(it: ForecastModel, binding: View) {

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
                holder.itemView.findViewById<TextView>(R.id.wind_speed).text = "${trimLeadingZeros(result.wind_kph)} km/h"                }



        }


        setForecast(it.forecast.forecastday, binding)
  //     var weatherRvHourly : MutableList<Hour> = mutableListOf()
        val weatherRvHourly = arrayListOf<Hour>()
        binding.findViewById<ImageView>(R.id.add_weathera).setOnClickListener {

            findNanControlle.navigate(R.id.addFragment)
          //  findNanControlle().navigate(R.id.addFragment)
            //  showInputMethod(view)

        }

        binding.findViewById<TextView>(R.id.temp_textview).setOnClickListener {
            viewModel.deleteAll()
        }

        binding.findViewById<TextView>(R.id.city_name).text = it.location.name
        binding.findViewById<TextView>(R.id.text_down).text = "L:${trimLeadingZeros(it.forecast.forecastday[0].day.mintemp_c)}°"
        binding.findViewById<TextView>(R.id.text_up).text = "H:${trimLeadingZeros(it.forecast.forecastday[0].day.maxtemp_c)}°"

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


        binding.findViewById<RecyclerView>(R.id.RvWeather).adapter = recyclerViewAdapterforecasat
       setBackGround(it.current.is_day, binding, it.current.condition.text.toString())
        Log.d("WeatherResponse", it.toString())

      // setMap(it.location.lat, it.location.lon, it.current.is_day)
        binding.findViewById<TextView>(R.id.wind_speed).text = "${trimLeadingZeros(it.current.wind_kph)} kmh"
        binding.findViewById<TextView>(R.id.wind_direction).text = "${it.current.wind_dir.toString()} "
        binding.findViewById<TextView>(R.id.wind_degree).text = it.current.wind_degree.toString()
        binding.findViewById<TextView>(R.id.wing_gust).text = "${trimLeadingZeros(it.current.gust_kph)} kmh"



    }


  /*  private fun setMap(lat: Double, lon: Double, isDay: Int) {




       // val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        val mapFragment =         activity.supportFragmentManager.findFragmentById(R.id.map)as SupportMapFragment
        mapFragment.getMapAsync(object : OnMapReadyCallback {
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

        })

    }
*/
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

        setUi(getItem(position), holder.itemView)
    }

    override fun getItemCount(): Int {
       return weatherlist.size
    }



    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setBackGround(day: Int, binding: View, condition: String) {
        if (day == 1){

            ///day
            if (condition.contains("rain")) {
                //   Glide.with(ctx).load(R.drawable.livegif).asGif().diskCacheStrategy(DiskCacheStrategy.SOURCE).crossFade().into(binding.findViewById(R.id.back_ground_image));
                Glide.with(binding).asGif().load(R.drawable.liverain).centerCrop()
                    .apply(RequestOptions().override(500,1500))

                    .into(binding.findViewById(R.id.back_ground_image))
            }
            else if (condition.contains("cloudy")){
                Glide.with(binding).asGif().load(R.drawable.cloudy).centerCrop()
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
                Glide.with(binding).asGif().load(R.drawable.cloudynight).centerCrop()
                    .apply(RequestOptions().override(500,1500))

                    .into(binding.findViewById(R.id.back_ground_image))
            }
            else if (condition.contains("rain")){
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
        binding.findViewById<RecyclerView>(R.id.rv_forecast).adapter = rvAdapter


    }

}