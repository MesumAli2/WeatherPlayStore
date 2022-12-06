package com.mesum.weather.favourites

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.mesum.weather.R
import com.mesum.weather.databinding.FragmentFavouriteBinding
import com.mesum.weather.favourites.adapter.FavouriteAdapter
import com.mesum.weather.favourites.favdb.FavCityDatabase
import com.mesum.weather.favourites.favdb.FavCityRepository
import com.mesum.weather.favourites.favdb.FavCitys
import com.mesum.weather.model.ForecastModel


class FavouriteFragment : Fragment(), FavouriteInterface{

     var _binding : FragmentFavouriteBinding? = null
     val binding get() = _binding!!
    private lateinit var repository : FavCityRepository
    private lateinit var viewModel: FavoriteViewModel
    private lateinit var adapter : FavouriteAdapter
    private var apiResponseCityFataList = arrayListOf<ForecastModel>()
    private var cityname : String? = null
    private var favCallBack : FavouriteInterface? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
           cityname = savedInstanceState.get("cityName") as String?
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentFavouriteBinding.inflate(inflater, container, false)
        return binding.root



    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository =  FavCityRepository(FavCityDatabase.getDatabase(context = this.requireContext()).FavDao())
        viewModel  = ViewModelProvider(this, FavoriteViewModel.FavouriteViewModelFactory(repository)).get(FavoriteViewModel::class.java)
        //Add City to Room DB
        binding.titleText.setOnClickListener { requireActivity().onBackPressed() }
        favCallBack = this
        addCity()
        observeCity()
        captureApiResponse()


    }

    private fun addDataToAdapter() {
            adapter.submitList(apiResponseCityFataList.distinct())
            binding.favRv.adapter = adapter
    }

    private fun captureApiResponse() {
        viewModel.weatherResponseFav.observe(viewLifecycleOwner){
            Log.d("ApiResponse", it.current.temp_c.toString())
            apiResponseCityFataList.add(it)
            addDataToAdapter()
        }
    }

    private fun observeCity() {
        adapter = FavouriteAdapter(favCallBack!!)
        viewModel.allCitys.observe(viewLifecycleOwner){
            for (i in it){
                viewModel.fetchResponse(i.cityName)
            }
        }
    }

    private fun addCity() {
        if (arguments?.getString("cityName")
            != null){

            viewModel.insert(FavCitys(cityNmid =arguments?.getString("cityName")!!  ,cityName = arguments?.getString("cityName")!!))

        }
    }

    override fun favClicked(cityName: String) {
        val bundle = Bundle()
        bundle.putString("favsCity", cityName)
        findNavController().navigate(R.id.weatherFragment, bundle)
    }


}