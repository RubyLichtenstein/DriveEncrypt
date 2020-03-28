package com.example.driveencrypt.gallery

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.example.driveencrypt.R
import com.example.driveencrypt.files.FilesManager
import com.example.driveencrypt.gallery.pager.ImagePagerAdapter
import com.example.driveencrypt.share.shareImage
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_image.*

class ImageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)
        val filesManager = FilesManager.create(this)

        val model: GalleryViewModel by viewModels()

        val path = intent.getStringExtra(ARG_IMAGE_PATH)

        setupBottomNavigation(path, filesManager)

        val imagesPagerAdapter = ImagePagerAdapter()
        image_pager.adapter = imagesPagerAdapter

        model.showAllLocalFiles(this)
        model.localFilesLiveData.observe(this, Observer {
            imagesPagerAdapter.addAll(it)
            val index = it.indexOf(path)
            image_pager.setCurrentItem(index, false)
        })
    }

    private fun setupBottomNavigation(
        path: String,
        filesManager: FilesManager
    ) {
        val bottomNavigationView = bottom_navigation as BottomNavigationView
        bottomNavigationView.setOnNavigationItemReselectedListener {
            when (it.itemId) {
                R.id.navigation_home -> {
                    shareImage(this, path)
                }

                R.id.upload -> {
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
        }
    }

    companion object {
        const val ARG_IMAGE_PATH = "image_path"
        const val ARG_SYNC_STATUS = "sync_status"
    }
}
