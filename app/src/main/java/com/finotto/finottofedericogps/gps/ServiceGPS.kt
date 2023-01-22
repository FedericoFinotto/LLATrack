package com.finotto.finottofedericogps.gps

import android.Manifest
import android.app.*
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.finotto.finottofedericogps.R
import com.finotto.finottofedericogps.database.*
import com.finotto.finottofedericogps.mainActivity.MainActivity
import com.google.android.gms.location.*
import java.util.*


class ServiceGPS : Service(){

    companion object {
        //Identificatori dello stato del Servizio
        const val STATO_FOREGROUND = "foreground" //Quando l'applicazione é aperta
        const val STATO_BACKGROUND = "background" //Quando l'applicazione rimane in Background
        const val LOG_TAG = "ServiceGPS"
        const val MS_REFRESH : Long = 1000
    }

    private lateinit var db: Database //Contiene l'istanza del Database a cui fanno accesso Activity,Fragment e Service
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private lateinit var richiestaAggiornamento: LocationRequest
    private var longitudine: Double = 0.0
    private var latitudine: Double = 0.0
    private var altitudine: Double = 0.0

    override fun onCreate() {
        super.onCreate()
        Log.d(LOG_TAG, "onCreate")
        db = (application as DatabaseApplication).database
        db.resettaDatabase() //Quando avvio il servizio mi assicuro che non ci siano residui degli avvii precedenti
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        richiestaAggiornamento = LocationRequest.create()
        avviaLocalizzazione()
    }

    private val rispostaAggiornamento = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            Log.d(LOG_TAG, "rispostaAggiornamento: $latitudine $longitudine $altitudine")
            latitudine = locationResult.lastLocation.latitude
            longitudine = locationResult.lastLocation.longitude
            altitudine = locationResult.lastLocation.altitude
            db.aggiungiAlDatabase(PositionSample(altitudine,longitudine,latitudine, Date()))
        }
    }

    private fun avviaLocalizzazione() {
        Log.d(LOG_TAG, "avviaLocalizzazione")
        richiestaAggiornamento.apply{
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = MS_REFRESH
            fastestInterval = MS_REFRESH
        }

        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(richiestaAggiornamento)
        val locationSettingsRequest = builder.build()
        val settingsClient = LocationServices.getSettingsClient(this)
        settingsClient.checkLocationSettings(locationSettingsRequest)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED
            &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        fusedLocationProviderClient!!.requestLocationUpdates( richiestaAggiornamento, rispostaAggiornamento, Looper.myLooper()!!)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(LOG_TAG, "onStartCommand")

        intent?.let {
            when (it.action) {
                STATO_FOREGROUND -> {
                    stopForeground(false)
                    Log.d(LOG_TAG, "onStartCommand => Servizio Avviato in Foreground")
                    avviaLocalizzazione()
                } // Metto il servizio in esecuzione normale (non foreground).
                STATO_BACKGROUND -> {
                    //Configuro un Intent necessario per Permettere la riapertura dell'app premendo la Notifica
                    val intentApriApp = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    intent.action = Intent.ACTION_MAIN
                    intent.addCategory(Intent.CATEGORY_LAUNCHER)

                    val pendingIntent = PendingIntent.getActivity(
                        this,
                        0,
                        intentApriApp,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    val iconBitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_launcher_foreground)
                    // Inserisco le Informazioni della notifica
                    val notification = NotificationCompat.Builder(this, MainActivity.ID_NOTIF_CH_MAIN)
                                        .setContentTitle(getString(R.string.notifica_titolo))
                                        .setContentText(getString(R.string.notifica_descrizione))
                                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                                        .setLargeIcon(iconBitmap)
                                        .setContentIntent(pendingIntent)
                                        .build()
                    startForeground(MainActivity.ID_NOTIF_READING, notification)
                    Log.d(LOG_TAG, "onStartCommand => Servizio Avviato in Background")
                    //avviaLocalizzazione()
                }
                else -> { }
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(LOG_TAG, "onDestroy")
        fusedLocationProviderClient?.removeLocationUpdates(rispostaAggiornamento)
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(LOG_TAG, "onBind")
        return null
    }

    /**
     Funzione lanciata quando l'app viene rimossa dalle app Recenti
     Elimino il Servizio
     */
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Log.d(LOG_TAG, "onTaskRemoved")
        stopSelf()
    }
}


