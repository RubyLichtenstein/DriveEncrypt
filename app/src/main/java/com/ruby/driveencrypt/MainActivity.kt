package com.ruby.driveencrypt

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
import android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.facebook.common.util.UriUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.ruby.driveencrypt.drive.log
import com.ruby.driveencrypt.files.FilesManager
import com.ruby.driveencrypt.gallery.*
import com.ruby.driveencrypt.gallery.grid.GalleryGridFragment
import com.ruby.driveencrypt.lockscreen.LockScreenSettingsActivity
import com.ruby.driveencrypt.signin.GoogleSignInHelper
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.main_fabs.*
import pub.devrel.easypermissions.EasyPermissions

class MainActivity : AppCompatActivity() {

    private val imageGalleryHelper = MediaStorePicker()
    private lateinit var filesManager: FilesManager
    private lateinit var googleSignInHelper: GoogleSignInHelper
    private var isFabMenuVisable = false
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
        filesManager.initFolderId(this)

        fab_menu.setOnClickListener {
            imageGalleryHelper.selectImages(this)
        }

        fab_menu.setOnClickListener {
            handleFabMenuClick()
        }

        fun animateAlpha(view: View, value: Float) {
            view
                .animate()
                .alpha(value)
                .setDuration(200)
                .withEndAction {
                    view.alpha = value
                }
                .start()
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

    private fun handleFabMenuClick() {
        if (isFabMenuVisable) {
            grid_toolbar.visibility = View.VISIBLE

            main_background.visibility = View.GONE
            fabs_menu.visibility = View.GONE
        } else {
            grid_toolbar.visibility = View.GONE

            main_background.visibility = View.VISIBLE
            fabs_menu.visibility = View.VISIBLE
        }

        isFabMenuVisable = !isFabMenuVisable
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
        val id: Int = item.itemId
        if (id == R.id.password_settings) {
            val intent = Intent(this, LockScreenSettingsActivity::class.java)
            startActivity(intent)
            return true
        }

        if (id == R.id.upload_drive) {
            filesManager
                .uploadNotSyncFiles()
                .addOnSuccessListener {
                    it.onEach {
                        it?.log(
                            "TAG",
                            "uploadNotSyncFiles"
                        )?.addOnSuccessListener {
                            Toast.makeText(
                                this,
                                "uploaded: " + it.name,
                                Toast.LENGTH_LONG
                            ).show()
                            Log.d("TAG", it.toString())
                        }
                    }
                }
            return true
        }

        if (id == R.id.download_drive) {
            filesManager
                .downloadNotSyncFiles()
                .addOnSuccessListener {
                    it.onEach {
                        it.addOnSuccessListener {
                            model.showAllLocalFiles(this)
//                            Log.d("TAG", it.toString())
                        }
                    }
                }
            return true
        }

        // todo show only if drive active
        if (id == R.id.sync_with_drive) {
            model.refreshSyncedStatusAndEmit(filesManager)
//            filesManager
//                .filesSyncStatus()
//                .addOnSuccessListener {
//                    Log.d("TAG", it.toString())
//
////                    Toast
////                        .makeText(this, it.toString(), Toast.LENGTH_LONG)
////                        .show()
//                }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)

        if (resultCode == Activity.RESULT_CANCELED) return

        if (isFabMenuVisable) {
            handleFabMenuClick()
        }

        if (requestCode == googleSignInHelper.RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task =
                GoogleSignIn.getSignedInAccountFromIntent(intent)
            googleSignInHelper.handleSignInResult(this, task)
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

    override fun onBackPressed() {
        if (isFabMenuVisable) {
            handleFabMenuClick()
        } else {
            super.onBackPressed()
        }
    }

    companion object {

    }
}
