package com.mesum.weather

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.mesum.weather.databinding.WeatherRvItemBinding
import com.mesum.weather.model.WeatherNetworkModel
import java.text.ParseException
import java.text.SimpleDateFormat

class WeatherRvAdapter() : ListAdapter<WeatherNetworkModel, WeatherRvAdapter.ViewHolder>(diffcallback){

    object diffcallback : DiffUtil.ItemCallback<WeatherNetworkModel>() {
        override fun areItemsTheSame(
            oldItem: WeatherNetworkModel,
            newItem: WeatherNetworkModel
        ): Boolean {
          return oldItem.temperature == newItem.temperature

        }

        override fun areContentsTheSame(
            oldItem: WeatherNetworkModel,
            newItem: WeatherNetworkModel
        ): Boolean {
        return oldItem == newItem
        }
    }

    class ViewHolder(private val binding: WeatherRvItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind (item : WeatherNetworkModel){
            binding.cdn.load("http:" + item.icon)
            val input = SimpleDateFormat("yyyy-MM-DD hh:mm")
            val output = SimpleDateFormat("hh:mm")
           val i =  input.parse(item.time)
            binding.Time.text = output.format(i)
            binding.temp.text = "${item.temperature} °C"
            binding.windSpeed.text = "${item.windSpeed} km/h"
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherRvAdapter.ViewHolder {
        return  ViewHolder(WeatherRvItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(getItem(position))
    }


}