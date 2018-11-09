package com.example.erdo.parsistapps

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import com.github.paolorotolo.appintro.AppIntro
import com.github.paolorotolo.appintro.AppIntroFragment


class IntroSlider : AppIntro() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addSlide(AppIntroFragment.newInstance("ParsistApp",
                "First Image",
                R.drawable.haritak,
                Color.parseColor("#51e2b7")
        ))
        addSlide(AppIntroFragment.newInstance("ParsistApp",
                "Second Image",
                R.drawable.car1,
                Color.parseColor("#8c50e3")
        ))
        addSlide(AppIntroFragment.newInstance("ParsistApp",
                "Third Image",
                R.drawable.car2,
                Color.parseColor("#4fd7ff")
        ))
        addSlide(AppIntroFragment.newInstance("Tüm Haritalar","Haritaya Gitmek İçin",R.drawable.haritak,Color.parseColor("#8c50e3")))


        //showStatusBar(true)
        //setBarColor(Color.parseColor("#E2C044"))
        //setSeparatorColor(Color.parseColor("#E2C044"))
    }

    override fun onDonePressed() {
       val intent=Intent(applicationContext,MapsActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onSkipPressed() {
        val intent=Intent(applicationContext,MapsActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onSlideChanged() {


    }
}
