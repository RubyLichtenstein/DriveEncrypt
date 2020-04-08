package com.ruby.driveencrypt.gallery.grid

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.WindowManager.LayoutParams.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StableIdKeyProvider
import androidx.recyclerview.selection.StorageStrategy
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

        val selectionTracker = SelectionTracker.Builder(
            "mySelection",
            recyclerView,
            StableIdKeyProvider(recyclerView),
            MyItemDetailsLookup(recyclerView),
            StorageStrategy.createLongStorage()
        ).withSelectionPredicate(
            SelectionPredicates.createSelectAnything()
        ).build()

        selectionTracker.addObserver(
            object : SelectionTracker.SelectionObserver<Long>() {
                override fun onSelectionChanged() {
                    super.onSelectionChanged()
                    val isItemsSelected = !selectionTracker.selection.isEmpty

                    multiselect_toolbar.visibility = if (isItemsSelected) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }

                    grid_toolbar.visibility = if (isItemsSelected) {
                        View.GONE
                    } else {
                        View.VISIBLE
                    }
                }
            })

        viewGridAdapter.tracker = selectionTracker

        pick_file.setOnClickListener {
            imageGalleryHelper.selectImages(this)
        }

        pick_videos.setOnClickListener {
            imageGalleryHelper.selectVideos(this)
        }

        delete_selected.setOnClickListener {
            val data = viewGridAdapter.data

            selectionTracker
                .selection
                .forEach { selected ->
                    val item = data.find { it.hashCode().toLong() == selected }!! // todo
                    filesManager.deleteLocal(item.path)
                }

            selectionTracker.clearSelection()
            model.showAllLocalFiles(this)
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

            viewGridAdapter.addAll(it)
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
