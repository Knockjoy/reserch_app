package com.knockjoy.location_share

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import java.util.Date
import java.util.Objects
import java.util.Timer
import java.util.TimerTask

class MainActivity : AppCompatActivity() {
    var pingTimer:Timer?=null
    var service:WifiAwareService?=null
    var location:Location_listener?=null
    var peerlatitude:Double=0.0
    var peerlongitude:Double=0.0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        service = WifiAwareService(this)
        service?.setup()
        service?.startService()
        location= Location_listener(this)
        location?.set_listener()
        this.pingTimer = Timer()
        pingTimer?.schedule(dif_Task(), Date(), 5 * 1000)
        val filter=IntentFilter()
        filter.addAction("Main")
        registerReceiver(MainReceiver(),filter)
    }
    inner class dif_Task:TimerTask(){
        override fun run() {
            Log.i("task","run task")
            this@MainActivity.service?.sendMessage("${location?.now_latitude},${location?.now_longitude}")
//            change_content(0.0,0.0,location?.now_latitude!!,location?.now_longitude!!)
        }
    }
    inner class MainReceiver:BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.i("receiver","received")
            if(intent?.getStringExtra("action")=="send_data"){
                val latitude = intent?.getStringExtra("latitude")
                val longitude= intent?.getStringExtra("longitude")
                this@MainActivity.peerlatitude=latitude?.toDouble()!!
                this@MainActivity.peerlongitude=longitude?.toDouble()!!
                change_content(location?.now_latitude!!,location?.now_longitude!!,latitude?.toDouble()!!,longitude?.toDouble()!!)
                }
            if(intent?.getStringExtra("action")=="mylocation"){
                Log.i("mainreceiver",location?.now_latitude.toString())
                change_content(location?.now_latitude!!,location?.now_longitude!!,this@MainActivity.peerlatitude,this@MainActivity.peerlongitude)
            }
            if(intent?.getStringExtra("action")=="test"){
                Log.i("MainReceiver","test")
            }
        }
    }

    fun change_content(m1:Double,m2:Double,y1:Double,y2:Double){
        var result= floatArrayOf(0f,0f,0f)
        val mainhandler=Handler(Looper.getMainLooper())
        Location.distanceBetween(m1,m2,y1,y2,result)
        val distance=result[0]
        val direction=result[1]
        val distance_text=distance.toString()
        Log.i("change","$distance_text,$direction")
        runOnUiThread{
            this@MainActivity.findViewById<TextView>(R.id.distance).setText(distance_text)
            this@MainActivity.findViewById<TextView>(R.id.direction).apply { rotation=direction }

        }
    }

}