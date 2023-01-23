package com.finotto.finottofedericogps.database

import android.app.Application

/**
  *  Utilizzo questa classe come Database per i dati necessari all'esecuzione dell'applicazione
  */
class DatabaseApplication : Application() {
    private lateinit var pDatabase: Database
    val database by ::pDatabase

    override fun onCreate() {
        super.onCreate()
        pDatabase = Database(this)
    }
}