package com.finotto.finottofedericogps.database

import android.content.Context
import android.content.SharedPreferences
import java.util.*

/**
 * [Repository] svolge il ruolo di accentratore per i dati comuni dell'applicazione.
 * Esiste una sola istanza di questa classe che viene creata all'avvio dell'app.
 * L'istanza è accessibile attraverso [MyApplication].
 *
 * [ReaderService] raccoglie i dati dai sensori e li pubblica nel repository.
 *
 * Il repository gestisce anche il salvataggio dei dati permanenti attraverso le [SharedPreferences].
 */
class Database(private val context: Context) {
    companion object {
        const val NUM_MAX_DATI = 150
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