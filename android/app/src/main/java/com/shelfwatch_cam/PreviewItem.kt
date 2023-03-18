package com.shelfwatch_cam

import android.graphics.Bitmap
import com.shelfwatch_cam.databinding.ItemPreviewImgBinding
import com.xwray.groupie.databinding.BindableItem

class PreviewItem(
  val img:Int,
//  val imageBitmap: Bitmap,
  val onClick: (img: Int) -> Unit,
//  val onClick: (img: Bitmap) -> Unit
): BindableItem<ItemPreviewImgBinding>() {

  override fun bind(viewBinding: ItemPreviewImgBinding, position: Int) {
    viewBinding.previewIv.setImageResource(img)
    viewBinding.root.setOnClickListener { onClick(img) }
//    viewBinding.previewIv.setImageBitmap(imageBitmap)
//    viewBinding.root.setOnClickListener { onClick(imageBitmap) }
  }

  override fun getLayout(): Int = R.layout.item_preview_img

}