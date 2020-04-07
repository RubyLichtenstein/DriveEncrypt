package com.ruby.driveencrypt.gallery.view

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
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
import com.ruby.driveencrypt.gallery.ImageGalleryHelper
import com.ruby.driveencrypt.gallery.pager.ImageActivity
import com.ruby.driveencrypt.signin.GoogleSignInHelper
import kotlinx.android.synthetic.main.activity_gallery.*
import pub.devrel.easypermissions.EasyPermissions
import java.io.File


class GalleryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView

    private val imageGalleryHelper = ImageGalleryHelper()
    private lateinit var viewAdapter: GalleryAdapter
    lateinit var filesManager: FilesManager

    private val SPAN_COUNT = 3
    private var mResizeOptions: ResizeOptions? = null
    private lateinit var googleSignInHelper: GoogleSignInHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        checkStoragePermission()

        googleSignInHelper = GoogleSignInHelper(this)
        viewAdapter = GalleryAdapter()
        filesManager = FilesManager.create(this)

        val model: GalleryViewModel by viewModels()
        setupViewModel(model)

        viewAdapter.onClick = { view, item ->
            ImageActivity.startWithTransition(
                this,
                item.path,
                view
            )
        }

        val gridLayoutManager = GridLayoutManager(this@GalleryActivity, SPAN_COUNT)

        recyclerView = my_recycler_view.apply {
            setHasFixedSize(true)
            layoutManager = gridLayoutManager
            adapter = viewAdapter
        }

        recyclerView.addOnLayoutChangeListener { view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            val imageSize = (right - left) / SPAN_COUNT
            mResizeOptions = ResizeOptions(imageSize, imageSize)
            viewAdapter.mResizeOptions = mResizeOptions
        }

//        filesManager.initFolderId(this)

        pick_file.setOnClickListener {
            imageGalleryHelper.selectImage(this)
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
                viewAdapter.add(it)
            }
        })

        model.addFileLiveData.observe(this, Observer {
            viewAdapter.add(it)
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
        viewAdapter.add(picturePath)
        empty_state.visibility = View.GONE
        val file = File(picturePath)
        LocalFilesManager.saveToLocalFiles(this, file)
        viewAdapter.add(picturePath)
        val delete = file.delete()
        MediaScannerConnection.scanFile(
            this,
            arrayOf(Environment.getExternalStorageDirectory().toString()),
            null
        ) { path, uri ->
//            Log.i("ExternalStorage", "Scanned $path:")
//            Log.i("ExternalStorage", "-> uri=$uri")
        }

//        sendBroadcast(
//            Intent(
//                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
//                Uri.fromFile(File(picturePath))
//            )
//        )
        filesManager.uploadFile(picturePath)?.addOnSuccessListener { }
    }

    companion object {

    }
}
