package com.example.driveencrypt.gallery

import android.graphics.Bitmap
import com.example.driveencrypt.files.FilesManager

data class GalleryItem(
    val path: String,
    val name: String,
    val src: String,
    val diffResult: FilesManager.DiffResult,
    val bitmap: Bitmap
)
