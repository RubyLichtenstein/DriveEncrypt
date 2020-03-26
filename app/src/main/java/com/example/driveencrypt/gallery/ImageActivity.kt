package com.example.driveencrypt.gallery

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.example.driveencrypt.R
import kotlinx.android.synthetic.main.fragment_image.*


class ImageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)

        val path = intent.getStringExtra(ARG_IMAGE_PATH)

        Glide
            .with(image)
            .load(path)
            .into(image)
    }

    companion object {
        const val ARG_IMAGE_BITMAP = "image_bitmap"
        const val ARG_IMAGE_PATH = "image_path"
    }
}
