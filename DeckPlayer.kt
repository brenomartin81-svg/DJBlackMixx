package com.djblackmix.app

import android.content.Context
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.net.Uri
import android.os.Build

class DeckPlayer(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    var isPrepared: Boolean = false
        private set

    private var cuePointMs: Int = 0
    private var currentSpeed: Float = 1.0f

    fun loadTrack(uri: Uri, onPreparedListener: () -> Unit) {
        release()
        cuePointMs = 0
        currentSpeed = 1.0f

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, uri)
                setOnPreparedListener {
                    isPrepared = true
                    onPreparedListener()
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            isPrepared = false
        }
    }

    fun play() {
        if (isPrepared && mediaPlayer?.isPlaying == false) {
            mediaPlayer?.start()
            applySpeed()
        }
    }

    fun pause() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
        }
    }

    fun triggerCue() {
        if (!isPrepared) return

        if (isPlaying()) {
            pause()
            seekTo(cuePointMs)
        } else {
            cuePointMs = getCurrentPosition()
        }
    }

    fun setPitchSpeed(speed: Float) {
        currentSpeed = speed.coerceIn(0.5f, 2.0f)
        if (isPlaying()) {
            applySpeed()
        }
    }

    private fun applySpeed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val params = mediaPlayer?.playbackParams ?: PlaybackParams()
                params.speed = currentSpeed
                mediaPlayer?.playbackParams = params
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun seekTo(positionMs: Int) {
        if (isPrepared) {
            mediaPlayer?.seekTo(positionMs)
        }
    }

    fun setVolume(leftVolume: Float, rightVolume: Float) {
        if (isPrepared) {
            mediaPlayer?.setVolume(leftVolume, rightVolume)
        }
    }

    fun getDuration(): Int = if (isPrepared) mediaPlayer?.duration ?: 0 else 0

    fun getCurrentPosition(): Int = if (isPrepared) mediaPlayer?.currentPosition ?: 0 else 0

    fun isPlaying(): Boolean = isPrepared && (mediaPlayer?.isPlaying == true)

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
        isPrepared = false
    }
}
