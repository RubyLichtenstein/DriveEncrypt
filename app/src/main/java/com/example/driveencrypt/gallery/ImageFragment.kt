package com.example.driveencrypt.gallery

import android.graphics.Bitmap
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide

import com.example.driveencrypt.R
import kotlinx.android.synthetic.main.fragment_image.*

private const val ARG_IMAGE_BITMAP = "image_bitmap"

class ImageFragment : Fragment() {
    lateinit var bitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            bitmap = it.getParcelable(ARG_IMAGE_BITMAP)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_image, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Glide
            .with(image)
            .load(bitmap)
            .into(image)
    }

    companion object {
        @JvmStatic
        fun newInstance(bitmap: Bitmap) =
            ImageFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_IMAGE_BITMAP, bitmap)
                }
            }
    }
}
