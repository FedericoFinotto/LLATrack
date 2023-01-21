package com.finotto.finottofedericogps.GPS

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
        const val ACTION_START = "start" //Quando l'applicazione é aperta
        const val ACTION_RUN_IN_BACKGROUND = "background" //Quando l'applicazione rimane in Background
        const val LOG_TAG = "ServiceGPS"
        const val MS_REFRESH : Long = 1000
    }

    private lateinit var db: Database //Contiene l'istanza del Database a cui fanno accesso Activity,Fragment e Service
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var longitude: Double = 0.0
    private var latitude: Double = 0.0
    private var altitude: Double = 0.0
    private lateinit var mLocationRequest: LocationRequest

     /**
     Funzione Chiamata all'avvio del Servizio, la utilizzo per predisporre l'accesso al servizio di Localizzazione
     */
    override fun onCreate() {
        super.onCreate()
        Log.d(LOG_TAG, "on create")
        db = (application as DatabaseApplication).database
        db.resettaDatabase() //Quando avvio il servizio mi assicuro che non ci siano residui degli avvii precedenti
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        mLocationRequest = LocationRequest.create()
        startLocationUpdates()
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            //locationResult.lastLocation
            Log.d(LOG_TAG, "mLocationCallback: $latitude $longitude $altitude")
            locationChanged(locationResult.lastLocation)
            latitude = locationResult.lastLocation.latitude
            longitude = locationResult.lastLocation.longitude
            altitude = locationResult.lastLocation.altitude
            db.aggiungiAlDatabase(PositionSample(altitude,longitude,latitude, Date()))
        }
    }

    fun locationChanged(location: Location) {
        //mLastLocation = location
        longitude = location.longitude
        latitude = location.latitude
        altitude = location.altitude
        Log.d(LOG_TAG, "locationChanged: $latitude $longitude $altitude")
    }

    private fun startLocationUpdates() {
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = MS_REFRESH
        mLocationRequest.fastestInterval = MS_REFRESH

        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(mLocationRequest)
        val locationSettingsRequest = builder.build()
        val settingsClient = LocationServices.getSettingsClient(this)
        settingsClient.checkLocationSettings(locationSettingsRequest)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED
            &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        fusedLocationProviderClient!!.requestLocationUpdates(
            mLocationRequest,
            mLocationCallback,
            Looper.myLooper()!!)
    }

    /**
     * onStartCommand funge da interfaccia di controllo per il servizio.
     * Il servizio può essere in esecuzione normale o in esecuzione foreground.
     * onStartCommand riconosce due azioni che permettono di mettere il servizio in una delle due
     * modalità di esecuzione.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(LOG_TAG, "onStartCommand => Servizio Avviato")

        intent?.let {
            when (it.action) {
                ACTION_START -> {
                    stopForeground(false);
                    Log.d(LOG_TAG, "onStartCommand => Servizio Avviato con Activity")
                    startLocationUpdates()
                } // Metto il servizio in esecuzione normale (non foreground).
                ACTION_RUN_IN_BACKGROUND -> {
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
                    val notification =
                        NotificationCompat.Builder(this, MainActivity.ID_NOTIF_CH_MAIN)
                            .setSmallIcon(R.drawable.ic_launcher_foreground)
                            .setLargeIcon(iconBitmap)
                            .setContentTitle(getString(R.string.notifica_titolo))
                            .setContentText(getString(R.string.notifica_descrizione))
                            .setContentIntent(pendingIntent)
                            .build()
                    startForeground(MainActivity.ID_NOTIF_READING, notification)
                    Log.d(LOG_TAG, "onStartCommand => Servizio Avviato in Background")
                }
                else -> { }
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(LOG_TAG, "onDestroy")
        fusedLocationProviderClient?.removeLocationUpdates(mLocationCallback)
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


