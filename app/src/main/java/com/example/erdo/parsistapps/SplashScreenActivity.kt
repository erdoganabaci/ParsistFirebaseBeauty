package com.example.erdo.parsistapps
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity

class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splashscreen)


        Handler().postDelayed({
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }, 2000)
    }
}