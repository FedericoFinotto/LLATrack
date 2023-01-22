package com.finotto.finottofedericogps.database

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import java.util.*

class Database(private val context: Context) {
    companion object {
        const val NUM_MAX_DATI = 150
        const val LOG_TAG = "Database"
    }

    //Lista che contiene l'effettivo database dei dati
    private val pListaValori = mutableListOf<PositionSample>()
    val listSample: List<PositionSample> get() = pListaValori

    //Variabile che contiene il valore in tempo reale
    private var pPosizioneAttuale = PositionSample(0f.toDouble(), 0f.toDouble(), 0f.toDouble(), Date())
    val posizioneAttuale: PositionSample get() = pPosizioneAttuale

    // Un oggetto osservabile che notifica gli osservatore quando la lista di campioni viene aggiornata.
    val notificaAggiornamenti = object : Observable() {
        override fun hasChanged(): Boolean {
            return true
        }
    }

    fun ListaCoordinata( tipo: String ): MutableList<Double>{
        val lista =  mutableListOf<Double>()
        pListaValori.forEach {
            if(tipo=="latitudine") lista.add(0, it.latitudine)
            if(tipo=="longitudine") lista.add(0, it.longitudine)
            if(tipo=="altitudine") lista.add(0, it.altitudine)
        }
        return lista
    }

    fun massimo(tipo: String): Double{
        val lista = ListaCoordinata(tipo)
        lista.sortDescending()
        return lista[0]
    }

    fun minimo(tipo: String): Double{
        val lista = ListaCoordinata(tipo)
        lista.sort()
        return lista[0]
    }

    fun aggiungiAlDatabase(sample: PositionSample) {

        // Se lo storico ha raggiunto la dimensione massima rimuovo i campioni più vecchi in eccesso.
        while (pListaValori.size >= NUM_MAX_DATI) pListaValori.removeLast()
        pListaValori.add(0, sample)
        aggiornaPosizioneAttuale(sample)
        notificaAggiornamenti.notifyObservers()
    }

    fun aggiornaPosizioneAttuale(sample: PositionSample) {
        pPosizioneAttuale = sample
        notificaAggiornamenti.notifyObservers()
    }

    //Svuota il Database
    fun resettaDatabase() {
        pListaValori.clear()
        // Notifico gli osservatori.
        notificaAggiornamenti.notifyObservers()
    }
}