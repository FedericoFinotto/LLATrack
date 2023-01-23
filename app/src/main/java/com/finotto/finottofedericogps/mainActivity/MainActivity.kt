package com.finotto.finottofedericogps.mainActivity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.finotto.finottofedericogps.database.*
import com.finotto.finottofedericogps.gps.*
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.finotto.finottofedericogps.R
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {
    companion object{
        const val ID_NOTIF_CH_MAIN = "LLAFinotto"
        const val ID_NOTIF_READING = 1136211
        const val ID_PERM_REQUEST = 1136211
    }

    private lateinit var db: Database
    private lateinit var viewModel:MainActivityViewModel

    private val permessi = object : Runnable{
        var COARSE: Boolean = false
        var FINE: Boolean = false
        var BACKGROUND: Boolean = false
        override fun run(){
            return
        }
        fun tuttoPermesso(): Boolean{
            return COARSE && FINE && BACKGROUND
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = (application as DatabaseApplication).database
        val viewModelFactory = MainActivityViewModelFactory(db)
        viewModel = ViewModelProvider(this,viewModelFactory).get(MainActivityViewModel::class.java)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navigation_container) as NavHostFragment?
        if (navHostFragment != null) {
            val navController = navHostFragment.navController
            val bottomNavigationView : BottomNavigationView = findViewById(R.id.navBar)
            bottomNavigationView.setupWithNavController(navController)
        }

        richiediPermessi(this)
        createNotificationChannel()
    }

    private fun avviaServizio(background: Boolean = false){
        val intent = when(background){
            true -> Intent()
                .setClass(this, ServiceGPS::class.java)
                .setAction(ServiceGPS.STATO_BACKGROUND)
            false -> Intent()
                .setClass(this, ServiceGPS::class.java)
                .setAction(ServiceGPS.STATO_FOREGROUND)
        }
        startService(intent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            val channel = NotificationChannel(
                ID_NOTIF_CH_MAIN,
                getString(R.string.CanaleNotifica_nome),
                NotificationManager.IMPORTANCE_HIGH)
            channel.description = getString(R.string.CanaleNotifica_descrizione)

            // Un canale può essere registrato più volte senza errori.
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun richiediPermessi(context: Context) {
        if (context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) permessi.COARSE=true
        else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), ID_PERM_REQUEST)
            return
        }

        if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) permessi.COARSE=true
        else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), ID_PERM_REQUEST)
            return
        }

        if (context.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) permessi.COARSE=true
        else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), ID_PERM_REQUEST)
            return
        }
    }

    override fun onRequestPermissionsResult( requestCode: Int, permissions: Array<out String>, grantResults: IntArray ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == ID_PERM_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if(permessi.tuttoPermesso()) avviaServizio()
                else richiediPermessi(this)
            } else {
                Toast.makeText(this, "Permesso Negato", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Metto in esecuzione il servizio che acquisisce i dati da FusedLocationProviderClient.
        avviaServizio()

    }

    //  Funzione chiamata quando l'activity viene chiusa e il processo deve rimanere in Background
    //  Viene chiamata anche quando avviene la rotazione, in tal caso non avvio in background
    override fun onPause() {
        super.onPause()
        if (!isChangingConfigurations) {
            avviaServizio(true)
        }

    }
}

