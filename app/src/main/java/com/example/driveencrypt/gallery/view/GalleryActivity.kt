package com.example.driveencrypt.gallery.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.driveencrypt.KeyValueStorage
import com.example.driveencrypt.R
import com.example.driveencrypt.drive.DriveService
import com.example.driveencrypt.files.FilesManager
import com.example.driveencrypt.files.LocalFilesManager
import com.example.driveencrypt.gallery.GalleryViewModel
import com.example.driveencrypt.gallery.ImageActivity
import com.example.driveencrypt.gallery.ImageGalleryHelper
import com.facebook.imagepipeline.common.ResizeOptions
import kotlinx.android.synthetic.main.activity_gallery.*
import java.io.File

class GalleryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView

    private lateinit var driveService: DriveService
    private val imageGalleryHelper =
        ImageGalleryHelper()
    private val folderName = "encrypt"

    private lateinit var viewAdapter: GalleryAdapter
    lateinit var filesManager: FilesManager

    private val SPAN_COUNT = 3
    private var mResizeOptions: ResizeOptions? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)
        driveService = DriveService.getDriveService(this)!!
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

        initFolderId()

        pick_file.setOnClickListener {
            imageGalleryHelper.selectImage(this)
        }

        swipe_refresh.setOnRefreshListener {
            model.refreshFiles(
                filesManager,
                this
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

    private fun initFolderId() {
        val folderId = KeyValueStorage.getFolderId(this)
        if (folderId == null) {
            driveService
                .files("name = '$folderName'")
                .addOnSuccessListener {
                    if (it.isEmpty()) {
                        driveService
                            .createFolder(folderName)
                            .addOnCompleteListener {
                                val folderId1 = it.result?.id
                                folderId1?.let { it1 ->
                                    KeyValueStorage.putFolderId(
                                        this,
                                        it1
                                    )
                                }
                            }
                    } else {
                        KeyValueStorage.putFolderId(
                            this,
                            folderId = it.first().id
                        )
                    }
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
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
