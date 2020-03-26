package com.example.driveencrypt.gallery

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.FileObserver
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.driveencrypt.KeyValueStorage
import com.example.driveencrypt.R
import com.example.driveencrypt.drive.DriveService
import com.example.driveencrypt.drive.log
import com.example.driveencrypt.files.FilesManager
import com.example.driveencrypt.files.LocalFilesProvider
import kotlinx.android.synthetic.main.activity_gallery_1.*
import java.io.File

class GalleryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView

    private lateinit var driveService: DriveService
    private val imageGalleryHelper = ImageGalleryHelper()
    private val folderName = "encrypt"
    private val filesProvider = LocalFilesProvider()

    private lateinit var viewAdapter: GalleryAdapter
    private lateinit var fileObserver: FileObserver
    private lateinit var filesManager: FilesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery_1)
        driveService = DriveService.getDriveService(this)!!
        filesManager = FilesManager(driveService, filesProvider)
        viewAdapter = GalleryAdapter()
        viewAdapter.onClick = {
            val intent = Intent(this, ImageActivity::class.java)
            intent.putExtra(ImageActivity.ARG_IMAGE_PATH, it.path)
            startActivity(intent)
        }

        recyclerView = my_recycler_view.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(this@GalleryActivity, 3)
            adapter = viewAdapter
        }

        delete.setOnClickListener {
            filesProvider.deleteAllFiles(this)
        }

        refresh.setOnClickListener {
            showAllLocalFiles()
        }

        refresh_remote.setOnClickListener {
            filesManager
                .downloadFilesFromDriveAndSaveToLocal(this) {
                    Log.d("TAG", """downloaded: ${it.name}""")
                    addFileToGallery(it)
                }
        }

        sync.setOnClickListener {
            filesManager.syncFiles(this) {
                Log.d("TAG", it.toString())
            }
        }

        showAllLocalFiles()

        initFolderId()

        pick_file.setOnClickListener {
            imageGalleryHelper.selectImage(this)
        }

        fileObserver = filesProvider.observeLocal(this)
        fileObserver.startWatching()
    }

    private fun addFileToGallery(file: File) {
        viewAdapter.add(file.absolutePath)
    }

    private fun showAllLocalFiles() {
        val localFilesPaths = filesProvider.getLocalFilesPaths(this)

        viewAdapter.clear()

        localFilesPaths.forEach {
            viewAdapter.add(it)
        }
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
        val fileName = file.name

        filesProvider.saveToLocalFiles(this, file)

        val folderId1 =
            KeyValueStorage.getFolderId(this)

        if (folderId1 == null) {
            Log.e("TAG", "folderId == null")
            return
        }

        driveService
            .uploadFile(file, folderId1, fileName)
            .addOnCompleteListener {
//                progress.visibility = View.INVISIBLE
            }.log("TA", "uploadFile($file, $folderId1, $fileName)")
    }

    override fun onBackPressed() {
        if (supportFragmentManager.fragments.isNotEmpty()) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }
}
