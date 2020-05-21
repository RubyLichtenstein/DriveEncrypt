package com.ruby.driveencrypt

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.transition.TransitionSet
import android.transition.Visibility
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
import android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.children
import androidx.lifecycle.Observer
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.ruby.driveencrypt.files.RemoteFilesManager
import com.ruby.driveencrypt.gallery.*
import com.ruby.driveencrypt.gallery.grid.GalleryGridFragment
import com.ruby.driveencrypt.gallery.pager.GalleryPagerFragment
import com.ruby.driveencrypt.lockscreen.LockScreenSettingsActivity
import com.ruby.driveencrypt.share.shareImage
import com.ruby.driveencrypt.signin.GoogleSignInHelper
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private var remoteFilesManager: RemoteFilesManager? = null
    private lateinit var googleSignInHelper: GoogleSignInHelper
    private var isFabMenuVisable = false
    private val viewModel: GalleryViewModel by viewModels()
    private val bottomSheetFragment = BottomSheetFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.addFlags(FLAG_TRANSLUCENT_STATUS or FLAG_TRANSLUCENT_NAVIGATION)
        setSupportActionBar(bottom_app_bar)

        viewModel.permissionNeededForDelete.observe(this, Observer { intentSender ->
            intentSender?.let {
                // On Android 10+, if the app doesn't have permission to modify
                // or delete an item, it returns an `IntentSender` that we can
                // use here to prompt the user to grant permission to delete (or modify)
                // the image.
                startIntentSenderForResult(
                    intentSender,
                    DELETE_PERMISSION_REQUEST,
                    null,
                    0,
                    0,
                    0,
                    null
                )
            }
        })
//        checkStoragePermission()
        Permissions.requestPermission(this)

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
            bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
        }
    }

    var galleryPagerFragment: GalleryPagerFragment? = null

    fun openGalleryPager(view: View, path: String) {
        galleryPagerFragment = GalleryPagerFragment.newInstance(path)
        // Exclude the clicked card from the exit transition (e.g. the card will disappear immediately
        // instead of fading out with the rest to prevent an overlapping animation of fade and move).
//        (fragment.exitTransition as TransitionSet).excludeTarget(
//            view,
//            true
//        )

//        currentPosition
        galleryPagerFragment?.let {
            supportFragmentManager
                .beginTransaction()
                .setReorderingAllowed(true)
                .addSharedElement(view, view.transitionName)
                .addToBackStack(null)
                .replace(
                    R.id.main_fragment_holder,
                    it,
                    GalleryPagerFragment::class.java.simpleName
                )
                .commit()
            pagerMenuItems(true)
        }
    }

    fun pagerMenuItems(isVisible: Boolean) {
        with(bottom_app_bar.menu) {
            findItem(R.id.app_bar_account)?.isVisible = !isVisible
            findItem(R.id.pager_share)?.isVisible = isVisible
            findItem(R.id.pager_upload)?.isVisible = isVisible
            findItem(R.id.pager_delete)?.isVisible = isVisible
        }
    }

    fun showBottomAppBar() {
        bottom_app_bar.performShow()
        fab_menu.show()
    }

    fun hideBottomAppBar() {
        bottom_app_bar.performHide()
        fab_menu.hide()
    }

    private fun handleFabMenuClick() {
        if (isFabMenuVisable) {
            fab_menu.setImageResource(R.drawable.ic_add_black_24dp)
        } else {
            fab_menu.setImageResource(R.drawable.ic_clear_black_24dp)
        }

        isFabMenuVisable = !isFabMenuVisable
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        menuInflater.inflate(R.menu.bottomappbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id: Int = item.itemId

        if (id == R.id.app_bar_account) {
            UserDialog().show(supportFragmentManager, "")
        }

        if (id == R.id.pager_share) {
            galleryPagerFragment?.let {
                val galleryItem = it.getCurrentGalleryItem()
                shareImage(this, galleryItem.path)
            }
        }

        if (id == R.id.password_settings) {
            val intent = Intent(this, LockScreenSettingsActivity::class.java)
            startActivity(intent)
            return true
        }

        val remoteFilesManager = remoteFilesManager
        if (remoteFilesManager != null) {
            if (id == R.id.upload_drive) {
                SimpleJobIntentService.upload(this)
                return true
            }

            if (id == R.id.download_drive) {
                SimpleJobIntentService.download(this)
                return true
            }

            // todo show only if drive active
            if (id == R.id.sync_with_drive) {
                viewModel.refreshSyncedStatusAndEmit(remoteFilesManager)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)

        if (resultCode == Activity.RESULT_CANCELED) return

        if (isFabMenuVisable) {
            bottomSheetFragment.dismiss()
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
                    MediaStorePicker.onResultFromGallery(
                        intent
                    ) { paths ->
//                        deleteFiles(paths)
                        viewModel.handleImagePath(this, paths)
                    }
                }
            }

            if (requestCode == REQUEST_TAKE_PHOTO) {
                val currentPhotoUri = MediaCapture.currentPhotoUri
                if (currentPhotoUri != null) {
                    viewModel.handleImagePath(this, listOf(currentPhotoUri))
                    MediaCapture.currentPhotoUri = null
                }
            }

            if (requestCode == REQUEST_VIDEO_CAPTURE) {
                val videoUri: Uri = intent!!.data!!
                viewModel.handleImagePath(this, listOf(videoUri))
            }
        }
    }

    private fun deleteFiles(paths: List<Uri>) {
        paths.forEach { uri ->
            GlobalScope.launch {
                val res = MediaStorePicker.queryImages(contentResolver, uri)
                res.forEach {
                    viewModel.performDeleteImage(
                        contentResolver,
                        it
                    )
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            READ_EXTERNAL_STORAGE_REQUEST -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // showImages()
                    // todo
                } else {
                    // If we weren't granted the permission, check to see if we should show
                    // rationale for the permission.
                    val showRationale =
                        ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        )

                    /**
                     * If we should show the rationale for requesting storage permission, then
                     * we'll show [ActivityMainBinding.permissionRationaleView] which does this.
                     *
                     * If `showRationale` is false, this means the user has not only denied
                     * the permission, but they've clicked "Don't ask again". In this case
                     * we send the user to the settings page for the app so they can grant
                     * the permission (Yay!) or uninstall the app.
                     */
                    if (showRationale) {
//                        showNoAccess()
                    } else {
                        goToSettings()
                    }
                }
                return
            }
        }
    }

    private fun goToSettings() {
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:$packageName")
        ).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }.also { intent ->
            startActivity(intent)
        }
    }

    companion object {
        var currentPosition = 0
    }
}
