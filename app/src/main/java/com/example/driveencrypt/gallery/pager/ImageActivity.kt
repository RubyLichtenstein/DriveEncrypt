package com.example.driveencrypt.gallery.pager

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.driveencrypt.R
import com.example.driveencrypt.files.FilesManager
import com.example.driveencrypt.gallery.GalleryViewModel
import com.example.driveencrypt.gallery.view.GalleryActivity
import com.example.driveencrypt.share.shareImage
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_image.*

class ImageActivity : AppCompatActivity() {

    private lateinit var model: GalleryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)
        val filesManager = FilesManager.create(this)

        val model: GalleryViewModel by viewModels()
        this.model = model

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
                    upload(filesManager, path)
                }

                R.id.delete -> {
                    val fileIdToDelete = model.localFilesLiveData.value
                    filesManager.deleteLocal(path)
                }
            }
        }
    }

    private fun upload(filesManager: FilesManager, path: String) {
        Toast
            .makeText(this, "start upload...", Toast.LENGTH_SHORT)
            .show()

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

    companion object {
        const val ARG_IMAGE_PATH = "image_path"
        const val ARG_SYNC_STATUS = "sync_status"

        fun startWithTransition(
            activity: Activity,
            path: String,
            view: View
        ) {
            val options = ActivityOptions
                .makeSceneTransitionAnimation(activity, view, "image")
            val intent = Intent(activity, ImageActivity::class.java)
            intent.putExtra(ARG_IMAGE_PATH, path)
            activity.startActivity(
                intent // ,
//                options.toBundle()
            )
        }
    }
}
