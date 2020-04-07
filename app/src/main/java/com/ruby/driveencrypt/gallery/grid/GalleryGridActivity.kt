package com.ruby.driveencrypt.gallery.grid

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.imagepipeline.common.ResizeOptions
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.ruby.driveencrypt.R
import com.ruby.driveencrypt.files.FilesManager
import com.ruby.driveencrypt.files.LocalFilesManager
import com.ruby.driveencrypt.gallery.GalleryViewModel
import com.ruby.driveencrypt.gallery.MediaStorePicker
import com.ruby.driveencrypt.gallery.UserDialog
import com.ruby.driveencrypt.gallery.pager.GalleryPagerActivity
import com.ruby.driveencrypt.signin.GoogleSignInHelper
import kotlinx.android.synthetic.main.activity_gallery.*
import pub.devrel.easypermissions.EasyPermissions
import java.io.File


class GalleryGridActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView

    private val imageGalleryHelper = MediaStorePicker()
    private lateinit var viewGridAdapter: GalleryGridAdapter
    lateinit var filesManager: FilesManager

    private val SPAN_COUNT = 3
    private var mResizeOptions: ResizeOptions? = null
    private lateinit var googleSignInHelper: GoogleSignInHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)
        window.addFlags(FLAG_TRANSLUCENT_STATUS or FLAG_TRANSLUCENT_NAVIGATION)

        checkStoragePermission()

        googleSignInHelper = GoogleSignInHelper(this)
        viewGridAdapter = GalleryGridAdapter()
        filesManager = FilesManager.create(this)

        val model: GalleryViewModel by viewModels()
        setupViewModel(model)

        viewGridAdapter.onClick = { view, item ->
            GalleryPagerActivity.startWithTransition(
                this,
                item.path,
                view
            )
        }

        val gridLayoutManager = GridLayoutManager(this@GalleryGridActivity, SPAN_COUNT)

        recyclerView = my_recycler_view.apply {
            setHasFixedSize(true)
            layoutManager = gridLayoutManager
            adapter = viewGridAdapter
        }

        recyclerView.addOnLayoutChangeListener { view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            val imageSize = (right - left) / SPAN_COUNT
            mResizeOptions = ResizeOptions(imageSize, imageSize)
            viewGridAdapter.mResizeOptions = mResizeOptions
        }

//        filesManager.initFolderId(this)

        pick_file.setOnClickListener {
            imageGalleryHelper.selectImages(this)
        }

        pick_videos.setOnClickListener {
            imageGalleryHelper.selectVideos(this)
        }

//        swipe_refresh.setOnRefreshListener {
//            model.refreshFiles(
//                filesManager,
//                this
//            )
//        }

        user_dialog.setOnClickListener {
            UserDialog()
                .show(supportFragmentManager, "")
        }
    }

    private fun checkStoragePermission() {
        val galleryPermissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        if (EasyPermissions.hasPermissions(this, *galleryPermissions)) {
            //            pickImageFromGallery()
        } else {
            EasyPermissions.requestPermissions(
                this,
                "Access for storage",
                101,
                *galleryPermissions
            )
        }
    }

    private fun setupViewModel(model: GalleryViewModel) {
        model.showAllLocalFiles(this)
        model.localFilesLiveData.observe(this, Observer {
            if (it.isEmpty()) {
                empty_state.visibility = View.VISIBLE
            } else {
                empty_state.visibility = View.GONE
            }

            it.forEach {
                viewGridAdapter.add(it)
            }
        })

        model.addFileLiveData.observe(this, Observer {
            viewGridAdapter.add(it)
        })

//        model.refreshLiveData.observe(this, Observer {
//            swipe_refresh.isRefreshing = it
//        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == googleSignInHelper.RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task =
                GoogleSignIn.getSignedInAccountFromIntent(data)
            googleSignInHelper.handleSignInResult(task)
        }

        if (resultCode != Activity.RESULT_CANCELED) {
            when (requestCode) {
                1 -> if (resultCode == Activity.RESULT_OK && data != null) {
                    imageGalleryHelper.onResultFromGallery(
                        data,
                        contentResolver
                    ) { paths ->
                        paths.forEach {
                            handleImagePath(it)
                        }
                    }
                }
            }
        }
    }

    private fun handleImagePath(picturePath: String) {
        empty_state.visibility = View.GONE
        val file = File(picturePath)
        LocalFilesManager.saveToLocalFiles(this, file)
//        deleteFile(file)
        viewGridAdapter.add(filesDir.path + '/' + file.name)
        filesManager.uploadFile(picturePath)?.addOnSuccessListener { }
    }

    private fun deleteFile(file: File) {
        val delete = file.delete()
        MediaScannerConnection.scanFile(
            this,
            arrayOf(Environment.getExternalStorageDirectory().toString()),
            null
        ) { path, uri ->
            //            Log.i("ExternalStorage", "Scanned $path:")
            //            Log.i("ExternalStorage", "-> uri=$uri")
        }
    }

    companion object {

    }
}
