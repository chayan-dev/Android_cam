package com.shelfwatch_cam

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter

class CameraModule(reactContext: ReactApplicationContext): ReactContextBaseJavaModule(reactContext) {

  override fun getName(): String = "CameraModule"

  private val rContext = reactApplicationContext
  private lateinit var mLocalBroadcastReceiver: LocalBroadcastReceiver

  init {
    mLocalBroadcastReceiver = LocalBroadcastReceiver()
    val localBroadcastManager = LocalBroadcastManager.getInstance(rContext)
    localBroadcastManager.registerReceiver(
      mLocalBroadcastReceiver,
      IntentFilter("my-custom-event")
    )
  }

  inner class LocalBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent) {
      val someData = intent.getStringExtra("my-extra-data")
      rContext.getJSModule(RCTDeviceEventEmitter::class.java)
        .emit("JS-Event", someData)
    }
  }

  @ReactMethod
  fun createToast(str:String){
    Toast.makeText(rContext,"data got from android : $str", Toast.LENGTH_LONG).show()
  }

  @ReactMethod
  fun openCamera(){

//    emitEvent()

    val intent = Intent(rContext,CameraActivity::class.java)
    if(intent.resolveActivity(rContext.packageManager) != null){
      intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
      rContext.startActivity(intent)
    }
  }

  fun emitEvent() {
    rContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
      .emit("eventName", "params")
  }

}