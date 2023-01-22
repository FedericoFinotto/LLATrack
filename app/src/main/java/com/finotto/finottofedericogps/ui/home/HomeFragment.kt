package com.finotto.finottofedericogps.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.finotto.finottofedericogps.R
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.finotto.finottofedericogps.database.*


class HomeFragment : Fragment() {

    private lateinit var db: Database
    private lateinit var latitudeValue : TextView
    private lateinit var longitudeValue : TextView
    private lateinit var altitudeValue : TextView
    private lateinit var latitudeName : TextView
    private lateinit var longitudeName : TextView
    private lateinit var altitudeName : TextView
    private lateinit var viewModel: HomeViewModel


    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View {
        db = (requireActivity().application as DatabaseApplication).database
        val viewModelFactory = HomeViewModelFactory(db)
        viewModel = ViewModelProvider(this, viewModelFactory).get(HomeViewModel::class.java)

        return inflater.inflate(R.layout.fragment_home, container, false)

    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        latitudeValue = view.findViewById(R.id.value_Latitude)
        longitudeValue = view.findViewById(R.id.value_Longitude)
        altitudeValue  = view.findViewById(R.id.value_Altitude)
        latitudeName  = view.findViewById(R.id.text_Latitude)
        longitudeName  = view.findViewById(R.id.text_Longitude)
        altitudeName  = view.findViewById(R.id.text_Altitude)

        latitudeName.text = getString(R.string.text_Latitude)
        longitudeName.text = getString(R.string.text_Longitude)
        altitudeName.text = getString(R.string.text_Altitude)

        stampaAttuale()

        viewModel.latitude.observe(viewLifecycleOwner) { value ->
            latitudeValue.text = getString(R.string.numericFormat).format(value)
        }
        viewModel.longitude.observe(viewLifecycleOwner) { value ->
            longitudeValue.text = getString(R.string.numericFormat).format(value)
        }
        viewModel.altitude.observe(viewLifecycleOwner) { value ->
            altitudeValue.text = getString(R.string.numericFormat).format(value)
        }

    }

    private fun stampaAttuale(){
        val attuale : PositionSample = db.posizioneAttuale
        latitudeValue.text = getString(R.string.numericFormat).format(attuale.latitudine)
        longitudeValue.text = getString(R.string.numericFormat).format(attuale.longitudine)
        altitudeValue.text = getString(R.string.numericFormat).format(attuale.altitudine)
    }

    override fun onResume() {
        super.onResume()
        stampaAttuale()
    }
}
