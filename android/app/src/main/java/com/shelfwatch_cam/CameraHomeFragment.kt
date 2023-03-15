package com.shelfwatch_cam

import android.Manifest
import android.content.ContentValues
import android.content.Intent
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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import com.shelfwatch_cam.databinding.FragmentCameraHomeBinding
import com.shelfwatch_cam.utils.loadImage
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class CameraHomeFragment : Fragment() {

  private var viewBinding: FragmentCameraHomeBinding? = null
  val viewModel : CameraViewModel by activityViewModels()
  private var imageCapture: ImageCapture? = null
  private lateinit var cameraExecutor: ExecutorService
  val imageCapturedList = mutableListOf<String>()

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {

    viewBinding = FragmentCameraHomeBinding.inflate(inflater, container, false)
    return viewBinding?.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)


    lifecycleScope.launch {
        viewModel.uiState.collect {

          if(it.left.isNotBlank()){
            viewBinding?.capturedImgLeft?.visibility = View.VISIBLE
            viewBinding?.capturedImgLeft?.loadImage(it.left)
          }
          else viewBinding?.capturedImgLeft?.visibility = View.INVISIBLE

          if(it.right.isNotBlank()){
            viewBinding?.capturedImgRight?.visibility = View.VISIBLE
            viewBinding?.capturedImgRight?.loadImage(it.right)
          }
          else viewBinding?.capturedImgRight?.visibility = View.INVISIBLE

          if(it.top.isNotBlank()){
            viewBinding?.capturedImgTop?.visibility = View.VISIBLE
            viewBinding?.capturedImgTop?.loadImage(it.top)
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
      context?.let { it -> viewModel.broadcast(it) }

//      findNavController().navigate(R.id.action_cameraHomeFragment_to_previewFragment)
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
//          viewBinding?.capturedImg?.setImageBitmap(image.convertImageProxyToBitmap())
//          val bm = image.convertImageProxyToBitmap()
//          val baos = ByteArrayOutputStream()
//          bm.compress(Bitmap.CompressFormat.PNG, 100, baos)
//          val bArray = baos.toByteArray()
//          val base64Img = encodeToString(bArray, DEFAULT)
//          imageCapturedList.add(base64Img)
          viewModel.handleClickedImage(image)
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
      val preview = Preview.Builder()
        .build()
        .also {
          it.setSurfaceProvider(viewBinding?.viewFinder?.surfaceProvider)
        }

      imageCapture = ImageCapture.Builder()
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

  override fun onDestroy() {
    super.onDestroy()
    cameraExecutor.shutdown()
  }

  companion object {
    private const val TAG = "CameraXApp"
    private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    private const val REQUEST_CODE_PERMISSIONS = 10
    val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
  }

  fun ImageProxy.convertImageProxyToBitmap(): Bitmap {
    val buffer = planes[0].buffer
    buffer.rewind()
    val bytes = ByteArray(buffer.capacity())
    buffer.get(bytes)
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
  }
}

