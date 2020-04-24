package com.ruby.driveencrypt.gallery.pager

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
import android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.ruby.driveencrypt.R
import com.ruby.driveencrypt.drive.DriveService
import com.ruby.driveencrypt.files.LocalFilesManager
import com.ruby.driveencrypt.files.RemoteFilesManager
import com.ruby.driveencrypt.gallery.GalleryItem
import com.ruby.driveencrypt.gallery.GalleryViewModel
import com.ruby.driveencrypt.share.shareImage
import kotlinx.android.synthetic.main.activity_gallery_pager.*

class GalleryPagerActivity : AppCompatActivity() {

    private var driveService: DriveService? = null
    var isSystemUiShowed = true
    private val model: GalleryViewModel by viewModels()

    private fun hideSystemUI() {
        isSystemUiShowed = false
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    // Shows the system bars by removing all the flags
    // except for the ones that make the content appear under the system bars.
    private fun showSystemUI() {
        isSystemUiShowed = true

        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery_pager)
        window.addFlags(FLAG_TRANSLUCENT_STATUS or FLAG_TRANSLUCENT_NAVIGATION)

        setSupportActionBar(gallery_pager_toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = ""
        }

        val filesManager = RemoteFilesManager.create(this)
        driveService = DriveService.getDriveService(this)

        val path = intent.getStringExtra(ARG_IMAGE_PATH)

        val imagesPagerAdapter = GalleryPagerAdapter()
        setupBottomNavigation(imagesPagerAdapter)
        imagesPagerAdapter.onTap = { view, galleryItem ->
            if (isSystemUiShowed) {
                hideSystemUI()
                bottom_navigation.visibility = View.GONE
                gallery_pager_toolbar.visibility = View.GONE
            } else {
                showSystemUI()
                bottom_navigation.visibility = View.VISIBLE
                gallery_pager_toolbar.visibility = View.VISIBLE
            }
        }

//        val color = ContextCompat.getColor(this, R.color.primaryColor)
//        bottom_navigation.setBackgroundColor(color)
//        window.navigationBarColor = color

        imagesPagerAdapter.onTapVideo = { view, uri ->
            val intent = Intent(this, VideoActivity::class.java)
            intent.putExtra(VideoActivity.ARG_URI, uri)
            startActivity(intent)
        }

        image_pager.adapter = imagesPagerAdapter

        model.showAllLocalFiles(this)
        model.localFilesLiveData.observe(this, Observer {
            imagesPagerAdapter.submitList(it)
            val index = it.indexOfFirst { it.path == path }
            image_pager.setCurrentItem(index, false)
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun setupBottomNavigation(
        imagesPagerAdapter: GalleryPagerAdapter
    ) {
        val bottomNavigationView = bottom_navigation as BottomNavigationView

        if (driveService == null) {
            bottomNavigationView.menu.removeItem(R.id.upload);
        }

        bottomNavigationView.setOnNavigationItemReselectedListener {
            when (it.itemId) {
                R.id.navigation_home -> {
                    val item = getCurrentGalleryItem(imagesPagerAdapter)
                    shareImage(this, item.path)
                }

                R.id.upload -> {
//                    upload(filesManager, path)
                }

                R.id.delete -> {
                    val item = getCurrentGalleryItem(imagesPagerAdapter)
                    LocalFilesManager.deleteLocal(item.path)
                    model.showAllLocalFiles(this)
                }
            }
        }
    }

    private fun getCurrentGalleryItem(imagesPagerAdapter: GalleryPagerAdapter): GalleryItem {
        val index = image_pager.currentItem
        val item = imagesPagerAdapter.currentList[index]
        return item
    }

    private fun upload(remoteFilesManager: RemoteFilesManager, path: String) {
//        Toast
//            .makeText(this, "start upload...", Toast.LENGTH_SHORT)
//            .show()
//
//        progress.visibility = View.VISIBLE
//        filesManager
//            .uploadFile(path)
//            ?.addOnSuccessListener {
//                progress.visibility = View.GONE
//
//                Toast
//                    .makeText(this, "file uploaded", Toast.LENGTH_SHORT)
//                    .show()
//            }
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
            val intent = Intent(activity, GalleryPagerActivity::class.java)
            intent.putExtra(ARG_IMAGE_PATH, path)
            activity.startActivity(
                intent // ,
//                options.toBundle()
            )
        }
    }
}
