package com.cmc.mytaxiapp

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import pub.devrel.easypermissions.EasyPermissions




class MainActivity : AppCompatActivity(), OnMapReadyCallback {


    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var btnStart: Button
    private lateinit var tvDistance: TextView
    private lateinit var tvTime: TextView
    private lateinit var tvFare: TextView

    private var startTime: Long = 0
    private var startLocation: LatLng? = null
    private var currentLocation: LatLng? = null
    private var totalDistance: Float = 0f
    private var totalTime: Long = 0
    private var fare: Float = 0f

    private val baseFare = 2.5f
    private val farePerKm = 1.5f
    private val farePerMinute = 0.5f

    // LocationRequest and LocationCallback for continuous updates
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        tvDistance = findViewById(R.id.tvDistance)
        tvTime = findViewById(R.id.tvTime)
        tvFare = findViewById(R.id.tvFare)
        btnStart = findViewById(R.id.btnStart)

        // Check for location and notification permissions
        if (EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.POST_NOTIFICATIONS)) {
            initMap()
        } else {
            EasyPermissions.requestPermissions(
                this,
                "This app needs location and notification permissions",
                1,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS
            )
        }

        // Setup location request for continuous updates
        setupLocationRequest()

        // Button click logic
        btnStart.setOnClickListener {
            if (startLocation == null) {
                startTrip()
            } else {
                stopTrip()
            }
        }
    }

    private fun initMap() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun setupLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            interval = 5000 // Update location every 5 seconds
            fastestInterval = 2000 // Get location updates at least every 2 seconds
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY // High accuracy location
        }

        // Properly implementing the LocationCallback with onLocationResult override
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult?.let {
                    for (location in it.locations) {
                        // Update the current location with each new update
                        currentLocation = LatLng(location.latitude, location.longitude)
                        mMap.clear() // Clear previous markers
                        currentLocation?.let { loc ->
                            mMap.addMarker(MarkerOptions().position(loc).title("Driver"))
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 15f))

                            // Calculate the distance from the start location
                            startLocation?.let { startLoc ->
                                val results = FloatArray(1)
                                Location.distanceBetween(startLoc.latitude, startLoc.longitude, loc.latitude, loc.longitude, results)
                                totalDistance = results[0] / 1000f // Convert meters to kilometers
                            }
                        }
                    }
                    updateUI()
                }
            }
        }
    }

    private fun startTrip() {
        startTime = System.currentTimeMillis()
        startLocation = getCurrentLocation()
        totalDistance = 0f
        totalTime = 0
        fare = baseFare
        btnStart.text = "Stop Trip"
        updateUI()
        startLocationUpdates()
    }

    private fun stopTrip() {
        val endTime = System.currentTimeMillis()
        totalTime = (endTime - startTime) / 1000 / 60  // Convert to minutes
        fare += totalDistance * farePerKm + totalTime * farePerMinute
        showNotification()
        updateUI()
        stopLocationUpdates()
    }

    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun getCurrentLocation(): LatLng? {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    currentLocation = LatLng(location.latitude, location.longitude)
                    mMap.clear()
                    currentLocation?.let { loc ->
                        mMap.addMarker(MarkerOptions().position(loc).title("Driver"))
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 15f))
                    }
                }
            }
        return currentLocation
    }

    private fun updateUI() {
        tvDistance.text = "${totalDistance} km"
        tvTime.text = "$totalTime min"
        tvFare.text = "$fare DH"
    }

    private fun showNotification() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.POST_NOTIFICATIONS)) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notification = NotificationCompat.Builder(this, "tripChannel")
                .setContentTitle("Trip Finished")
                .setContentText(" $fare DH, ${totalDistance} km,  $totalTime min")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    "tripChannel",
                    "Trip Notifications",
                    NotificationManager.IMPORTANCE_HIGH
                )
                notificationManager.createNotificationChannel(channel)
            }

            notificationManager.notify(1, notification)
        } else {
            Toast.makeText(this, "Please grant notification permissions to receive trip updates", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.isMyLocationEnabled = true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.POST_NOTIFICATIONS)) {
                initMap()
            } else {
                Toast.makeText(this, "Permissions required for this app", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
