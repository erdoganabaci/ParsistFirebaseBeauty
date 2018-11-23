package com.example.erdo.parsistapps

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.doubleclick.PublisherAdView
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.mancj.materialsearchbar.MaterialSearchBar
import com.parse.ParseException
import com.parse.ParseObject
import com.parse.ParseQuery
import com.parse.ParseUser
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import java.security.AccessController.getContext

//var mAdView: AdView? = null
class MainActivity : AppCompatActivity() {
    lateinit var mAdView: AdView
    var firebaseDatabase: FirebaseDatabase? = null
    var myRef: DatabaseReference? = null
    var mStorageRef: StorageReference? = null
    var nameArray = ArrayList<String>()
    @SuppressLint("ResourceType")
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.show_place, menu)
        //menuInflater.inflate(R.id.home,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.show_place) {
            val intent = Intent(applicationContext, MapsActivity::class.java)
            startActivity(intent)

        }

        return super.onOptionsItemSelected(item)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        firebaseDatabase = FirebaseDatabase.getInstance()
        myRef = firebaseDatabase!!.getReference()
        mStorageRef = FirebaseStorage.getInstance().getReference()


        getFirebaseData()
        //listvieve tıklanınca ne olacağı yani nameleri alıp detailed activitye aktarıcak
        //ilk dataları formalite icabi koy resim eklemeyide sonra databaseden manuel eklerim.
        listView.setOnItemClickListener { adapterView, view, i, l ->
            val intent = Intent(applicationContext, DetailActivity::class.java)
            intent.putExtra("name", nameArray[i])
            startActivity(intent)

        }
        var android_id = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        println("emülatör_id:" + android_id)
    }

    fun getFirebaseData() {
        val lv = findViewById(R.id.listView) as ListView
        val searchBar = findViewById(R.id.searchBar) as MaterialSearchBar
        searchBar.setHint("Park Ara..")
        searchBar.setSpeechMode(true)
        //lv.setBackgroundColor(Color.BLUE)
        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, nameArray)
        listView.adapter = arrayAdapter
        val newReference = FirebaseDatabase.getInstance().getReference("Locations")
        newReference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                nameArray.clear()
                for (ds in p0.children) {
                    val hashMap: (HashMap<String, String>) = ds.getValue() as HashMap<String, String>
                    val parkname = hashMap.get("parkname") as String
                    nameArray.add(parkname)
                }
                arrayAdapter.notifyDataSetChanged()
            }
        })
        searchBar.addTextChangeListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                arrayAdapter.getFilter().filter(p0)
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }


        })
    }

}