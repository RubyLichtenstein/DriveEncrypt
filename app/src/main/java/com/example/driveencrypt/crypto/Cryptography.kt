package com.example.driveencrypt.crypto

import android.content.Context
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKeys
import java.io.File

class Cryptography {
    fun readAFile(context: Context, directory: String, fileToRead: String): EncryptedFile {
        // Although you can define your own key generation parameter specification, it's
        // recommended that you use the value specified here.

        val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
        val masterKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)

        return EncryptedFile.Builder(
            File(directory, fileToRead),
            context,
            masterKeyAlias,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build();

//        val contents = encryptedFile.bufferedReader().useLines { lines ->
//            lines.fold("") { working, line ->
//                "$working\n$line"
//            }
//        }
    }

    fun writeAFile(context: Context, directory: String, fileToWrite: String): EncryptedFile {
        // Although you can define your own key generation parameter specification, it's
        // recommended that you use the value specified here.

        val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
        val masterKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)

        // Creates a file with this name, or replaces an existing file
        // that has the same name. Note that the file name cannot contain
        // path separators.

        return EncryptedFile.Builder(
            File(directory, fileToWrite),
            context,
            masterKeyAlias,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()

//        encryptedFile.openFileOutput().bufferedWriter().use {
//            it.write("MY SUPER-SECRET INFORMATION")
//        }
    }

    fun writeAFile(context: Context, file: File): EncryptedFile {
        // Although you can define your own key generation parameter specification, it's
        // recommended that you use the value specified here.

        val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
        val masterKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)

        // Creates a file with this name, or replaces an existing file
        // that has the same name. Note that the file name cannot contain
        // path separators.

        return EncryptedFile.Builder(
            file,
            context,
            masterKeyAlias,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()

//        encryptedFile.openFileOutput().bufferedWriter().use {
//            it.write("MY SUPER-SECRET INFORMATION")
//        }
    }
}