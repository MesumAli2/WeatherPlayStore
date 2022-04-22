package com.mesum.weather

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mesum.weather.databinding.WeatherRvItemBinding
import com.mesum.weather.model.ForecastModel
import com.mesum.weather.model.Forecastday
import com.mesum.weather.model.Hour
import com.mesum.weather.model.WeatherNetworkModel
import org.w3c.dom.Text
import java.text.ParseException
import java.text.SimpleDateFormat

class WeatherRvAdapter(var context: Context, var weatherRvModelArrayList: ArrayList<WeatherNetworkModel>) : RecyclerView.Adapter<WeatherRvAdapter.ViewHolder>() {


    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
       val temp  = itemView.findViewById<TextView>(R.id.temp)
        var wind = itemView.findViewById<TextView>(R.id.wind_speed)
        var time = itemView.findViewById<TextView>(R.id.Time)
        var image = itemView.findViewById<ImageView>(R.id._condition)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherRvAdapter.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.weather_rv_item, parent, false)
        return  ViewHolder(view)
    }

    override fun onBindViewHolder(holder: WeatherRvAdapter.ViewHolder, position: Int) {
        var data = weatherRvModelArrayList.get(position)
        holder.image.setImageURI(("http:${data.icon}").toUri())
        holder.wind.text = data.windSpeed.toString()
        holder.temp.text = "${data.temperature}C"
        var input = SimpleDateFormat("yyyy-MM-dd hh:mm")
        var output = SimpleDateFormat("hh:mm aa")
        try {
            var time = input.parse(data.temperature)
            holder.time.text = output.format(time)
        }catch (e : ParseException){
            e.printStackTrace()
        }
     }

    override fun getItemCount(): Int {
       return weatherRvModelArrayList.size
    }


}