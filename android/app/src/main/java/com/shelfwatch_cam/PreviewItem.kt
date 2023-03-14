package com.shelfwatch_cam

import com.shelfwatch_cam.databinding.ItemPreviewImgBinding
import com.xwray.groupie.databinding.BindableItem

class PreviewItem(
  val img:Int,
  val onClick: (img: Int) -> Unit
): BindableItem<ItemPreviewImgBinding>() {

  override fun bind(viewBinding: ItemPreviewImgBinding, position: Int) {
    viewBinding.previewIv.setImageResource(img)
    viewBinding.root.setOnClickListener { onClick(img) }
  }

  override fun getLayout(): Int = R.layout.item_preview_img

}