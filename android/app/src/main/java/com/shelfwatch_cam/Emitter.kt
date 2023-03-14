package com.shelfwatch_cam

import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule

class Emitter(val reactContext: ReactApplicationContext) {

  private fun sendEvent(reactContext: ReactContext, eventName: String, params: WritableMap?) {
    reactContext
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
      .emit(eventName, params)
  }

  private var listenerCount = 0

  @ReactMethod
  fun addListener(eventName: String) {
    if (listenerCount == 0) {
      // Set up any upstream listeners or background tasks as necessary
    }

    listenerCount += 1
  }

  @ReactMethod
  fun removeListeners(count: Int) {
    listenerCount -= count
    if (listenerCount == 0) {
      // Remove upstream listeners, stop unnecessary background tasks
    }
  }


  fun emitEventAndIncrement() {
    val eventMap = Arguments.createMap()
    eventMap.putString("eventProperty", "someValue")
    sendEvent(reactContext, "EventReminder", eventMap)
    listenerCount += 1
  }


}