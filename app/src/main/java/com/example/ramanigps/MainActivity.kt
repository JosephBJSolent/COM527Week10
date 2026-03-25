package com.example.ramanigps


import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.example.ramanigps.ui.theme.RamaniGPSTheme
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.Style
import org.ramani.compose.CameraPosition
import org.ramani.compose.Circle
import org.ramani.compose.MapLibre

class MainActivity : ComponentActivity(), LocationListener {

    lateinit var locationManager: LocationManager
    lateinit var nMgr: NotificationManager

    // You must create the style builder as an attribute of the activity and then
    // pass it to composables as a parameter. If you create the style builder on
    // each recompose, the app may crash!
    var styleBuilder = Style.Builder().fromUri("https://tiles.openfreemap.org/styles/bright")
    
    val viewModel: GPSViewModel by viewModels()

    val channelID = "EMAIL_CHANNEL"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermissions()
        nMgr = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(channelID, "Email notifications", NotificationManager.IMPORTANCE_DEFAULT)
        nMgr.createNotificationChannel(channel)
        setContent {
            RamaniGPSTheme {
                // state variable for displaying location info
                var dialogVisible by remember { mutableStateOf(false) }
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
                        color="#0000ff",
                        onClick = {
                            dialogVisible = true
                        }
                    )
                }
                if (dialogVisible) {
                    AlertDialog(
                        title = {
                            Text("Current Position")
                        },
                        text = {
                            Text(
                                "Longitude: ${latLng.longitude} Latitude: ${latLng.latitude}"
                            )
                        },
                        onDismissRequest = {
                            dialogVisible = false
                        },
                        dismissButton = {
                            Button(onClick = {
                                dialogVisible = false
                            }) {
                                Text("Dismiss")
                            }
                        },
                        confirmButton = {
                            Button(onClick = {
                                dialogVisible = false
                            }) {
                                Text("Confirm")
                            }
                        }
                    )
                }
            }
        }
    }

    fun checkPermissions() {
        Log.d("ramanigps1", "checkPermissions()")

        val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.POST_NOTIFICATIONS)
        if (permissions.any { checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED }) {
            Log.d("ramanigps1", "We need to request some permissions")
            val launcher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { isGranted ->
                Log.d("ramanigps1", "Granted status: $isGranted")
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
            Log.d("ramanigps1", "All permissions granted already")
            Toast.makeText(this, "All permissions granted already", Toast.LENGTH_LONG).show()
            startGPS()
        }
    }

    @SuppressLint("MissingPermission")
    fun startGPS() {
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 5f, this)
    }

    var uniqueIdCounter = 0
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onLocationChanged(location: Location) {
        viewModel.latLng = LatLng(location.latitude, location.longitude)
        uniqueIdCounter++
        println("got here")
        val notification = Notification.Builder(this, channelID)
            .setContentTitle("GPS update")
            .setContentText("The GPS position has changed")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // change this icon
            .build()
        nMgr.notify(uniqueIdCounter, notification) // uniqueId is a unique ID for this notification
    }
}


