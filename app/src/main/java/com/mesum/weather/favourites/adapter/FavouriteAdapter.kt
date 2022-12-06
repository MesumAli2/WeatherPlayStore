package com.mesum.weather.favourites.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.mesum.weather.R
import com.mesum.weather.favourites.FavouriteInterface
import com.mesum.weather.model.ForecastModel
import javax.xml.transform.ErrorListener

class FavouriteViewHolder(val view: View) : RecyclerView.ViewHolder(view)
val diff = object : DiffUtil.ItemCallback<ForecastModel>(){
    override fun areItemsTheSame(oldItem: ForecastModel, newItem: ForecastModel): Boolean {
       return oldItem== newItem
    }

    override fun areContentsTheSame(oldItem: ForecastModel, newItem: ForecastModel): Boolean {
        return oldItem.location.name == newItem.location.name
    }

}
class FavouriteAdapter(val favCallBack: FavouriteInterface) : ListAdapter<ForecastModel, FavouriteViewHolder>( diff ) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavouriteViewHolder {
        return FavouriteViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.fav_item, parent, false))
    }

    override fun onBindViewHolder(holder: FavouriteViewHolder, position: Int) {

        val result = getItem(position)
        val cityname = holder.itemView.findViewById<TextView>(R.id.city_name)
        cityname.text = result.location.name
        val temp = holder.itemView.findViewById<TextView>(R.id.city_temp)
        temp.text = result.current.temp_c.toString()
        val image =  holder.itemView.findViewById<ImageView>(R.id.img_cond)
        image.load("http:" + result.current.condition.icon)
        holder.itemView.setOnClickListener {
                favCallBack.favClicked(result.location.name)
        }



    }

}