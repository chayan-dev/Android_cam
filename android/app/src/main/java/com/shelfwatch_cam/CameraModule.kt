package com.shelfwatch_cam

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.shelfwatch_cam.utils.MapUtil
import org.json.JSONObject

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
      val stateMap = intent.getSerializableExtra("dataMap") as Map<String,Any>
      val eventName = intent.getStringExtra("event_name")
      Log.d("cam_mod: ", stateMap.toString())
      val gson = Gson()
      val response = gson.toJson(stateMap)
      val dataMap = MapUtil.convertJsonToMap(JSONObject(response))
      Log.d("cam_mod1: ", eventName.toString())
//      val param = Arguments.createMap()
//      param.putMap(someData)
      if(eventName != null){
      emitEvent(eventName, dataMap)
    }
//      rContext.getJSModule(RCTDeviceEventEmitter::class.java)
//        .emit("JS-Event", dataMap)
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

  @ReactMethod
  fun dispatchDataToNativeSide(data: String){
    currentActivity?.applicationContext?.let { broadcastDispatchEvent(it, data) }
    Log.d("Data_to_kotlin",data.toString())
  }

  @ReactMethod
  fun sendtoNative(data: String){
    Log.d("Data_in_kotlin",data.toString())
  }

  fun emitEvent(eventName: String, param: Any) {
    rContext.getJSModule(RCTDeviceEventEmitter::class.java)
      .emit(eventName, param)
  }

  private fun broadcastDispatchEvent(context: Context, param: String){
    val customEvent = Intent("dispatch-event")
    customEvent.putExtra("dataMap", param)
    context.let { it1 -> LocalBroadcastManager.getInstance(it1) }.sendBroadcast(customEvent)
  }



}