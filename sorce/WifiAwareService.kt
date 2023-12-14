package com.knockjoy.location_share

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.aware.AttachCallback
import android.net.wifi.aware.DiscoverySessionCallback
import android.net.wifi.aware.PeerHandle
import android.net.wifi.aware.PublishConfig
import android.net.wifi.aware.PublishDiscoverySession
import android.net.wifi.aware.SubscribeConfig
import android.net.wifi.aware.SubscribeDiscoverySession
import android.net.wifi.aware.WifiAwareManager
import android.net.wifi.aware.WifiAwareSession
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat

class WifiAwareService(context: Context) {
    private var mAwaresession:WifiAwareSession?=null
    var mSession:SubscribeDiscoverySession?=null
    var mPeerHandle:PeerHandle?=null
    final val mHandle:Handler= Handler()
    public final val AWARE_SERVICE_NAME:String="Aware_Service"
    var mWifiAwareManager:WifiAwareManager?=null
    val master:Context=context

    fun setup(){
        if(!master.packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI_AWARE)){
            //can't use
        }
        val wifiAwareManager=this.master.getSystemService(Context.WIFI_AWARE_SERVICE) as WifiAwareManager?
        mWifiAwareManager=wifiAwareManager
        val filter=IntentFilter(WifiAwareManager.ACTION_WIFI_AWARE_STATE_CHANGED)
        val myReceiver=object :BroadcastReceiver(){
            override fun onReceive(context: Context?, intent: Intent?) {
                if(this@WifiAwareService.mWifiAwareManager?.isAvailable == true){
                    Toast.makeText(this@WifiAwareService.master,"can use WiFi-Aware", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(this@WifiAwareService.master,"can not use WiFi-Aware", Toast.LENGTH_SHORT).show()
                }
                TODO("Not yet implemented")
            }
        }
        this.master.registerReceiver(myReceiver,filter)

    }
    fun startService(){
        if (this.master.packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI_AWARE)) {
            mWifiAwareManager?.attach(MyattachCallback(), null)
        }
    }


    inner class MyattachCallback: AttachCallback() {
        override fun onAttached(session: WifiAwareSession?) {
            val debugintent=Intent("Main")
            debugintent.putExtra("action","test")
            this@WifiAwareService.master.sendBroadcast(debugintent)
            super.onAttached(session)
            Log.i("WifiAwareService","onAttach")
            Toast.makeText(this@WifiAwareService.master,"onAttach",Toast.LENGTH_SHORT).show()
            this@WifiAwareService.mAwaresession=session
            val publish_config: PublishConfig = PublishConfig.Builder()
                .setServiceName(AWARE_SERVICE_NAME)
                .build()
            if (ActivityCompat.checkSelfPermission(
                    this@WifiAwareService.master,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    this@WifiAwareService.master,
                    Manifest.permission.NEARBY_WIFI_DEVICES
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

            this@WifiAwareService.mAwaresession?.publish(publish_config, object :
                DiscoverySessionCallback(){
                override fun onPublishStarted(session: PublishDiscoverySession) {
                    super.onPublishStarted(session)
                    Log.i("WifiAwareService","onPublishStarted()")
                    Toast.makeText(this@WifiAwareService.master,"onPublishStarted()",Toast.LENGTH_SHORT).show()

                }

                override fun onMessageReceived(peerHandle: PeerHandle?, message: ByteArray) {
                    super.onMessageReceived(peerHandle, message)
                    var data=String(message,Charsets.UTF_8).split(",")
                    Log.i("onMSGReceived", String(message,Charsets.UTF_8))
                    val intent=Intent("Main")
                    intent.putExtra("action","send_data")
                    intent.putExtra("latitude",data[0])
                    intent.putExtra("longitude",data[1])
                    this@WifiAwareService.master.sendBroadcast(intent)
//                    Toast.makeText(this@WifiAwareService.master,"onMessageReceived : "+ String(message,Charsets.UTF_8),Toast.LENGTH_SHORT).show()
                }
            }
                ,null)



            val subscribe_config: SubscribeConfig = SubscribeConfig.Builder()
                .setServiceName(AWARE_SERVICE_NAME)
                .build()

            this@WifiAwareService.mAwaresession?.subscribe(subscribe_config,object : DiscoverySessionCallback(){

                override fun onServiceDiscovered(
                    peerHandle: PeerHandle?,
                    serviceSpecificInfo: ByteArray?,
                    matchFilter: MutableList<ByteArray>?
                ) {

                    super.onServiceDiscovered(peerHandle, serviceSpecificInfo, matchFilter)
                    mPeerHandle=peerHandle
                    Toast.makeText(this@WifiAwareService.master,"start",Toast.LENGTH_SHORT).show()
                    }
                override fun onSubscribeStarted(session: SubscribeDiscoverySession) {
                    super.onSubscribeStarted(session)
                    mSession=session
                    Log.i("subscribe", session.toString())
                    Toast.makeText(this@WifiAwareService.master,"called onSubscribeStarted()",Toast.LENGTH_SHORT).show()
                    if (mPeerHandle != null) {
                        Toast.makeText(this@WifiAwareService.master, "send message", Toast.LENGTH_SHORT).show()
                        var messageId: Int = 1
                        session.sendMessage(mPeerHandle!!, messageId, "testMessage".toByteArray())
                    }
                }
            }

                ,null)


        }

        override fun onAttachFailed() {
            super.onAttachFailed()
            Toast.makeText(this@WifiAwareService.master, "onAttachFailed", Toast.LENGTH_SHORT).show()
        }

    }


    fun sendMessage(message:String){
        if(mPeerHandle!=null){
            mSession?.sendMessage(mPeerHandle!!,1,message.toByteArray())
//            Toast.makeText(this.master,"succeed",Toast.LENGTH_SHORT).show()
        }
        else{
//            Toast.makeText(this.master,"failed",Toast.LENGTH_SHORT).show()
        }
    }
}
