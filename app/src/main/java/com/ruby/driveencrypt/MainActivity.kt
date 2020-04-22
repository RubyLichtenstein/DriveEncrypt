package com.ruby.driveencrypt

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewAnimationUtils
import android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
import android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.facebook.common.util.UriUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.ruby.driveencrypt.drive.log
import com.ruby.driveencrypt.files.RemoteFilesManager
import com.ruby.driveencrypt.gallery.*
import com.ruby.driveencrypt.gallery.grid.GalleryGridFragment
import com.ruby.driveencrypt.lockscreen.LockScreenSettingsActivity
import com.ruby.driveencrypt.signin.GoogleSignInHelper
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.main_fabs.*
import pub.devrel.easypermissions.EasyPermissions
import kotlin.math.hypot

class MainActivity : AppCompatActivity() {

    private val imageGalleryHelper = MediaStorePicker()
    private var remoteFilesManager: RemoteFilesManager? = null
    private lateinit var googleSignInHelper: GoogleSignInHelper
    private var isFabMenuVisable = false
    private val model: GalleryViewModel by viewModels()

    private val mediaCapture = MediaCapture()

    fun createCircularReveal(
        myView: View,
        anchor: View
    ) {
        // previously invisible view
        // get the center for the clipping circle
        val anchorCx = anchor.x + (anchor.width / 2)
        val anchorCy = anchor.y + (anchor.height / 2)

        val cx = myView.width// / 2
        val cy = myView.height// / 2

        // get the final radius for the clipping circle
        val finalRadius = hypot(cx.toDouble(), cy.toDouble()).toFloat()

        // create the animator for this view (the start radius is zero)
        val anim = ViewAnimationUtils.createCircularReveal(
            myView,
            anchorCx.toInt(),
            anchorCy.toInt(),
            0f,
            finalRadius
        )
        // make the view visible and start the animation
        myView.visibility = View.VISIBLE
        anim.start()
    }

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

        remoteFilesManager = RemoteFilesManager.create(this)
        remoteFilesManager?.initFolderId(this)

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
            fab_menu.setImageResource(R.drawable.ic_add_black_24dp)
        } else {
            grid_toolbar.visibility = View.GONE

            createCircularReveal(main_background, fab_menu)
            fabs_menu.visibility = View.VISIBLE
            fab_menu.setImageResource(R.drawable.ic_clear_black_24dp)
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

        val remoteFilesManager = remoteFilesManager
        if (remoteFilesManager != null) {
            if (id == R.id.upload_drive) {
                remoteFilesManager
                    .uploadNotSyncFiles()
                    .addOnSuccessListener {
                        it.onEach {
                            it?.addOnSuccessListener {
                                Toast.makeText(
                                    this,
                                    "uploaded: " + it.name,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                return true
            }

            if (id == R.id.download_drive) {
                remoteFilesManager
                    .downloadNotSyncFiles()
                    .addOnSuccessListener {
                        it.onEach {
                            it.addOnSuccessListener {
                                model.showAllLocalFiles(this)
                            }
                        }
                    }
                return true
            }

            // todo show only if drive active
            if (id == R.id.sync_with_drive) {
                model.refreshSyncedStatusAndEmit(remoteFilesManager)
                return true
            }
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
