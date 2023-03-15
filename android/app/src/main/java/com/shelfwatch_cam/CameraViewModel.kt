package com.shelfwatch_cam

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.camera.core.ImageProxy
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.facebook.react.bridge.ReadableMap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.ByteArrayOutputStream


class CameraViewModel : ViewModel()  {

  // Expose screen UI state
  private val _uiState = MutableStateFlow(CameraUiState())
  val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()
  val currentImageList = arrayListOf<String>()
  val imageCapturedList: MutableLiveData<ArrayList<String>> = MutableLiveData()

  val defaultData = hashMapOf<String, Any>("isAutomatic" to false, "left" to "", "right" to "",
  "top" to "", "direction" to "", "nextStep" to "", "stepsTaken" to arrayListOf<String>(),
  "previewList" to arrayListOf<String>()
  )

  val sendingData = hashMapOf<String,Any>( "state" to defaultData, "image_base64" to "abcd")

  //business logic
  fun updateUiState(){

  }

  fun handleClickedImage(image: ImageProxy){
    val bm = image.convertImageProxyToBitmap()
    val baos = ByteArrayOutputStream()
    bm.compress(Bitmap.CompressFormat.PNG, 100, baos)
    val bArray = baos.toByteArray()
    val base64Img = Base64.encodeToString(bArray, Base64.DEFAULT)
    currentImageList.add(base64Img)
    imageCapturedList.value = currentImageList
    Log.d("imgList1: ",currentImageList.size.toString())
  }

  fun broadcast(context: Context){
    val customEvent = Intent("my-custom-event")

//    customEvent.putExtra("state", defaultData)
//    customEvent.putExtra("image_base64", "abcd")
    customEvent.putExtra("dataMap", sendingData)
//    customEvent.putExtra("my-extra-data", "that's it")
    context.let { it1 -> LocalBroadcastManager.getInstance(it1) }.sendBroadcast(customEvent)
  }

  fun ImageProxy.convertImageProxyToBitmap(): Bitmap {
    val buffer = planes[0].buffer
    buffer.rewind()
    val bytes = ByteArray(buffer.capacity())
    buffer.get(bytes)
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
  }
}

data class CameraUiState(
  val right: String = "",
  val left : String = "",
  val top : String = ""
)
