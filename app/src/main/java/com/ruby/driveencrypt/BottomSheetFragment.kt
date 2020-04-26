package com.ruby.driveencrypt

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ruby.driveencrypt.gallery.MediaCapture
import com.ruby.driveencrypt.gallery.MediaStorePicker
import kotlinx.android.synthetic.main.main_fabs.*
import pub.devrel.easypermissions.EasyPermissions

class BottomSheetFragment : BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_bottom_sheet_dialog, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fab_import_videos.setOnClickListener {
            MediaStorePicker.selectVideos(requireActivity())
        }

        fab_import_photos.setOnClickListener {
            MediaStorePicker.selectImages(requireActivity())
        }

        fab_take_photo.setOnClickListener {
            checkCameraPermission {
                MediaCapture.dispatchTakePictureIntent(requireActivity())
            }
        }

        fab_take_video.setOnClickListener {
            checkCameraPermission {
                MediaCapture.dispatchTakeVideoIntent(requireActivity())
            }
        }
    }

    fun checkCameraPermission(onHasPermissions: () -> Unit) {
        val galleryPermissions = arrayOf(
            Manifest.permission.CAMERA
        )

        if (EasyPermissions.hasPermissions(
                requireActivity(),
                *galleryPermissions
            )
        ) {
            onHasPermissions()
        } else {
            EasyPermissions.requestPermissions(
                this,
                "Access for storage",
                101,
                *galleryPermissions
            )
        }
    }
}