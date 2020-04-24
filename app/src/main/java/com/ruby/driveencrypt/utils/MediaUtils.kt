package com.ruby.driveencrypt.utils

import android.webkit.MimeTypeMap
import com.google.common.collect.ImmutableMap
import java.util.*


/** Utility class.  */
object MediaUtils {
    // Additional mime types that we know to be a particular media type but which may not be
    // supported natively on the device.
    val ADDITIONAL_ALLOWED_MIME_TYPES: Map<String, String> =
        ImmutableMap.of("mkv", "video/x-matroska", "glb", "model/gltf-binary")

    fun isPhoto(mimeType: String?): Boolean {
        return mimeType != null && mimeType.startsWith("image/")
    }

    fun isVideo(mimeType: String?): Boolean {
        return mimeType != null && mimeType.startsWith("video/")
    }

    fun isThreeD(mimeType: String?): Boolean {
        return mimeType != null && mimeType == "model/gltf-binary"
    }

    fun extractMime(path: String): String? {
        var extension = extractExtension(path) ?: return null
        extension = extension.toLowerCase(Locale.US)
        var mimeType: String? = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)

        // If we did not find a mime type for the extension specified, check our additional
        // extension/mime-type mappings.
        if (mimeType == null) {
            mimeType = ADDITIONAL_ALLOWED_MIME_TYPES[extension]
        }
        return mimeType
    }

    private fun extractExtension(path: String): String? {
        val pos = path.lastIndexOf('.')
        return if (pos < 0 || pos == path.length - 1) {
            null
        } else path.substring(pos + 1)
    }

    /**
     * @return true if the mime type is one of our whitelisted mimetypes that we support beyond what
     * the native platform supports.
     */
    fun isNonNativeSupportedMimeType(mimeType: String): Boolean {
        return ADDITIONAL_ALLOWED_MIME_TYPES.containsValue(mimeType)
    }

    fun isVideoFile(path: String): Boolean {
        return MediaUtils.isVideo(MediaUtils.extractMime(path))
    }
}