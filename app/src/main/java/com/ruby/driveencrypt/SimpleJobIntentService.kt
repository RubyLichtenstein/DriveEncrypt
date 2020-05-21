package com.ruby.driveencrypt

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ruby.driveencrypt.files.RemoteFilesManager
import kotlin.random.Random


/**
 * Example implementation of a JobIntentService.
 */
class SimpleJobIntentService : JobIntentService() {

    private var remoteFilesManager: RemoteFilesManager? = null

    override fun onCreate() {
        super.onCreate()
        remoteFilesManager = RemoteFilesManager.create(this)
    }

    override fun onHandleWork(intent: Intent) {
        val jobType = intent.getSerializableExtra(KEY_JOB_TYPE) as JobType
        when (jobType) {
            JobType.UPLOAD -> upload()
            JobType.DOWNLOAD -> download()
        }
    }

    private fun download() {
        remoteFilesManager
            ?.downloadNotSyncFiles()
            ?.addOnSuccessListener {
                val notificationId = Random.nextInt()
                val uploadCount = it.size
                var currentUploadCount = it.size
                downloadNotification(uploadCount, currentUploadCount, notificationId)

                it.onEach {
                    it.addOnSuccessListener {
                        currentUploadCount -= 1
                        downloadNotification(uploadCount, currentUploadCount, notificationId)
//                        viewModel.showAllLocalFiles(this)
                    }
                }
            }
    }

    private fun upload() {
        remoteFilesManager
            ?.uploadNotSyncFiles()
            ?.addOnSuccessListener {
                val notificationId = Random.nextInt()
                val uploadCount = it.size
                var currentUploadCount = it.size
                uploadNotification(uploadCount, currentUploadCount, notificationId)

                it.forEachIndexed { index, task ->
                    task?.addOnSuccessListener {
                        currentUploadCount -= 1
                        uploadNotification(uploadCount, currentUploadCount, notificationId)
                    }
                }
            }
    }

    private fun uploadNotification(
        uploadCount: Int,
        currentUploadCount: Int,
        notificationId: Int
    ) {
        val progressCurrent = uploadCount - currentUploadCount
        progressNotification(
            "upload",
            "$uploadCount / $progressCurrent",
            uploadCount,
            progressCurrent,
            notificationId
        )
    }

    private fun downloadNotification(
        uploadCount: Int,
        currentUploadCount: Int,
        notificationId: Int
    ) {
        val progressCurrent = uploadCount - currentUploadCount
        progressNotification(
            "download",
            "$uploadCount / $progressCurrent",
            uploadCount,
            progressCurrent,
            notificationId
        )
    }

    private fun progressNotification(
        contentTitle: String,
        contentText: String,
        progressMax: Int,
        progressCurrent: Int,
        notificationId: Int
    ) {
        val builder = NotificationCompat.Builder(
            this,
            "CHANNEL_ID" // todo
        ).apply {
            setContentTitle(contentTitle)
            setContentText(contentText)
            setSmallIcon(R.drawable.ic_cloud_done_black_24dp)
            priority = NotificationCompat.PRIORITY_LOW
        }

        NotificationManagerCompat.from(this).apply {
            // Issue the initial notification with zero progress
            builder.setProgress(progressMax, progressCurrent, false)
            notify(notificationId, builder.build())

            // Do the job here that tracks the progress.
            // Usually, this should be in a
            // worker thread
            // To show progress, update PROGRESS_CURRENT and update the notification with:
            // builder.setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false);
            // notificationManager.notify(notificationId, builder.build());

            // When done, update the notification one more time to remove the progress bar
            builder
                .setContentText(contentText)
                .setProgress(0, 0, false)

            notify(notificationId, builder.build())
        }
    }

    private fun uploadNotification(uploadCount: Int, currentUploadCount: Int) {
        val builder = NotificationCompat.Builder(
            this,
            "CHANNEL_ID"
        )
            .setSmallIcon(R.drawable.exo_icon_play)
            .setContentTitle("uploading files")
            .setContentText("textContent")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define
            notify(0, builder.build())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {

        enum class JobType {
            UPLOAD,
            DOWNLOAD
        }

        const val JOB_ID = 1000
        const val KEY_JOB_TYPE = "JOB_TYPE"

        fun upload(context: Context) {
            val mIntent = Intent(context, SimpleJobIntentService::class.java)
                .apply {
                    putExtra(KEY_JOB_TYPE, JobType.UPLOAD)
                }

            enqueueWork(context, mIntent)
        }

        fun download(context: Context) {
            val mIntent = Intent(context, SimpleJobIntentService::class.java)
                .apply {
                    putExtra(KEY_JOB_TYPE, JobType.DOWNLOAD)
                }

            enqueueWork(context, mIntent)
        }

        private fun enqueueWork(context: Context, work: Intent) {
            enqueueWork(
                context,
                SimpleJobIntentService::class.java,
                JOB_ID,
                work
            )
        }
    }
}