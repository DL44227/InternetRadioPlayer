package io.github.vladimirmi.radius.service


import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserServiceCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.*
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlaybackException.*
import com.google.android.exoplayer2.Player
import io.github.vladimirmi.radius.BuildConfig
import io.github.vladimirmi.radius.R
import timber.log.Timber


/**
 * Developer Vladimir Mikhalev, 09.05.2017.
 */

class PlayerService : MediaBrowserServiceCompat() {

    companion object {
        const val ACTION_PLAY = BuildConfig.APPLICATION_ID + ".ACTION_PLAY"
        const val ACTION_PAUSE = BuildConfig.APPLICATION_ID + ".ACTION_PAUSE"
        const val ACTION_STOP = BuildConfig.APPLICATION_ID + ".ACTION_STOP"

        const val EXTRA_STATION = "EXTRA_STATION"
    }

    private var stationUrl: String? = null
    private lateinit var session: MediaSessionCompat
    private lateinit var playback: Playback
    private var serviceStarted: Boolean = false

    override fun onCreate() {
        super.onCreate()

        session = MediaSessionCompat(this, javaClass.simpleName)
        sessionToken = session.sessionToken
        session.setCallback(SessionCallback())
        session.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
//        val activityIntent = Intent(applicationContext, RootActivity::class.java)
//        session.setSessionActivity(PendingIntent.getActivity(applicationContext, 0, activityIntent, 0))

        playback = Playback(this, playerCallback)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            Timber.d("onStartCommand: Stop self")
            stopSelf()
            return Service.START_STICKY
        }
        when (intent.action) {
            null -> Timber.e("onStartCommand: actions null")
            ACTION_PLAY -> {
                if (intent.hasExtra(EXTRA_STATION)) {
                    stationUrl = intent.getStringExtra(EXTRA_STATION)
                    handlePlayRequest(Uri.parse(stationUrl))
                } else {
                    handlePlayRequest()
                }
            }
            ACTION_PAUSE -> handlePauseRequest()
            ACTION_STOP -> handleStopRequest()
        }
        return Service.START_STICKY
    }

    override fun onDestroy() {
        playback.releasePlayer()
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): MediaBrowserServiceCompat.BrowserRoot? {
        return MediaBrowserServiceCompat.BrowserRoot(getString(R.string.app_name), null)
    }

    override fun onLoadChildren(parentId: String, result: MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>>) {
        result.sendResult(emptyList())
    }

    private val playerCallback = object : EmptyPlayerCallback() {

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            val state = when (playbackState) {
                Player.STATE_BUFFERING -> STATE_BUFFERING
                Player.STATE_ENDED -> STATE_PAUSED
                Player.STATE_READY -> if (playWhenReady) STATE_PLAYING else STATE_STOPPED
                else -> STATE_NONE
            }

            session.setPlaybackState(createPlaybackState(state))
        }

        override fun onPlayerError(error: ExoPlaybackException?) {
            when (error?.type) {
                TYPE_RENDERER -> Timber.e("RENDERER error occurred: ${error.rendererException}")
                TYPE_SOURCE -> Timber.e("SOURCE error occurred: ${error.sourceException}")
                TYPE_UNEXPECTED -> Timber.e("UNEXPECTED error occurred: ${error.unexpectedException}")
            }
        }

        private fun createPlaybackState(state: Int): PlaybackStateCompat {
            val availableActions = if (state == STATE_PLAYING) {
                PlaybackStateCompat.ACTION_STOP or PlaybackStateCompat.ACTION_PAUSE
            } else {
                PlaybackStateCompat.ACTION_PLAY
            }

            return Builder().setActions(availableActions)
                    .setState(state, 0, 1F)
                    .build()
        }
    }

    private fun startService() {
        if (!serviceStarted) {
            Timber.v("Starting service")
            startService(Intent(applicationContext, PlayerService::class.java))
            serviceStarted = true
            session.isActive = true
        }
    }

    private fun handlePlayRequest() {
        Timber.d("handlePlayRequest")
        playback.resume()
    }

    private fun handlePlayRequest(uri: Uri) {
        Timber.d("handlePlayRequest with url $uri")
        startService()
        playback.play(uri)
    }

    private fun handlePauseRequest() {
        Timber.d("handlePauseRequest")
        playback.pause()
    }

    private fun handleStopRequest() {
        Timber.d("handleStopRequest")
        playback.stop()
        stopSelf()
        serviceStarted = false
        session.isActive = false
    }

    inner class SessionCallback : MediaSessionCompat.Callback() {

        override fun onPlay() {
            handlePlayRequest()
        }

        override fun onPlayFromUri(uri: Uri, extras: Bundle?) {
            handlePlayRequest(uri)
        }

        override fun onPause() {
            handlePauseRequest()
        }

        override fun onStop() {
            handleStopRequest()
        }
    }
}
