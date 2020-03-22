package com.example.driveencrypt

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.driveencrypt.crypto.CryptoUtils
import com.example.driveencrypt.drive.DriveService
import com.example.driveencrypt.drive.log
import com.example.driveencrypt.gallery.ImageGalleryHelper
import kotlinx.android.synthetic.main.activity_gallery_1.*
import java.io.File
import java.lang.Exception

class GalleryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView

    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var driveService: DriveService
    private val imageGalleryHelper = ImageGalleryHelper()
    val folderName = "encrypt"
    private lateinit var viewAdapter: MyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery_1)

        viewManager = LinearLayoutManager(this)
        viewAdapter = MyAdapter()
        recyclerView = my_recycler_view.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        driveService = DriveService.getDriveService(this)!!

        driveService
            .files("name = '$folderName'") {
                if (it.isEmpty()) {
                    driveService
                        .createFolder(folderName)
                        .addOnCompleteListener {
                            folderId = it.result?.id
                        }
                } else {
                    folderId = it.first().id
                }
            }
            .log("TAG", "files \"name=$folderName\"")

        downloadImages(viewAdapter)

        pick_file.setOnClickListener {
            imageGalleryHelper.selectImage(this)
        }
    }

    private fun downloadImages(viewAdapter: MyAdapter) {
        driveService
            .files("mimeType='image/jpeg'") {
                for (file in it) {
                    Log.d(
                        "TAG",
                        "Found file: ${file.name} ${file.id}"
                    )
                    driveService
                        .downloadAndDecrypt(this, file.id) {
                            Log.d("TAG", it.absolutePath)
                            viewAdapter.data.add(it.absolutePath)
                            viewAdapter.notifyDataSetChanged()
                        }
                }
            }
            .addOnCompleteListener {

            }
    }

    var folderId: String? = null

    class MyAdapter() : RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

        val data = mutableListOf<String>()

        inner class MyViewHolder(val imageView: ImageView) : RecyclerView.ViewHolder(imageView)

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): MyAdapter.MyViewHolder {
            // create a new view
            val textView = LayoutInflater.from(parent.context)
                .inflate(
                    R.layout.gallery_list_item,
                    parent,
                    false
                ) as ImageView
            return MyViewHolder(textView)
        }

        // Replace the contents of a view (invoked by the layout manager)
        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val myBitmap = BitmapFactory.decodeFile(data[position])
            holder.imageView.setImageBitmap(myBitmap)
        }

        // Return the size of your dataset (invoked by the layout manager)
        override fun getItemCount() = data.size
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_CANCELED) {
            when (requestCode) {
                1 -> if (resultCode == Activity.RESULT_OK && data != null) {
                    imageGalleryHelper.onResultFromGallery(data, contentResolver) {
                        if (it != null) {
                            handleImagePath(it)
                        } else {
                            Log.e("TAG", "image path is null")
                        }
                    }
                }
            }
        }
    }

    private fun handleImagePath(picturePath: String) {
        progress.visibility = View.VISIBLE

        val file = File(picturePath)

        val fileEncrypted = File(
            filesDir,
            System.currentTimeMillis().toString() + "_encrypted.jpg"
        )

        try {
            CryptoUtils.encrypt(CryptoUtils.key, file, fileEncrypted)

            val folderId1 = folderId
            if (folderId1 == null) {
                Log.e("TAG", "folderId == null")
                return
            }

            driveService
                .uploadFile(fileEncrypted, folderId1)
                .addOnCompleteListener {
                    progress.visibility = View.INVISIBLE
                    downloadImages(viewAdapter)
                }

        } catch (e: Exception) {
            Log.e("TAG", e.message, e)
        }
    }
}
