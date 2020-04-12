package com.ruby.driveencrypt.gallery.pager

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.ruby.driveencrypt.R
import kotlinx.android.synthetic.main.fragment_video.*

class VideoActivity : AppCompatActivity() {
    private lateinit var uri: Uri
    private var player: SimpleExoPlayer? = null
    private var isSystemUiShowed = true

    private fun hideSystemUI() {
        isSystemUiShowed = false
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    private fun showSystemUI() {
        isSystemUiShowed = true

        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_video)
        hideSystemUI()

        uri = intent.getParcelableExtra(ARG_URI)
        val playerView = player_view
        player = SimpleExoPlayer.Builder(this).build()

        playerView.player = player

        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
            this,
            Util.getUserAgent(this, "yourApplicationName")
        )

        val videoSource: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(uri)
        player?.prepare(videoSource)
        playerView.hideController()
        startPlayer()

        playerView.setOnClickListener {
            if (isSystemUiShowed) {
                hideSystemUI()
            } else {
                showSystemUI()
            }
        }
    }

    private fun pausePlayer() {
        player?.playWhenReady = false
    }

    private fun startPlayer() {
        player?.playWhenReady = true
    }

    companion object {
        const val ARG_URI = "param_uri"
    }

    override fun onPause() {
        super.onPause()
        pausePlayer()
    }

    override fun onResume() {
        super.onResume()
        startPlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
    }
}
