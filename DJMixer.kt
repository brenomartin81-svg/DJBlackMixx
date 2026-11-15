package com.djblackmix.app

import android.content.Context
import android.net.Uri
import kotlin.math.cos
import kotlin.math.sin

class DJMixer(context: Context) {

    val deckA = DeckPlayer(context)
    val deckB = DeckPlayer(context)

    private var volumeDeckA: Float = 1.0f
    private var volumeDeckB: Float = 1.0f
    private var crossfaderPosition: Float = 0.0f

    fun loadTrackDeckA(uri: Uri, onPrepared: () -> Unit) {
        deckA.loadTrack(uri) {
            updateAudioBalance()
            onPrepared()
        }
    }

    fun loadTrackDeckB(uri: Uri, onPrepared: () -> Unit) {
        deckB.loadTrack(uri) {
            updateAudioBalance()
            onPrepared()
        }
    }

    fun setChannelVolumeA(volume: Float) {
        volumeDeckA = volume.coerceIn(0.0f, 1.0f)
        updateAudioBalance()
    }

    fun setChannelVolumeB(volume: Float) {
        volumeDeckB = volume.coerceIn(0.0f, 1.0f)
        updateAudioBalance()
    }

    fun setCrossfader(position: Float) {
        crossfaderPosition = position.coerceIn(-1.0f, 1.0f)
        updateAudioBalance()
    }

    private fun updateAudioBalance() {
        val normalizedPos = (crossfaderPosition + 1.0f) / 2.0f
        val crossfadeGainA = cos(normalizedPos * (Math.PI / 2)).toFloat()
        val crossfadeGainB = sin(normalizedPos * (Math.PI / 2)).toFloat()

        val finalVolumeA = volumeDeckA * crossfadeGainA
        val finalVolumeB = volumeDeckB * crossfadeGainB

        deckA.setVolume(finalVolumeA, finalVolumeA)
        deckB.setVolume(finalVolumeB, finalVolumeB)
    }

    fun releaseAll() {
        deckA.release()
        deckB.release()
    }
}
