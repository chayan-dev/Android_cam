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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream


class CameraViewModel : ViewModel()  {

  // Expose screen UI state
  private val _uiState = MutableStateFlow(CameraUiState())
  val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()
  val currentImageList = arrayListOf<CaptureImageModel>()
  val imageCapturedList: MutableLiveData<ArrayList<CaptureImageModel>> = MutableLiveData()

  val defaultData = hashMapOf<String, Any>("isAutomatic" to false, "left" to "", "right" to "",
  "top" to "", "direction" to "", "nextStep" to "", "stepsTaken" to arrayListOf<String>(),
  "previewList" to arrayListOf<String>()
  )

  var currentData = hashMapOf<String, Any>("isAutomatic" to false, "left" to "", "right" to "",
    "top" to "", "direction" to "", "nextStep" to "", "stepsTaken" to arrayListOf<String>(),
    "previewList" to arrayListOf<String>()
  )

  var sendingData = hashMapOf<String,Any>( "state" to currentData, "image_base64" to "abcd")

  fun updateUiStateMap(stateJsonString: String){
    val mapObj: HashMap<String, Any> = Gson().fromJson(
      stateJsonString, object : TypeToken<HashMap<String?, Any?>?>() {}.getType())
    currentData = mapObj
    Log.d("Data_to_kotlin__map", mapObj.toString() )

    renderUi()
  }

  fun renderUi(){
    Log.d("state_received", currentData.toString())
    val preViewList = currentData["previewList"] as ArrayList<String>
    val guideResult = showGuide()

    if(currentData["isAutomatic"] == false){
      _uiState.update { state->
        state.copy(
          showArrowAllDirection = true
        )
      }
    }
    if(preViewList.size > 0 && currentData["isAutomatic"] == false){
      _uiState.update { state->
        state.copy(
          showArrowAllDirection = true
        )
      }
    }
    else {
      _uiState.update { state->
        state.copy(
          showArrowAllDirection = false
        )
      }
    }

    if( guideResult.toString() == "left"){
      _uiState.update { state->
        state.copy(
          showLeftArrow = true,
          showDownArrow = false,
          showRightArrow = false,
          leftGuide= true,
          downGuide= false,
          rightGuide = false
        )
      }
    }
    else if( guideResult.toString() == "down"){
      _uiState.update { state->
        state.copy(
          showLeftArrow = false,
          showDownArrow = true,
          showRightArrow = false,
          leftGuide= false,
          downGuide= true,
          rightGuide = false
        )
      }
    }
    else if( guideResult.toString() == "right"){
      _uiState.update { state->
        state.copy(
          showLeftArrow = false,
          showDownArrow = false,
          showRightArrow = true,
          leftGuide= false,
          downGuide= false,
          rightGuide = true
        )
      }
    }

    doImageOverlay()

  }

  private fun doImageOverlay() {

    Log.d("image_overlay",currentData["left"].toString())
    Log.d("image_overlay",currentData.toString())
    try{
//      var leftMap = currentData["left"] as Map<String, String>

      //
      var leftValue = currentData["left"]
      var leftMap: Map<String, String>
      if(leftValue!= null) {
        leftMap = currentData["left"] as Map<String, String>
      }
      else leftMap = mapOf()
      ////
      val leftString = leftMap["index"]
      val leftIndex = leftString?.toInt()
      if (leftString != null) {
        if (leftString.isNotEmpty()) {
          try{
            val leftImg = currentImageList.first { it.index == leftIndex }
            _uiState.update { state ->
              state.copy(
                leftOverlayImage = leftImg.image
              )
            }
          } catch (e: NoSuchElementException ){
            _uiState.update { state ->
              state.copy(
                leftOverlayImage = null
              )
            }
          }
        } else {
          _uiState.update { state ->
            state.copy(
              leftOverlayImage = null
            )
          }
        }
      }
    } catch ( e: ClassCastException){
      _uiState.update { state ->
        state.copy(
          leftOverlayImage = null
        )
      }
      Log.d("state_livedata","leftoverlay excption")
    }

    try{
//      val rightMap = currentData["right"] as Map<String, String>
      //
      var rightValue = currentData["right"]
      var rightMap: Map<String, String>
      if(rightValue!= null) {
        rightMap = currentData["right"] as Map<String, String>
      }
      else rightMap = mapOf()
      ////

      Log.d("rightMap", rightMap.toString())
      val rightString = rightMap["index"]
      val rightIndex = rightString?.toInt()
      if (rightString != null) {
        if (rightString.isNotEmpty()) {
          try{
            val rightImg = currentImageList.first { it.index == rightIndex }
            _uiState.update { state ->
              state.copy(
                rightOverlayImage = rightImg.image
              )
            }
          }catch ( e: NoSuchElementException){
            _uiState.update { state ->
              state.copy(
                rightOverlayImage = null
              )
            }
            Log.d("overlay_image",e.toString())
          }

        } else {
          _uiState.update { state ->
            state.copy(
              rightOverlayImage = null
            )
          }
        }
      }
    } catch ( e: ClassCastException){
      Log.d("state_livedata","rightoverlay excption")
      _uiState.update { state ->
        state.copy(
          rightOverlayImage = null
        )
      }
    }

    try{
//      val topMap = currentData["top"] as Map<String, String>
      //
      var topValue = currentData["top"]
      var topMap: Map<String, String>
      if(topValue!= null) {
        topMap = currentData["top"] as Map<String, String>
      }
      else topMap = mapOf()
      ////
      val topString = topMap["index"]
      val topIndex = topString?.toInt()
      if (topString != null) {
        if (topString.isNotEmpty()) {
          try{
            val topImg = currentImageList.first { it.index == topIndex }
            _uiState.update { state ->
              state.copy(
                topOverlayImage = topImg.image
              )
            }
            Log.d("overlay_image_top",topMap.toString())

          } catch ( e: NoSuchElementException){
            _uiState.update { state ->
              state.copy(
                topOverlayImage = null
              )
            }
          }
        } else {
          _uiState.update { state ->
            state.copy(
              topOverlayImage = null
            )
          }
        }
      }
    } catch ( e: ClassCastException){
      Log.d("overlay_image_top","topoverlay excption")
      _uiState.update { state ->
        state.copy(
          topOverlayImage = null
        )
      }
    }

  }

  fun leftArrowClicked(context: Context){
    val map = hashMapOf<String,Any>("direction" to "left", "state" to currentData )
    broadcast(context,map,"DirectionEvent")
  }

  fun rightArrowClicked(context: Context){
    val map = hashMapOf<String,Any>("direction" to "right", "state" to currentData )
    broadcast(context,map,"DirectionEvent")
  }

  fun bottomArrowClicked(context: Context){
    val map = hashMapOf<String,Any>("direction" to "down", "state" to currentData )
    broadcast(context,map,"DirectionEvent")
  }

  fun showGuide(): String?{
    val previewList = currentData["previewList"] as ArrayList<String>
    val rowDouble = currentData["row"] as Double
    val row = rowDouble.toInt()
    val direction = currentData["direction"] as String
    val isAutomatic = currentData["isAutomatic"] as Boolean

    //TODO: null condition return

    val result = if (previewList.size % row == 0) direction else "down"

    return if(isAutomatic) result
    else null
  }

  fun dataFromDispatchEvent(stateJsonString: String){
    Log.d("Data_to_kotlin__", currentData.toString() )
    Log.d("Data_to_kotlin", JSONObject(stateJsonString).toMap().toString() )

    updateUiStateMap( stateJsonString)
  }

  fun handleClickedImage(image: ImageProxy, context: Context){
    val bm = image.convertImageProxyToBitmap()
    val baos = ByteArrayOutputStream()
    bm.compress(Bitmap.CompressFormat.PNG, 100, baos)
    val bArray = baos.toByteArray()
    val base64Img = Base64.encodeToString(bArray, Base64.DEFAULT)
    Log.d("image_base64", base64Img)

    updateSendingData(base64Img,context)

    currentImageList.add(CaptureImageModel(bm,currentImageList.size))
    imageCapturedList.value = currentImageList
    Log.d("imgList1: ",currentImageList.size.toString())
  }

  fun updateSendingData(base64_img: String, context: Context){
    sendingData["state"] = currentData
    sendingData["image_base64"] = base64_img
    broadcast(context, sendingData,"JS-Event")

  }

  fun broadcast(
    context: Context, data: HashMap<String,Any>, eventName: String
  ){
    val customEvent = Intent("my-custom-event")

//    customEvent.putExtra("state", defaultData)
//    customEvent.putExtra("image_base64", "abcd")
    customEvent.putExtra("dataMap", data)
    customEvent.putExtra("event_name", eventName)
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

  fun JSONObject.toMap(): Map<String, *> = keys().asSequence().associateWith {
    when (val value = this[it])
    {
      is JSONArray ->
      {
        val map = (0 until value.length()).associate { Pair(it.toString(), value[it]) }
        JSONObject(map).toMap().values.toList()
      }
      is JSONObject -> value.toMap()
      JSONObject.NULL -> null
      else            -> value
    }
  }
}

data class CameraUiState(
  val rightOverlayImage: Bitmap? = null,
  val leftOverlayImage : Bitmap? = null,
  val topOverlayImage : Bitmap? = null,
  val showArrowAllDirection: Boolean = false,
  val showLeftArrow: Boolean = false,
  val showDownArrow: Boolean = false,
  val showRightArrow: Boolean = false,
  val leftGuide: Boolean = false,
  val downGuide: Boolean = false,
  val rightGuide: Boolean = false
)
