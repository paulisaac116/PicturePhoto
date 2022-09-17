package com.example.picture

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
//import com.google.firebase.firestore.ktx.firestore

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val db = Firebase.firestore.collection("Photos")
    private var locationRequest: LocationRequest? = null

    private var latitudeCurrentUser = ""
    private var longitudeCurrentUser = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        locationRequest = LocationRequest.create()



        // set the interval time to update the current location
        locationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest!!.interval = 5000
        locationRequest!!.fastestInterval = 2000

        val REQUEST_CODE = 200

        btn_shoot.setOnClickListener { handleTakePhoto(Intent(MediaStore.ACTION_IMAGE_CAPTURE), REQUEST_CODE) }


        // active the current GPS location on the device
        getCurrentLocation()
    }

    private fun handleTakePhoto(intent: Intent, REQUEST_CODE: Int) {

        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                val imageBitmap = intent?.extras?.get("data") as Bitmap
                //val imageView = findViewById<ImageView>(R.id.imageView)
                imageView.setImageBitmap(imageBitmap)
            }
        }

    }

    private fun getCurrentLocation() {


        // request the permission to access the location configuration
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(
                    this@MainActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                if (isGPSEnabled()) {
                    LocationServices.getFusedLocationProviderClient(this@MainActivity)
                        .requestLocationUpdates(locationRequest!!, object : LocationCallback() {
                            override fun onLocationResult(locationResult: LocationResult) {
                                super.onLocationResult(locationResult)
                                LocationServices.getFusedLocationProviderClient(this@MainActivity)
                                    .removeLocationUpdates(this)
                                if (locationResult != null && locationResult.locations.size > 0) {
                                    val index = locationResult.locations.size - 1
                                    val latitude1 = locationResult.locations[index].latitude
                                    val longitude1 = locationResult.locations[index].longitude

                                    latitudeCurrentUser = latitude1.toString()
                                    longitudeCurrentUser = longitude1.toString()

                                    //latUser.text = latitudeCurrentUser
                                    //longUser.text = longitudeCurrentUser

                                    //println("user id: ${idUser.text}")

                                    //Firebase.firestore.collection("Locations")
//                                    db.document(idUser.text.toString()).update(
//                                        mapOf(
//                                            "lati" to latitudeCurrentUser,
//                                            "long" to longitudeCurrentUser
//                                        )
//                                    ).addOnCompleteListener { succesfulLocation() }
//                                        .addOnFailureListener { error -> errorLocation(error) }

                                }
                            }
                            // execute a loop to get the location data
                        }, Looper.getMainLooper())
                } else {
                    turnOnGPS()
                }
            } else {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            }
        }
    }

    private fun turnOnGPS() {

        // request the current config data of the local service
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest!!)
        builder.setAlwaysShow(true)
        val result = LocationServices.getSettingsClient(
            applicationContext
        )
            .checkLocationSettings(builder.build())
        result.addOnCompleteListener(OnCompleteListener<LocationSettingsResponse?> { task ->
            try {
                val response = task.getResult(ApiException::class.java)
                Toast.makeText(this@MainActivity, "GPS is already tured on", Toast.LENGTH_SHORT)
                    .show()
            } catch (e: ApiException) {
                when (e.statusCode) {

                    // the location service is turn off
                    //  show a card to active the location
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                        val resolvableApiException = e as ResolvableApiException
                        resolvableApiException.startResolutionForResult(this@MainActivity, 2)
                    } catch (ex: IntentSender.SendIntentException) {
                        ex.printStackTrace()
                    }

                    // the mobile phone does not have the 'location service' installed
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {}
                }
            }
        })
    }

    private fun isGPSEnabled(): Boolean {

        // check if the location service is active
        // if active, returns 'true'
        // if not, return 'false'

        // intitialize the LocationManager
        var locationManager: LocationManager? = null
        var isEnabled = false
        if (locationManager == null) {
            locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        }
        isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        return isEnabled
    }

    private fun succesfulLocation() {

        Toast.makeText(this, "Localización registrada", Toast.LENGTH_LONG).show()
    }

    private fun errorLocation(error: Exception) {

        Toast.makeText(
            this,
            "Se ha producido un error actualizando la localización ${error.message}",
            Toast.LENGTH_LONG
        ).show()

    }

}