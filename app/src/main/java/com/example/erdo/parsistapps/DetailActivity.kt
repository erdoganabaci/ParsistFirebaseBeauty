package com.example.erdo.parsistapps

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Camera
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.parse.ParseFile
import com.parse.ParseObject
import com.parse.ParseQuery
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_detail.*

class DetailActivity : AppCompatActivity() ,OnMapReadyCallback {
    var chosenPlace=""
    private lateinit var mMap: GoogleMap
    var firebaseDatabase: FirebaseDatabase? = null
    var myRef: DatabaseReference?=null
    var mStorageRef: StorageReference?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseDatabase= FirebaseDatabase.getInstance()
        myRef= firebaseDatabase!!.getReference()
        mStorageRef= FirebaseStorage.getInstance().getReference()

        setContentView(R.layout.activity_detail)
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)
        val intent=intent
        chosenPlace=intent.getStringExtra("name")
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId==android.R.id.home){
            val intent =Intent(applicationContext,MapsActivity::class.java)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onMapReady(p0: GoogleMap) {
        mMap=p0
        val reference =FirebaseDatabase.getInstance().getReference()
        val query=reference.child("Locations").orderByChild("parkname").equalTo(chosenPlace)
        query.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                for (ds in p0.children) {
                    //println("Ds "+ds.getValue())
                    var hashMap: (HashMap<String, String>) = ds.getValue() as HashMap<String, String>
                    val parkname=hashMap.get("parkname") as String
                    val parkdetail =hashMap.get("parkdetail") as String
                    val latitute =hashMap.get("latitute") as String
                    val longitute =hashMap.get("longitute") as String
                    val latitudeDouble=latitute.toDouble()
                    val longituteDouble=longitute.toDouble()
                    val userLocation=LatLng(latitudeDouble,longituteDouble)
                    val uriImage=hashMap.get("downloadurl") as String

                    Picasso.get().load(uriImage).into(detailImageView)
                    nameTextView.text=parkname
                    detailTextView.text=parkdetail

                    mMap.addMarker(MarkerOptions().position(userLocation).title(parkname))
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,17f))
                }
            }

        })

    }

}


/*
         println("Çalışıyor")
        mMap=p0
        val query=ParseQuery<ParseObject>("Locations")
        query.whereEqualTo("name",chosenPlace)
        //println("Çalışıyor")
        query.findInBackground { objects, e ->
            if (e!=null){
                Toast.makeText(applicationContext,e.localizedMessage, Toast.LENGTH_LONG).show()
            }else{
                if (objects.size>0){
                    for (parseObject in objects){
                        val image=parseObject.get("image") as ParseFile
                        image.getDataInBackground { data, e ->
                            if (e!=null){
                                Toast.makeText(applicationContext,e.localizedMessage, Toast.LENGTH_LONG).show()
                            }else
                            {     // println("Çalışıyor")
                                val bitmap=BitmapFactory.decodeByteArray(data,0,data.size)
                                detailImageView.setImageBitmap(bitmap)
                                val  name=parseObject.get("name") as String
                                val latitute =parseObject.get("latitute") as String
                                val longitite =parseObject.get("longitute") as String
                                val  detail=parseObject.get("detail") as String

                                nameTextView.text=name
                                detailTextView.text=detail

                                val latituteDouble=latitute?.toDouble()
                                val longituteDouble=longitite.toDouble()
                                val userLocation= LatLng(latituteDouble!!,longituteDouble!!)
                                mMap.addMarker(MarkerOptions().position(userLocation).title(name))
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,17f))
                            }

                        }

                    }
                }
            }

        }

*/