package com.example.ramanigps


import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.LocationManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ramanigps.ui.theme.RamaniGPSTheme
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.Style
import org.ramani.compose.CameraPosition
import org.ramani.compose.Circle
import org.ramani.compose.MapLibre

class MainActivity : ComponentActivity(), LocationListener {

    lateinit var locationManager: LocationManager

    // You must create the style builder as an attribute of the activity and then
    // pass it to composables as a parameter. If you create the style builder on
    // each recompose, the app may crash!
    var styleBuilder = Style.Builder().fromUri("https://tiles.openfreemap.org/styles/bright")
    
    val viewModel: GPSViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermissions()
        setContent {
            RamaniGPSTheme {
                var latLng by remember { mutableStateOf(LatLng(50.9, -1.4))}
                viewModel.latLngLiveData.observe(this) {
                    latLng = it
                }
                MapLibre(
                    modifier = Modifier.fillMaxSize(),
                    cameraPosition = CameraPosition(
                        target = latLng,
                        zoom = 14.0
                    ),
                    styleBuilder = styleBuilder
                ) {

                    Circle(
                        center=latLng,
                        radius=20f,
                        opacity=0.3f,
                        color="#0000ff"
                    )
                }
            }
        }
    }

    fun checkPermissions() {
        val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.POST_NOTIFICATIONS)
        if (permissions.any { checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED }) {
            val launcher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { isGranted ->

                // With multiple permissions, isGranted is a map and we use the
                // specific permission as a key (index) to test if that specific
                // permission has been granted
                if(isGranted[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
                    startGPS()
                }

                if(isGranted[Manifest.permission.POST_NOTIFICATIONS] == true) {
                    Toast.makeText(this, "Notification permission granted", Toast.LENGTH_LONG).show()
                }
            }
            // Launch the launcher with the array of requested permissions
            launcher.launch(permissions)
        } else {
            startGPS()
        }
    }

    @SuppressLint("MissingPermission")
    fun startGPS() {
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 5f, this)
    }

    override fun onLocationChanged(location: Location) {
        viewModel.latLng = LatLng(location.latitude, location.longitude)
    }
}


