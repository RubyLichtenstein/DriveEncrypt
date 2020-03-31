package com.example.driveencrypt.gallery.view

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.driveencrypt.R
import com.example.driveencrypt.files.FilesManager
import com.example.driveencrypt.files.LocalFilesManager
import com.example.driveencrypt.gallery.GalleryViewModel
import com.example.driveencrypt.gallery.pager.ImageActivity
import com.example.driveencrypt.gallery.ImageGalleryHelper
import com.example.driveencrypt.signin.GoogleSignInHelper
import com.facebook.imagepipeline.common.ResizeOptions
import com.google.android.gms.auth.api.signin.GoogleSignIn
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

        viewAdapter.onClick = {
            val intent = Intent(this, ImageActivity::class.java)
            intent.putExtra(ImageActivity.ARG_IMAGE_PATH, it.path)
            startActivity(intent)
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

        swipe_refresh.setOnRefreshListener {
            model.refreshFiles(
                filesManager,
                this
            )
        }

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
            it.forEach {
                viewAdapter.add(it)
            }
        })
        model.addFileLiveData.observe(this, Observer {
            viewAdapter.add(it)
        })
        model.refreshLiveData.observe(this, Observer {
            swipe_refresh.isRefreshing = it
        })
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
        val file = File(picturePath)
        LocalFilesManager.saveToLocalFiles(this, file)
        filesManager.uploadFile(picturePath)?.addOnSuccessListener { }
    }
}
