package com.example.driveencrypt.gallery

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.driveencrypt.FilesProvider
import com.example.driveencrypt.KeyValueStorage
import com.example.driveencrypt.R
import com.example.driveencrypt.drive.DriveService
import kotlinx.android.synthetic.main.activity_gallery_1.*
import java.io.File

class GalleryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView

    private lateinit var driveService: DriveService
    private val imageGalleryHelper = ImageGalleryHelper()
    private val folderName = "encrypt"
    private val filesProvider = FilesProvider()

    private lateinit var viewAdapter: GalleryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery_1)

        viewAdapter = GalleryAdapter()
        recyclerView = my_recycler_view.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(this@GalleryActivity, 3)
            adapter = viewAdapter
        }

        val localFilesPaths = filesProvider.getLocalFilesPaths(this)
        localFilesPaths.forEach {
            viewAdapter.data.add(it)
            viewAdapter.notifyItemInserted(viewAdapter.itemCount)
        }

        driveService = DriveService.getDriveService(this)!!

        initFolderId()
//        downloadImages(viewAdapter)

        pick_file.setOnClickListener {
            imageGalleryHelper.selectImage(this)
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

    private fun downloadImages(viewAdapter: GalleryAdapter) {
        driveService
            .files("mimeType='image/jpeg'")
            .addOnSuccessListener {
                for (file in it) {
                    driveService
                        .downloadAndDecrypt(this, file.id) {
                            viewAdapter.data.add(it.absolutePath)
                            viewAdapter.notifyDataSetChanged()
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
                    ) { path ->
                        Log.d("TAG", path)
                        handleImagePath(path)
                    }
                }
            }
        }
    }

    private fun handleImagePath(picturePath: String) {
        progress.visibility = View.VISIBLE

        viewAdapter.data.add(picturePath)
        viewAdapter.notifyItemInserted(viewAdapter.itemCount)

        val file = File(picturePath)
        val fileName = file.name;

        filesProvider.saveToLocalFiles(this, file)

//        val folderId1 =
//            KeyValueStorage.getFolderId(this)
//
//        if (folderId1 == null) {
//            Log.e("TAG", "folderId == null")
//            return
//        }
//
//        driveService
//            .uploadFile(file, folderId1, fileName)
//            .addOnCompleteListener {
//                progress.visibility = View.INVISIBLE
//            }
    }
}
