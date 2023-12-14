package com.knockjoy.location_share

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.util.Date

class Location_listener(context: Context) {
    val host_context=context
    var times=0
    //create Location Manager's instance
    var  locationManager=context.getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager?
    var now_latitude:Double?=0.0
    var now_longitude:Double?=0.0
    val locationListener=object : LocationListener {
        override fun onLocationChanged(location: Location){

            now_latitude=location.latitude
            now_longitude=location.longitude
            var result= floatArrayOf(0f,0f,0f)
            Location.distanceBetween(now_latitude!!,now_longitude!!, 34.47201526,135.48609537,result)
            Log.i("location",result[0].toString())
            val intent=Intent("Main")
            intent.putExtra("action","mylocation")
            context.sendBroadcast(intent)
        Log.i("location","$now_latitude,$now_longitude")
        }
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
            // Handle status changes
        }

        override fun onProviderEnabled(provider: String) {
            // Handle provider enabled
        }

        override fun onProviderDisabled(provider: String) {
            // Handle provider disabled
        }
    }
    fun set_listener(){
        if (ActivityCompat.checkSelfPermission(
                this.host_context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this.host_context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        //set location service
        this.locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0.5f,locationListener)
    }
}