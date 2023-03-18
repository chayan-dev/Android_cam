package com.shelfwatch_cam

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64.DEFAULT
import android.util.Base64.encodeToString
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.shelfwatch_cam.databinding.FragmentCameraHomeBinding
import com.shelfwatch_cam.utils.loadImage
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.log


class CameraHomeFragment : Fragment() {

  private var viewBinding: FragmentCameraHomeBinding? = null
  val viewModel : CameraViewModel by activityViewModels()
  private var imageCapture: ImageCapture? = null
  private lateinit var cameraExecutor: ExecutorService
  val imageCapturedList = mutableListOf<String>()
  private lateinit var mLocalBroadcastReceiver: LocalBroadcastReceiver

  init {
    mLocalBroadcastReceiver = LocalBroadcastReceiver()

  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {

    viewBinding = FragmentCameraHomeBinding.inflate(inflater, container, false)
    return viewBinding?.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val localBroadcastManager = LocalBroadcastManager.getInstance(requireContext())
    localBroadcastManager.registerReceiver(
      mLocalBroadcastReceiver,
      IntentFilter("dispatch-event")
    )

    updateGuidelineConstraints(0.2.toFloat())


    lifecycleScope.launch {
        viewModel.uiState.collect {

          Log.d("state_livedata_fragment:", "leftArrow: ${it.showLeftArrow}, rightArraow: ${it.showRightArrow}, downArrow: ${it.showDownArrow}, All: ${it.showArrowAllDirection}")



          //arrows
//          if( it.showLeftArrow ){
//            viewBinding?.leftArrowIv?.visibility = View.VISIBLE
//            viewBinding?.leftArrowIv?.isEnabled = false
//            viewBinding?.leftArrowIv?.setImageResource(R.drawable.img_9)
//          }else viewBinding?.leftArrowIv?.visibility = View.INVISIBLE
//
//          if( it.showRightArrow || it.showArrowAllDirection){
//            viewBinding?.rightArrowIv?.visibility = View.VISIBLE
//          }else viewBinding?.rightArrowIv?.visibility = View.INVISIBLE
//
//          if( it.showDownArrow || it.showArrowAllDirection){
//            viewBinding?.downArrowIv?.visibility = View.VISIBLE
//          }else viewBinding?.downArrowIv?.visibility = View.INVISIBLE

          if(it.showArrowAllDirection){
            //make them visible
            viewBinding?.let { binding ->
              binding.leftArrowIv.visibility = View.VISIBLE
              binding.downArrowIv.visibility = View.VISIBLE
              binding.rightArrowIv.visibility = View.VISIBLE
            }
          }
          else{
            //make it direction img invisible
//            viewBinding?.let { binding ->
//              binding.leftArrowIv.visibility = View.INVISIBLE
//              binding.downArrowIv.visibility = View.INVISIBLE
//              binding.rightArrowIv.visibility = View.INVISIBLE
//            }

            if( it.showLeftArrow ){
              viewBinding?.leftArrowIv?.visibility = View.VISIBLE
              viewBinding?.leftArrowIv?.isEnabled = false
              viewBinding?.leftArrowIv?.setImageResource(R.drawable.img_9)
            }else viewBinding?.leftArrowIv?.visibility = View.INVISIBLE

            if( it.showRightArrow ){
              viewBinding?.rightArrowIv?.visibility = View.VISIBLE
              viewBinding?.rightArrowIv?.isEnabled = false
              viewBinding?.rightArrowIv?.setImageResource(R.drawable.img_10)
            }else viewBinding?.rightArrowIv?.visibility = View.INVISIBLE

            if( it.showDownArrow ){
              viewBinding?.downArrowIv?.visibility = View.VISIBLE
              viewBinding?.downArrowIv?.isEnabled = false
              viewBinding?.downArrowIv?.setImageResource(R.drawable.img_11)
            }else viewBinding?.downArrowIv?.visibility = View.INVISIBLE
          }

          //images
          if(it.leftOverlayImage != null){
            viewBinding?.capturedImgLeft?.visibility = View.VISIBLE
            viewBinding?.capturedImgLeft?.setImageBitmap(it.leftOverlayImage)
          }
          else viewBinding?.capturedImgLeft?.visibility = View.INVISIBLE

          if(it.rightOverlayImage != null){
            viewBinding?.capturedImgRight?.visibility = View.VISIBLE
            viewBinding?.capturedImgRight?.setImageBitmap(it.rightOverlayImage)
          }
          else viewBinding?.capturedImgRight?.visibility = View.INVISIBLE

          if(it.topOverlayImage != null){
            Log.d("top overlay image","exist")
            viewBinding?.capturedImgTop?.visibility = View.VISIBLE
            viewBinding?.capturedImgTop?.setImageBitmap(it.topOverlayImage)
          }
          else viewBinding?.capturedImgTop?.visibility = View.INVISIBLE
        }
    }

    // Request camera permissions
    if (allPermissionsGranted()) {
      startCamera()
    } else {
      requestPermissions(
        REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
      )
    }

    // Set up the listeners for take photo and video capture buttons
    viewBinding?.let {
      it.imageCaptureButton.setOnClickListener {
        takePhoto()
        Log.d("btn", "clicked1")
//      EmitterObj.emitEventAndIncrement()

//        val customEvent = Intent("my-custom-event")
//        customEvent.putExtra("my-extra-data", "that's it")
//        context?.let { it1 -> LocalBroadcastManager.getInstance(it1) }?.sendBroadcast(customEvent)

        Log.d("btn", "clicked2")
      }

    }

    viewBinding?.previewBtn?.setOnClickListener {
//      context?.let { it -> viewModel.broadcast(it) }

      findNavController().navigate(R.id.action_cameraHomeFragment_to_previewFragment)
    }

    viewBinding?.leftArrowIv?.setOnClickListener {
      context?.let { it1 -> viewModel.leftArrowClicked(it1) }
    }

    viewBinding?.rightArrowIv?.setOnClickListener {
      context?.let { it1 -> viewModel.rightArrowClicked(it1) }
    }

    viewBinding?.downArrowIv?.setOnClickListener {
      context?.let { it1 -> viewModel.bottomArrowClicked(it1) }
    }

    cameraExecutor = Executors.newSingleThreadExecutor()
  }

  private fun takePhoto() {
    // Get a stable reference of the modifiable image capture use case
    val imageCapture = imageCapture ?: return

    // Create time stamped name and MediaStore entry.
    val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
      .format(System.currentTimeMillis())
    val contentValues = ContentValues().apply {
      put(MediaStore.MediaColumns.DISPLAY_NAME, name)
      put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
      if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
      }
    }

    // Create output options object which contains file + metadata
    val outputOptions = activity?.let {
      ImageCapture.OutputFileOptions
        .Builder(
          it.contentResolver,
          MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
          contentValues
        )
        .build()
    }

    // Set up image capture listener, which is triggered after photo has
    // been taken
//    if (outputOptions != null) {
//      imageCapture.takePicture(
//        outputOptions,
//        ContextCompat.getMainExecutor(requireContext()),
//        object : ImageCapture.OnImageSavedCallback {
//          override fun onError(exc: ImageCaptureException) {
//            Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
//          }
//
//          override fun onImageSaved(output: ImageCapture.OutputFileResults) {
//            val msg = "Photo capture succeeded: ${output.savedUri}"
//            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
//            Log.d(TAG, msg)
//          }
//        }
//      )
//    }

    imageCapture.takePicture(
      ContextCompat.getMainExecutor(requireContext()),
      object : ImageCapture.OnImageCapturedCallback() {

        override fun onCaptureSuccess(image: ImageProxy) {
          super.onCaptureSuccess(image)
          Log.d("format", "${image.format}")
          Log.d("image: ", image.toString())
          //call vm method that will convert ot base64 and save it in a live data variable
          // let's suppose we have to send event on clicking btn, clik btn in frag that will call a vm fun that will broadcast the live data

//          viewBinding?.capturedImgLeft?.setImageBitmap(image.convertImageProxyToBitmap())

//          val bm = image.convertImageProxyToBitmap()
//          val baos = ByteArrayOutputStream()
//          bm.compress(Bitmap.CompressFormat.PNG, 100, baos)
//          val bArray = baos.toByteArray()
//          val base64Img = encodeToString(bArray, DEFAULT)
//          imageCapturedList.add(base64Img)
          context?.let { viewModel.handleClickedImage(image, it) }
//          Log.d("imgList2: ",imageCapturedList.size.toString())
          image.close()
        }

        override fun onError(exception: ImageCaptureException) {
          super.onError(exception)
          Log.e(TAG, "Photo capture failed: ${exception.message}", exception)
        }
      }
    )
  }

  private fun startCamera() {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

    cameraProviderFuture.addListener({
      // Used to bind the lifecycle of cameras to the lifecycle owner
      val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

      // Preview
      val preview = Preview.Builder().setTargetAspectRatio(AspectRatio.RATIO_16_9)
        .build()
        .also {
          it.setSurfaceProvider(viewBinding?.viewFinder?.surfaceProvider)
        }

      imageCapture = ImageCapture.Builder().setTargetAspectRatio(AspectRatio.RATIO_16_9)
        .build()

      // Select back camera as a default
      val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

      try {
        // Unbind use cases before rebinding
        cameraProvider.unbindAll()

        // Bind use cases to camera
        cameraProvider.bindToLifecycle(
          this, cameraSelector, preview, imageCapture
        )

      } catch (exc: Exception) {
        Log.e(TAG, "Use case binding failed", exc)
      }

    }, ContextCompat.getMainExecutor(requireContext()))
  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (requestCode == REQUEST_CODE_PERMISSIONS) {
      if (allPermissionsGranted()) {
        startCamera()
      } else {
        Toast.makeText(
          requireContext(),
          "Permissions not granted by the user.",
          Toast.LENGTH_SHORT
        ).show()
        activity?.finish()
      }
    }
  }

  private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
    ContextCompat.checkSelfPermission(
      requireContext(), it
    ) == PackageManager.PERMISSION_GRANTED
  }

  private fun updateGuidelineConstraints(percent: Float){
    val constParam = viewBinding?.leftGuideline?.layoutParams as ConstraintLayout.LayoutParams
    constParam.guidePercent = percent
    viewBinding?.leftGuideline?.layoutParams = constParam
  }

  override fun onDestroy() {
    super.onDestroy()
    cameraExecutor.shutdown()
  }

  companion object {
    private const val TAG = "CameraXApp"
    private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    private const val REQUEST_CODE_PERMISSIONS = 10
    val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
  }

  fun ImageProxy.convertImageProxyToBitmap(): Bitmap {
    val buffer = planes[0].buffer
    buffer.rewind()
    val bytes = ByteArray(buffer.capacity())
    buffer.get(bytes)
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
  }

  inner class LocalBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent) {
      val stateMap = intent.getStringExtra("dataMap")
//      Log.d("cam_mod: ", stateMap.toString())
//      val gson = Gson()
//      val response = gson.toJson(stateMap)
//      val dataMap = MapUtil.convertJsonToMap(JSONObject(response))
      if (stateMap != null) {
        viewModel.dataFromDispatchEvent(stateMap)
      }
//      Log.d("cam_mod1: ", someData?.size.toString())
//      val param = Arguments.createMap()
//      param.putMap(someData)
//      rContext.getJSModule(RCTDeviceEventEmitter::class.java)
//        .emit("JS-Event", dataMap)
    }
  }
}

