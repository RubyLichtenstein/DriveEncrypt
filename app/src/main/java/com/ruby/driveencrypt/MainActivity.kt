package com.ruby.driveencrypt

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
import android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.facebook.common.util.UriUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.ruby.driveencrypt.files.FilesManager
import com.ruby.driveencrypt.gallery.*
import com.ruby.driveencrypt.gallery.grid.GalleryGridFragment
import com.ruby.driveencrypt.lockscreen.LockScreenSettingsActivity
import com.ruby.driveencrypt.signin.GoogleSignInHelper
import kotlinx.android.synthetic.main.activity_main.*
import pub.devrel.easypermissions.EasyPermissions
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private val imageGalleryHelper = MediaStorePicker()
    lateinit var filesManager: FilesManager
    private lateinit var googleSignInHelper: GoogleSignInHelper
    var isFabsMenuVisable = false
    private val model: GalleryViewModel by viewModels()

    private val mediaCapture = MediaCapture()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.addFlags(FLAG_TRANSLUCENT_STATUS or FLAG_TRANSLUCENT_NAVIGATION)
        setSupportActionBar(grid_toolbar)
        checkStoragePermission()

        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.main_fragment_holder,
                GalleryGridFragment()
            )
            .commit()

        googleSignInHelper = GoogleSignInHelper(this)

        filesManager = FilesManager.create(this)

        fab_menu.setOnClickListener {
            imageGalleryHelper.selectImages(this)
        }

        fab_menu.setOnClickListener {
            isFabsMenuVisable = !isFabsMenuVisable
            fabs_menu.visibility = if (isFabsMenuVisable) View.GONE else View.VISIBLE
        }

        fab_import_videos.setOnClickListener {
            imageGalleryHelper.selectVideos(this)
        }

        fab_import_photos.setOnClickListener {
            imageGalleryHelper.selectImages(this)
        }

        fab_take_photo.setOnClickListener {
            mediaCapture.dispatchTakePictureIntent(this)
        }

        fab_take_video.setOnClickListener {
            mediaCapture.dispatchTakeVideoIntent(this)
        }

        model.isInSelectionModeLiveData.observe(this, Observer {
            grid_toolbar.visibility = if (it) {
                View.GONE
            } else {
                View.VISIBLE
            }
        })

        user_dialog.setOnClickListener {
            UserDialog()
                .show(supportFragmentManager, "")
        }
    }

    private fun checkStoragePermission() {
        val galleryPermissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id: Int = item.itemId
        if (id == R.id.password_settings) {
            val intent = Intent(this, LockScreenSettingsActivity::class.java)
            startActivity(intent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)

        if (resultCode == Activity.RESULT_CANCELED) return

        if (requestCode == googleSignInHelper.RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task =
                GoogleSignIn.getSignedInAccountFromIntent(intent)
            googleSignInHelper.handleSignInResult(task)
        }


        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_PICK_IMAGES, REQUEST_PICK_VIDEO -> if (intent != null) {
                    imageGalleryHelper.onResultFromGallery(
                        intent,
                        contentResolver
                    ) { paths ->
                        model.handleImagePath(this, paths)
                    }
                }
            }

            if (requestCode == REQUEST_TAKE_PHOTO) {
                model.handleImagePath(this, listOf(mediaCapture.currentPhotoPath!!))
            }

            if (requestCode == REQUEST_VIDEO_CAPTURE) {
                val videoUri: Uri = intent!!.data!!
                val path = UriUtil.getRealPathFromUri(contentResolver, videoUri)
                model.handleImagePath(this, listOf(path!!))
            }
        }

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
