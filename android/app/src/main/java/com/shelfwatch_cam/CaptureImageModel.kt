package com.shelfwatch_cam

import android.graphics.Bitmap

data class CaptureImageModel(
  val image: Bitmap,
//  val direction: String? = null,
  val index: Int
)
