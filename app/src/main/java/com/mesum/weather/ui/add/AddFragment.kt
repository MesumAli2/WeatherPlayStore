package com.mesum.weather.ui.add

import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.mesum.weather.Database.Citys
import com.mesum.weather.R
import com.mesum.weather.databinding.FragmentAddBinding
import com.mesum.weather.model.WeatherViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AddFragment : Fragment() {


    var _binding : FragmentAddBinding? = null
    val binding get() = _binding!!

    //private lateinit var repository : CitysRepository
    private  val viewModel: WeatherViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentAddBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val inputMethodManager = requireActivity().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager?
       /* inputMethodManager!!.toggleSoftInputFromWindow(
            binding.root.getApplicationWindowToken(),
            InputMethodManager.SHOW_FORCED, 0
        )*/
      //  view.showKeyboard()


       // repository =  CitysRepository(CitysRoomDatabase.getDatabase(context = this.requireContext()).CitysDao())
//        viewModel  = ViewModelProvider(this, WeatherViewModel.WeatherViewModelFactory(repository))
//            .get(WeatherViewModel::class.java)


        binding.search.requestFocus()
        (requireActivity().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).toggleSoftInput(
            InputMethodManager.SHOW_FORCED,
            InputMethodManager.HIDE_IMPLICIT_ONLY
        )

        // imm!!.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        binding.search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                Toast.makeText(activity, query.toString() + " added", Toast.LENGTH_SHORT).show()

                viewModel.insert(Citys( cityName = query.toString()))
                val bundle : Bundle = Bundle()
                bundle.putBoolean("Added", true)
                findNavController().navigate(R.id.weatherFragment, bundle)
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {

                return true
            }

        })
        binding.search.setOnCloseListener(object : SearchView.OnCloseListener{
            override fun onClose(): Boolean {
                findNavController().navigate(R.id.weatherFragment)
              return  true
            }

        })

        binding.search.setOnQueryTextFocusChangeListener { view, b ->
            if (b){
                view.showKeyboard()
            }
        }

       /// binding.search.oncane
        binding.search.onCancelPendingInputEvents()
        binding.search.onFocusChangeListener
    }

    private fun showDpNow(){
        val showvalvz = "DpValvz"
        val popularShesBachelor = "StrStrStrStrStrStrStrStrStr"
    }

    private fun View.showKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    private fun showInputMethod(view: View) {
        val imm: InputMethodManager? = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (imm != null) {
            imm.showSoftInput(view, 0)
        }
    }

}