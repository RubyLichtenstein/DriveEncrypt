//package com.example.driveencrypt.crypto
//
//import android.content.Context
//import androidx.security.crypto.EncryptedFile
//import androidx.security.crypto.MasterKeys
//import java.io.File
//import java.io.FileInputStream
//import java.io.FileOutputStream
//
//class Cryptography {
//
//    fun decryptFile(
//        file: File,
//        toFile: File,
//        context: Context
//    ): FileOutputStream {
//        val outputStream = FileOutputStream(toFile)
//        return decryptFile(file, context).read();
//    }
//
//    fun encryptFile(
//        file: File,
//        toFile: File,
//        context: Context
//    ): FileOutputStream {
//
//        val outputBytes: ByteArray = ByteArray(0)
//        return encryptFile(file, context).use {
//            it.write(outputBytes)
//            it
//        };
//    }
//
//    fun encryptFile(
//        file: File,
//        context: Context
//    ): FileOutputStream {
//        return encryptedFile(file, context).openFileOutput().bufferedWriter().use {
//                it.newLine().
//        };
//    }
//
//    fun decryptFile(
//        file: File,
//        context: Context
//    ): FileInputStream {
//        return encryptedFile(file, context)
//            .openFileInput()
//            .bufferedReader().useLines {
//
//            }
//    }
//
//    private fun encryptedFile(
//        file: File,
//        context: Context
//    ): EncryptedFile {
//        val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
//        val masterKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)
//
//        return EncryptedFile.Builder(
//            file,
//            context,
//            masterKeyAlias,
//            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
//        ).build()
//    }
//}