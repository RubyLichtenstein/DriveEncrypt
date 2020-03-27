package com.example.driveencrypt.gallery

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.driveencrypt.R
import com.example.driveencrypt.files.FilesManager
import kotlinx.android.synthetic.main.activity_image.*
import kotlinx.android.synthetic.main.fragment_image.image

class ImageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)
        val filesManager = FilesManager.create(this)

        val path = intent.getStringExtra(ARG_IMAGE_PATH)
        val diffResult = intent?.getSerializableExtra(ARG_SYNC_STATUS) as FilesManager.SyncStatus

        Glide
            .with(image)
            .load(path)
            .into(image)

        sync_status.text = diffResult.name

        sync_button.setOnClickListener {
            progress.visibility = View.VISIBLE
            filesManager
                .uploadFile(path)
                ?.addOnSuccessListener {
                    progress.visibility = View.GONE

                    Toast
                        .makeText(this, "file uploaded", Toast.LENGTH_SHORT)
                        .show()
                }
        }
    }

    companion object {
        const val ARG_IMAGE_PATH = "image_path"
        const val ARG_SYNC_STATUS = "sync_status"
    }
}
