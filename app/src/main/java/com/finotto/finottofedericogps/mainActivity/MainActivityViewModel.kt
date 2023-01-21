package com.finotto.finottofedericogps.mainActivity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.finotto.finottofedericogps.database.*

/**
 * [MainActivityViewModel] contiene la logica di controllo per [MainActivity].
 * Il viewModel recupera i dati grezzi e li trasforma preparandoli per la visualizzazione.
 * Per il momento il viewModel è vuoto ma resta a disposizione per sviluppi futuri.
 */
class MainActivityViewModel(private val db: Database):ViewModel()


class MainActivityViewModelFactory(private val db: Database) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainActivityViewModel::class.java)) {
            return MainActivityViewModel(db) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}