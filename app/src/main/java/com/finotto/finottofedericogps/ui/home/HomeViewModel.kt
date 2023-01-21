package com.finotto.finottofedericogps.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.util.*
import com.finotto.finottofedericogps.database.*

class HomeViewModel(private val db: Database) : ViewModel() {
    private val pLatitude = MutableLiveData(0f.toDouble())
    val latitude: LiveData<Double> by ::pLatitude

    private val pLongitude = MutableLiveData(0f.toDouble())
    val longitude: LiveData<Double> by ::pLongitude

    private val pAltitude = MutableLiveData(0f.toDouble())
    val altitude: LiveData<Double> by ::pAltitude

    private val semaforoNuovoValore: Observer =
        Observer { _, _ ->
            db.posizioneAttuale.let {
                pLatitude.value = it.latitudine
                pLongitude.value = it.longitudine
                pAltitude.value = it.altitudine
            }
        }

    init {
        // Mi registro per essere notificato quando una nuova Posizione è disponibile.
        db.notificaAggiornamenti.addObserver(semaforoNuovoValore)
    }

    override fun onCleared() {
        super.onCleared()
        db.notificaAggiornamenti.deleteObserver(semaforoNuovoValore)
    }
}

class HomeViewModelFactory(private val db: Database) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(db) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}