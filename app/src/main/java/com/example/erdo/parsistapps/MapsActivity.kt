package com.example.erdo.parsistapps

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Uri
import android.nfc.Tag
import android.support.v7.app.AppCompatActivity

import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.github.clans.fab.FloatingActionButton
import com.github.clans.fab.FloatingActionMenu
import com.github.ybq.android.spinkit.style.Wave
import com.google.android.gms.maps.*

import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.onesignal.OneSignal
import com.parse.ParseException
import com.parse.ParseFile
import com.parse.ParseObject
import com.parse.ParseQuery
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.customcardpopup.*
import kotlinx.android.synthetic.main.mycard.*
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

var latituteDouble: Double? = 0.0
var longituteDouble: Double? = 0.0
var userLocation = LatLng(latituteDouble!!, longituteDouble!!)
var googleMap: GoogleMap? = null

class MapsActivity : AppCompatActivity(), OnMapReadyCallback,ClusterManager.OnClusterClickListener<MyItem>, ClusterManager.OnClusterInfoWindowClickListener<MyItem>, ClusterManager.OnClusterItemClickListener<MyItem>, ClusterManager.OnClusterItemInfoWindowClickListener<MyItem> {


    private lateinit var mMap: GoogleMap
    internal lateinit var switch: Switch
    var firebaseDatabase: FirebaseDatabase? = null
    var myRef: DatabaseReference? = null
    var mStorageRef: StorageReference? = null
    //val progressDialog = ProgressDialog(this@MapsActivity)
    //firebase
    var locationManager: LocationManager? = null
    var locationListener: LocationListener? = null
    var floatingActionMenu: FloatingActionMenu? = null
    var aboutUs: FloatingActionButton? = null
    var carList: FloatingActionButton? = null
    var changeMap: FloatingActionButton? = null
    var myDialog: Dialog? = null
    var myDialogAbout: Dialog? = null
    var progressBar: ProgressBar? = null
    var clusterManager: ClusterManager<MyItem>?=null
    val clusterItems=ArrayList<MyItem>()
    var flag : Boolean = true
    //var name=""
    //var nameInfoArray=HashSet<String>()


    //private var FASTEST_INTERVAL = 1000 // use whatever suits you
    //private var currentLocation:Location? = null
    //private var locationUpdatedAt = java.lang.Long.MIN_VALUE

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.show_maplist, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.show_maplist) {
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)

        }
        return super.onOptionsItemSelected(item)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.supportActionBar?.hide()
        val thread = Thread(object : Runnable {
            override fun run() {
                val getPrefs = PreferenceManager.getDefaultSharedPreferences(baseContext)
                val isFirstStart: Boolean = getPrefs.getBoolean("firstStart", true)
                if (isFirstStart) {
                    startActivity(Intent(applicationContext, IntroSlider::class.java))
                    val e: SharedPreferences.Editor = getPrefs.edit()
                    e.putBoolean("firstStart", false)
                    e.apply()
                }


            }


        })

        thread.start()



        firebaseDatabase = FirebaseDatabase.getInstance()
        myRef = firebaseDatabase!!.getReference()
        mStorageRef = FirebaseStorage.getInstance().getReference()

        if (isConnected(this) == false) {
            buildDialog(this).show()
        } else {
            setContentView(R.layout.activity_maps)
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            val mapFragment = supportFragmentManager
                    .findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(this)


        }

        //floatingActionMenu= findViewById(R.id.menu) as FloatingActionMenu  //çalıştırıca hata veriyor null pointer hatası
        aboutUs = findViewById(R.id.menu_item1) as FloatingActionButton?
        carList = findViewById(R.id.menu_item) as FloatingActionButton?
        changeMap = findViewById(R.id.menu_item2) as FloatingActionButton?

         myDialogAbout = Dialog(this)

         aboutUs?.setOnClickListener(object : View.OnClickListener {
             override fun onClick(v: View?) {
                 myDialogAbout!!.setContentView(R.layout.custompopup)
                var textClose = myDialogAbout!!.findViewById(R.id.txtclose) as TextView
                 var webButton = myDialogAbout!!.findViewById(R.id.btnweb) as Button
                textClose.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                         myDialogAbout!!.dismiss()
                   }


             })
                 myDialogAbout!!.show()
               webButton.setOnClickListener(object : View.OnClickListener {
                     override fun onClick(v: View?) {
                         val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://erdoganabaci.github.io/ParsistWebsite/"))
                       startActivity(intent)
                  }


                })
             }


         })


        carList?.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val intent = Intent(applicationContext, MainActivity::class.java)
                startActivity(intent)
            }


        })



        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init()


    }

    fun isConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netinfo = cm.getActiveNetworkInfo()

        if (netinfo != null && netinfo.isConnectedOrConnecting) {
            val wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
            val mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
            if (mobile != null && mobile.isConnectedOrConnecting || wifi != null && wifi.isConnectedOrConnecting) {
                return true
            } else {
                return false
            }
        } else {
            return false
        }
//You need to have Mobile Data or wifi to access this

    }

    fun buildDialog(c: Context): AlertDialog.Builder {
        val builder = AlertDialog.Builder(c)
        builder.setTitle("No Internet Connection")
        builder.setMessage("Lütfen Internet Bağlantınızı Kontrol Ediniz.Devam etmek için Ok basınız.")
        builder.setCancelable(false)
        builder.setPositiveButton("Ok", object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface, which: Int) {
                finish()
            }
        })
        return builder
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    override fun onMapReady(googleMap: GoogleMap) {
        myDialog = Dialog(this)
        //upload()
        getFireLocation()
        mMap = googleMap
        val mUiSettings = mMap.getUiSettings()
        mUiSettings.isZoomControlsEnabled=true
        mUiSettings.isCompassEnabled=true
        mUiSettings.isMyLocationButtonEnabled=true
         fun setupMapFragment() {
            val mapFragment = getSupportFragmentManager()
                    .findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.setRetainInstance(true)
            mapFragment.getMapAsync(this)
        }
        progressBar = findViewById(R.id.SpinKit)
        val wave: Wave = Wave()
        progressBar?.setIndeterminateDrawableTiled(wave)
        try {
            val succes = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.darkmap))
            if (!succes) {
                Toast.makeText(applicationContext, "Map Parsing Failed", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            Toast.makeText(applicationContext, e.localizedMessage, Toast.LENGTH_SHORT).show()

        }


        changeMap?.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {

                if (flag){
                    changeMap!!.setImageDrawable(ContextCompat.getDrawable(applicationContext,R.mipmap.map_icon_white))
                    try {
                        val succes = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this@MapsActivity, R.raw.standartmap))
                        if (!succes) {
                            Toast.makeText(applicationContext, "Map Parsing Failed", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(applicationContext, e.localizedMessage, Toast.LENGTH_SHORT).show()

                    }
                    flag = false
                }else if (!flag){
                    changeMap!!.setImageDrawable(ContextCompat.getDrawable(applicationContext,R.mipmap.map_icon_black))
                    try {
                        val succes = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this@MapsActivity, R.raw.darkmap))
                        if (!succes) {
                            Toast.makeText(applicationContext, "Map Parsing Failed", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(applicationContext, e.localizedMessage, Toast.LENGTH_SHORT).show()

                    }
                    flag = true
                }


            }
        })

        switch = findViewById(R.id.switch1) as Switch
        switch.visibility = View.INVISIBLE
        switch.setOnClickListener {
            if (switch.isChecked == true) {
                switch.setTextColor(Color.BLACK)
                try {
                    val succes = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this@MapsActivity, R.raw.standartmap))
                    if (!succes) {
                        Toast.makeText(applicationContext, "Map Parsing Failed", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(applicationContext, e.localizedMessage, Toast.LENGTH_SHORT).show()

                }
            } else {
                switch.setTextColor(Color.WHITE)
                try {
                    val succes = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.darkmap))
                    if (!succes) {
                        Toast.makeText(applicationContext, "Map Parsing Failed", Toast.LENGTH_SHORT).show()
                    }

                } catch (e: Exception) {
                    Toast.makeText(applicationContext, e.localizedMessage, Toast.LENGTH_SHORT).show()

                }
            }


        }
        /*
        myDialog = Dialog(this)
        aboutUs = findViewById(R.id.menu_item1) as FloatingActionButton?
        aboutUs?.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                myDialog!!.setContentView(R.layout.customcardpopup)
                var textCloseCard = myDialog!!.findViewById(R.id.txtclosecard) as TextView
                //var webButton = myDialog!!.findViewById(R.id.btnweb) as Button
                textCloseCard.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        myDialog!!.dismiss()
                    }


                })
                myDialog!!.show()
            }


        })*/


        var pd = ProgressDialog(this)
        //saveToParse() //bu kayıt etmek için
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        // val lastLocation=locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        // val lastUserLocation=LatLng(lastLocation.latitude,lastLocation.longitude)
        //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,18f))
        locationListener = object : LocationListener {
            override fun onLocationChanged(p0: Location?) {
                if (p0 != null) {
                    //mMap.clear()
                    //locationManager?.removeUpdates(locationListener)

                    //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,17f))
                    userLocation = LatLng(p0?.latitude, p0?.longitude)
                    // var deletedMarker =mMap.addMarker(MarkerOptions().position(userLocation).title("Benim Konumum").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)))


                }
                //mMap.addMarker(MarkerOptions().position(userLocation).title("Benim Konumum").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)))
                pd.dismiss()
                //getLocation()

            }


            override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {

            }

            override fun onProviderEnabled(p0: String?) {

            }

            override fun onProviderDisabled(p0: String?) {

                val i = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i)
            }
        }



        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 1)
            //ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),2)
        } else {
            //getLocation()
            locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener)
            //mMap.clear()
            val lastLocation = locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)


            if (lastLocation != null) {
                val lastUserLocation = LatLng(lastLocation.latitude, lastLocation.longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation, 11.5f), 3000, null)  //burası eskiye giriyor.verileri alıp yazdır.

                //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,18f),5000,null)
                //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,18f))
                pd.setMessage("Parklar Yükleniyor...")
                pd.show()
                pd.setCancelable(true)
                mMap.setMyLocationEnabled(true)

                //mUiSettings.setScrollGesturesEnabled(true)
            } else {

                mMap.setMyLocationEnabled(true)
                //Toast.makeText(this,"Lütfen Konumunuzu Açınız ve Uygulamayı Yeniden Başlatınız",Toast.LENGTH_LONG).show()
                //ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION),1)
            }


        }


    }

    fun upload() {
        var uuid = UUID.randomUUID()
        var imageName = "images/" + uuid + ".jpg"
        var storageReference = mStorageRef!!.child(imageName)
        val selectedImage = Uri.parse("android.resource://com.example.erdo.parsistapps/drawable/balmumuacik")
        //var selectedImage:InputStream=getContentResolver().openInputStream(uri)
        storageReference.putFile(selectedImage).addOnSuccessListener(this, object : OnSuccessListener<UploadTask.TaskSnapshot> {
            override fun onSuccess(taskSnapshot: UploadTask.TaskSnapshot) {
                val newReference = FirebaseStorage.getInstance().getReference(imageName)
                newReference.downloadUrl.addOnSuccessListener(object : OnSuccessListener<Uri> {
                    override fun onSuccess(uri: Uri) {
                        val downloadUrl = uri.toString()
                        val parkname = "Ispark Balmumcu Acık"
                        val parkDetail = "----"
                        val latitute = "41.061968"
                        val longitute = "29.010787"
                        val uuid1 = UUID.randomUUID()
                        val uuidString = uuid1.toString()
                        myRef?.child("Locations")?.child(uuidString)?.child("parkname")?.setValue(parkname)
                        myRef?.child("Locations")?.child(uuidString)?.child("parkdetail")?.setValue(parkDetail)
                        myRef?.child("Locations")?.child(uuidString)?.child("latitute")?.setValue(latitute)
                        myRef?.child("Locations")?.child(uuidString)?.child("longitute")?.setValue(longitute)
                        myRef?.child("Locations")?.child(uuidString)?.child("downloadurl")?.setValue(downloadUrl)
                        Toast.makeText(applicationContext, "Firebase Kayıt Başarılı", Toast.LENGTH_SHORT).show()

                    }


                })
            }


        }).addOnFailureListener(this, object : OnFailureListener {
            override fun onFailure(p0: Exception) {
                Toast.makeText(applicationContext, p0.localizedMessage, Toast.LENGTH_LONG).show()
                println("Hata" + p0.toString())
            }

        })
    }

    fun getFireLocation() {
        val forFireDetail = ArrayList<String>()
        val newReference = firebaseDatabase?.getReference("Locations")
        newReference?.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Toast.makeText(applicationContext, "Database Connection Failed ", Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(p0: DataSnapshot) {
                for (ds in p0.children) {
                    //println("Ds "+ds.getValue())
                    if (ds.exists()) {
                    val hashMap: (HashMap<String, String>) = ds.getValue() as HashMap<String, String>
                    val parkname = hashMap.get("parkname") as String
                    val parkdetail = hashMap.get("parkdetail") as String
                    val latitute = hashMap.get("latitute") as String
                    val situation = hashMap.get("situation") as String
                    val longitute = hashMap.get("longitute") as String
                    val latitudeDouble = latitute.toDouble()
                    val longituteDouble = longitute.toDouble()
                    val userLocation = LatLng(latitudeDouble, longituteDouble)
                   /*
                    val locationA = Location("point A")
                    locationA.setLatitude(com.example.erdo.parsistapps.userLocation.latitude)
                    locationA.setLongitude(com.example.erdo.parsistapps.userLocation.longitude)
                    val locationB = Location("point B")
                    locationB.setLatitude(latitudeDouble)
                    locationB.setLongitude(longituteDouble)
                    val uriImage = hashMap.get("downloadurl") as String
                    //println("myuri" + uriImage)
                    //Picasso.get().load(uriImage).into(imageView); resmi yüklüyor.
                    var distance = locationA.distanceTo(locationB)

                    distance = (distance / 1000.0).toFloat()
                    var s = String.format("%.2f", distance)
                        */
                    forFireDetail.add(parkname)
                      clusterManager = ClusterManager(this@MapsActivity, mMap)


                        mMap.setOnCameraIdleListener(clusterManager)
                        mMap.setOnMarkerClickListener(clusterManager)
                        clusterManager!!.setOnClusterClickListener(this@MapsActivity)
                        mMap.setOnInfoWindowClickListener(clusterManager)
                        //mMap.setInfoWindowAdapter(clusterManager!!.markerManager)
                        clusterManager!!.setOnClusterItemInfoWindowClickListener(this@MapsActivity)


                        clusterItems.add(MyItem(userLocation,parkname,parkdetail))
                        clusterManager?.addItems(clusterItems)




                       // clusterManager!!.cluster()
/*
                        clusterManager!!.markerCollection.setOnInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter{
                            override fun getInfoContents(p0: Marker?): View? {
                                return null

                            }

                            override fun getInfoWindow(p0: Marker?): View {
                                val inflater=getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                                val view=inflater.inflate(R.layout.custom_info_window,null)
                                val customWindowTitle=view.findViewById(R.id.title) as TextView
                                val customWindowSnippet=view.findViewById(R.id.snippet) as TextView
                                customWindowTitle.setText(parkname)
                                customWindowSnippet.setText(parkdetail)

                                return  view


                            }


                        })
*/


                    if (situation.equals("1")) {
                        //mMap.addMarker(MarkerOptions().position(userLocation).title(parkname).snippet("Detaylar:" + parkdetail + "\nKuşuçuşu Uzaklığım:" + s + " km").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher_situationparssist_park)))

                    }
                    if (situation.equals("0")) {
                       //mMap.addMarker(MarkerOptions().position(userLocation).title(parkname).snippet("Detaylar:" + parkdetail + "\nKuşuçuşu Uzaklığım:" + s + " km").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher_park)))

                    }
                   mMap.setInfoWindowAdapter(CustomInfoWindowAdapter(applicationContext))



                    mMap.setOnInfoWindowClickListener(object : GoogleMap.OnInfoWindowClickListener {
                        override fun onInfoWindowClick(p0: Marker?) {
                            val markerTitle = p0?.title
                            for (placename in forFireDetail) {
                                if (markerTitle.equals(placename)) {

                                    myDialog!!.setContentView(R.layout.customcardpopup)
                                    var textCloseCard = myDialog!!.findViewById(R.id.txtclosecard) as TextView
                                    //var webButton = myDialog!!.findViewById(R.id.btnweb) as Button
                                    textCloseCard.setOnClickListener(object : View.OnClickListener {
                                        override fun onClick(v: View?) {
                                            myDialog!!.dismiss()
                                        }


                                    })

                                    val reference = FirebaseDatabase.getInstance().getReference()
                                    val query = reference.child("Locations").orderByChild("parkname").equalTo(placename)
                                    query.addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onCancelled(p0: DatabaseError) {
                                            Toast.makeText(this@MapsActivity, "Database Park Conneciton Error", Toast.LENGTH_SHORT).show()
                                        }

                                        override fun onDataChange(p0: DataSnapshot) {
                                            for (ds in p0.children) {
                                                //println("Ds "+ds.getValue())
                                                var hashMap: (HashMap<String, String>) = ds.getValue() as HashMap<String, String>


                                                val parkname = hashMap.get("parkname") as String
                                                println("parkismi" + parkname)
                                                val parkdetail = hashMap.get("parkdetail") as String
                                                val uriImage = hashMap.get("downloadurl") as String
                                                val situation = hashMap.get("situation") as String
                                                val cardParkName = myDialog?.findViewById<TextView>(R.id.nameTextCardView) //custom dialog olduğu zaman id böyle çekiyorsun!!
                                                val cardParkDetailName = myDialog?.findViewById<TextView>(R.id.detailTextCardView) //custom dialog olduğu zaman id böyle çekiyorsun!!
                                                val cardParkImage = myDialog?.findViewById<ImageView>(R.id.detailImageCardView) //custom dialog olduğu zaman id böyle çekiyorsun!!
                                                val situationCard = myDialog?.findViewById<TextView>(R.id.situationCard) //custom dialog olduğu zaman id böyle çekiyorsun!!

                                                Picasso.get().load(uriImage).into(cardParkImage!!, object : Callback {
                                                    override fun onSuccess() {
                                                        progressBar?.setVisibility(View.INVISIBLE)
                                                    }

                                                    override fun onError(e: Exception?) {
                                                        Toast.makeText(applicationContext, "Resim Yüklenemedi", Toast.LENGTH_SHORT).show()
                                                    }


                                                })

                                                cardParkName!!.text = parkname
                                                cardParkDetailName!!.text = parkdetail
                                                if (situation.equals("1")) {
                                                    situationCard!!.text = "Açık Otopark"
                                                }
                                                if (situation.equals("0")) {
                                                    situationCard!!.text = "Kapalı Otopark"
                                                }


                                            }
                                        }

                                    })
                                    myDialog!!.show()


                                    // ------------
                                    /*  myDialog!!.setContentView(R.layout.customcardpopup)
                                      var textCloseCard = myDialog!!.findViewById(R.id.txtclosecard) as TextView
                                      //var webButton = myDialog!!.findViewById(R.id.btnweb) as Button
                                      textCloseCard.setOnClickListener(object : View.OnClickListener {
                                          override fun onClick(v: View?) {
                                              myDialog!!.dismiss()
                                          }


                                      })
                                      myDialog!!.show()
                                      val cardParkName=myDialog?.findViewById<TextView>(R.id.nameTextCardView) //custom dialog olduğu zaman id böyle çekiyorsun!!
                                      cardParkName!!.text=placename*/
                                    //  ------------------------------
                                    /*
                                    //myDialog = Dialog(MapsActivity.this)
                                    myDialog!!.setContentView(R.layout.customcardpopup)
                                    var textCloseCard = myDialog!!.findViewById(R.id.txtclosecard) as TextView
                                    //var webButton = myDialog!!.findViewById(R.id.btnweb) as Button
                                    textCloseCard.setOnClickListener(object : View.OnClickListener {
                                        override fun onClick(v: View?) {
                                            myDialog!!.dismiss()
                                        }


                                    })
                                    myDialog!!.show()

                                    Picasso.get().load(uriImage).into(detailImageView, object : Callback {
                                        override fun onSuccess() {
                                            progressBar?.setVisibility(View.INVISIBLE)
                                        }

                                        override fun onError(e: Exception?) {
                                            Toast.makeText(applicationContext, "Resim Yüklenemedi", Toast.LENGTH_SHORT).show()
                                        }


                                    })
                                    nameTextView.text = parkname
                                    detailTextView.text = parkdetail

                                    mMap.addMarker(MarkerOptions().position(userLocation).title(parkname))
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 17f))

*/


                                    //---------------------------------------
                                    /*             myDialog!!.setContentView(R.layout.customcardpopup)
                                                  var textCloseCard = myDialog!!.findViewById(R.id.txtclosecard) as TextView
                                                  //var webButton = myDialog!!.findViewById(R.id.btnweb) as Button
                                                  textCloseCard.setOnClickListener(object : View.OnClickListener {
                                                      override fun onClick(v: View?) {
                                                          myDialog!!.dismiss()
                                                      }


                                                  })

                                                  val reference = FirebaseDatabase.getInstance().getReference()
                                                  val query = reference.child("Locations").orderByChild("parkname").equalTo(placename)
                                                  query.addListenerForSingleValueEvent(object : ValueEventListener {
                                                      override fun onCancelled(p0: DatabaseError) {

                                                      }

                                                      override fun onDataChange(p0: DataSnapshot) {
                                                          for (ds in p0.children) {
                                                              //println("Ds "+ds.getValue())
                                                              var hashMap: (HashMap<String, String>) = ds.getValue() as HashMap<String, String>
                                                              //val parkname = hashMap.get("parkname") as String
                                                              val parkdetail = hashMap.get("parkdetail") as String
                                                              val latitute = hashMap.get("latitute") as String
                                                              val longitute = hashMap.get("longitute") as String
                                                              val latitudeDouble = latitute.toDouble()
                                                              val longituteDouble = longitute.toDouble()
                                                              val userLocation = LatLng(latitudeDouble, longituteDouble)
                                                              val uriImage = hashMap.get("downloadurl") as String

              /*                                                Picasso.get().load(uriImage).into(detailImageView,object : Callback{
                                                                  override fun onSuccess() {
                                                                      progressBar?.setVisibility(View.INVISIBLE)
                                                                  }

                                                                  override fun onError(e: Exception?) {
                                                                      Toast.makeText(applicationContext,"Resim Yüklenemedi",Toast.LENGTH_SHORT).show()
                                                                  }


                                                              })
                                                              */
                                                              nameTextCardView.text = "sdada"
                                                              detailTextView.text = parkdetail

                                                              mMap.addMarker(MarkerOptions().position(userLocation).title(parkname))
                                                              mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 17f))
                                                          }
                                                      }

                                                  })



                                                  myDialog!!.show()
              */
                                    //-------------------------------------------------
                                }

                            }
                        }
                    })
                  }
                }
                val renderer=MyItemCustomIcon(this@MapsActivity,mMap,clusterManager)
                clusterManager!!.renderer=renderer
               // clusterManager!!.cluster()
            }
        })


    }

    override fun onClusterClick(p0: Cluster<MyItem>?): Boolean {
       /* val userLocation = LatLng(p0!!.position.latitude, p0!!.position.longitude)
        val center = CameraUpdateFactory.newLatLng(p0.position)

        val zoom = CameraUpdateFactory.zoomIn()
        mMap.moveCamera(center)
        mMap.animateCamera(zoom)
        //mMap.animateCamera(CameraUpdateFactory.zoomIn())
        Toast.makeText(this, "Cluster Clicked", Toast.LENGTH_SHORT).show()

        //p0.position.latitude*/

        val builder = LatLngBounds.builder()
        val myItem = p0!!.getItems()
        for (item in myItem)
        {
            val myItemPosition = item.getPosition()
            builder.include(myItemPosition)
        }
        val bounds = builder.build()
        try
        {
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
        }
        catch (error:Exception) {
            print(error.message)
        }
        return true
    }

    override fun onClusterInfoWindowClick(p0: Cluster<MyItem>?) {
           // Toast.makeText(this,"yalancoo",Toast.LENGTH_SHORT).show()
    }

    override fun onClusterItemClick(p0: MyItem?): Boolean {
        Toast.makeText(this,"cluster item clicked",Toast.LENGTH_SHORT).show()
        return true
    }

    override fun onClusterItemInfoWindowClick(p0: MyItem?) {
        Toast.makeText(this,"Cluster Item Info clicked",Toast.LENGTH_LONG).show()
    }

    fun getLocation() {

        val forDetail = ArrayList<String>()

        val query = ParseQuery<ParseObject>("Locations")
        query.findInBackground { objects, e ->
            if (e != null) {
                Toast.makeText(applicationContext, e.localizedMessage, Toast.LENGTH_LONG).show()
            } else {
                if (objects.size > 0) {
                    for (parseObject in objects) {

                        val name = parseObject.get("name") as String
                        val detail = parseObject.get("detail") as String

                        val latitude = parseObject.get("latitute") as String
                        val longitute = parseObject.get("longitute") as String
                        val latituteDouble = latitude.toDouble()
                        val longituteDouble = longitute.toDouble()
                        val userLocate = LatLng(latituteDouble!!, longituteDouble!!)
                        forDetail.add(name)
                        // println(forDetail)
                        val locationA = Location("point A")
                        locationA.setLatitude(userLocation.latitude)
                        locationA.setLongitude(userLocation.longitude)
                        val locationB = Location("point B")
                        locationB.setLatitude(latituteDouble)
                        locationB.setLongitude(longituteDouble)
                        var distance = locationA.distanceTo(locationB)

                        distance = (distance / 1000.0).toFloat()
                        //var decimalFormat = DecimalFormat("#.##")
                        //var twoDigitsDistance = java.lang.Float.valueOf(decimalFormat.format(distance))
                        //var distanceString = java.lang.Float.toString(distance)
                        var s = String.format("%.2f", distance)


                        //var distanceString = java.lang.Float.toString(distance)
                        //var format = String.format("%.02f", distance)


                        //"Ortalama Uzaklık: "+distanceString+"\n"+detail
                        mMap.addMarker(MarkerOptions().position(userLocate).title(name).snippet("Detaylar:" + detail + "\nKuşuçuşu Uzaklığım:" + s + " km").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher_park)))
                        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocate,11.5f))
                        mMap.setInfoWindowAdapter(CustomInfoWindowAdapter(this))

                        mMap.setOnInfoWindowClickListener(object : GoogleMap.OnInfoWindowClickListener {
                            override fun onInfoWindowClick(marker: Marker) {
                                val markerTitle = marker.title
                                //Cycle through places array
                                for (place in forDetail) {
                                    if (markerTitle.equals(place)) {
                                        val intent = Intent(applicationContext, DetailActivity::class.java)
                                        intent.putExtra("name", place)
                                        startActivity(intent)
                                    }
                                }
                            }
                        })

                    }

                    // println(forDetail+"bu for döngüsünden cıktıktan sonradır")


                }
            }
        }

    }


    fun saveToParse() {
        var choosenImage: Bitmap? = null
        val byteArrayOutputStream = ByteArrayOutputStream()
        val bytes = byteArrayOutputStream.toByteArray()
        val parseFile = ParseFile("image.png", bytes)
        val parseObject = ParseObject("Locations")
        parseObject.put("name", "deneme")
        parseObject.put("latitute", "38.405765")
        parseObject.put("longitute", "27.098422")
        // parseObject.put("name","Erdo")
        //parseObject.put("latitute","38.376037")
        //parseObject.put("longitute","27.188325")
        //parseObject.put("image",parseFile)
        parseObject.saveInBackground { e: ParseException? ->
            if (e != null) {
                Toast.makeText(applicationContext, e.localizedMessage, Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(applicationContext, "Yer Kaydedildi", Toast.LENGTH_LONG).show()
            }

        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (grantResults.size > 0 && requestCode == 1) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener)
                //mMap.clear()
                val lastLocation = locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (lastLocation != null) {
                    val lastUserLocation = LatLng(lastLocation.latitude, lastLocation.longitude)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation, 11.5f), 3000, null)  //burası eskiye giriyor.verileri alıp yazdır.

                    //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,18f),5000,null)
                    //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,18f))

                    mMap.setMyLocationEnabled(true)
                } else {
                    mMap.setMyLocationEnabled(true)
                    // Toast.makeText(this,"Lütfen Konumunuzu Açınız ve Uygulamayı Yeniden Başlatınız",Toast.LENGTH_LONG).show()
                    //ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION),1)
                }
                //getLocation()
                getFireLocation()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}


/*

    fun getLocation(){


        val query=ParseQuery<ParseObject>("Locations")
        query.findInBackground{objects, e ->
            if(e!=null)
            {
                Toast.makeText(applicationContext,e.localizedMessage,Toast.LENGTH_LONG).show()
            }else{
                if (objects.size>0)
                {
                    for (parseObject in objects){

                        val name =parseObject.get("name") as String
                        val detail=parseObject.get("detail") as String

                        val latitude =parseObject.get("latitute") as String
                        val longitute =parseObject.get("longitute") as String
                        val latituteDouble=latitude.toDouble()
                        val longituteDouble=longitute.toDouble()
                        val userLocate=LatLng(latituteDouble!!,longituteDouble!!)
                        val locationA = Location("point A")
                        locationA.setLatitude(userLocation.latitude)
                        locationA.setLongitude(userLocation.longitude)
                        val locationB = Location("point B")
                        locationB.setLatitude(latituteDouble)
                        locationB.setLongitude(longituteDouble)
                        var distance = locationA.distanceTo(locationB)

                        distance= (distance/1000.0).toFloat()
                        //var decimalFormat = DecimalFormat("#.##")
                        //var twoDigitsDistance = java.lang.Float.valueOf(decimalFormat.format(distance))
                        //var distanceString = java.lang.Float.toString(distance)
                        var s = String.format("%.2f", distance)


                        //var distanceString = java.lang.Float.toString(distance)
                        //var format = String.format("%.02f", distance)


                        //"Ortalama Uzaklık: "+distanceString+"\n"+detail
                        mMap.addMarker(MarkerOptions().position(userLocate).title(name).snippet("Detaylar:"+detail+"\nKuşuçuşu Uzaklığım:"+s+" km").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher_park)))
                        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocate,11.5f))
                        mMap.setInfoWindowAdapter(CustomInfoWindowAdapter(this))
                        /*   mMap.setOnInfoWindowClickListener(object: GoogleMap.OnInfoWindowClickListener{
                               override fun onInfoWindowClick(marker:Marker) {
                                   Toast.makeText(applicationContext,"Şuna Tıkladınız :"+name,Toast.LENGTH_SHORT).show()
                               }
                           })  */
                        // nameInfoArray.add(name)
                        // nameInfoArray.clear()
                    }


                }
            }
        }

    }



*/