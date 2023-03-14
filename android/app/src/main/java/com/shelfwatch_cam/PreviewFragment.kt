package com.shelfwatch_cam

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.shelfwatch_cam.databinding.FragmentPreviewBinding
import com.xwray.groupie.GroupieAdapter

class PreviewFragment : Fragment() {

  private var binding: FragmentPreviewBinding? = null

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    // Inflate the layout for this fragment
    binding = FragmentPreviewBinding.inflate(inflater, container, false)
    return binding?.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding?.exitBtn?.setOnClickListener { activity?.onBackPressed() }

    val adapter = GroupieAdapter()
    binding?.previewImgRv?.adapter = adapter

    val imgList = listOf(R.drawable.img,R.drawable.img_1,R.drawable.img_2,R.drawable.img_3,R.drawable.img_4,
      R.drawable.img_5,R.drawable.img_6,R.drawable.img_7,R.drawable.img_8).toPreviewItem()

    adapter.addAll(imgList)
  }

  private fun List<Int>.toPreviewItem(): List<PreviewItem> {
    return this.map {
      PreviewItem(
        it,
        onClick = { clickedImg->
          binding?.previewIv?.setImageResource(clickedImg)
        }
      )
    }
  }


}