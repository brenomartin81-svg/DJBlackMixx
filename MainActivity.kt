package com.djblackmix.app

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var djMixer: DJMixer

    private lateinit var tvTrackA: TextView
    private lateinit var tvTrackB: TextView

    private val pickAudioDeckA = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                val fileName = getFileName(uri)
                tvTrackA.text = fileName
                djMixer.loadTrackDeckA(uri) {
                    Toast.makeText(this, "Deck A Pronto!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private val pickAudioDeckB = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                val fileName = getFileName(uri)
                tvTrackB.text = fileName
                djMixer.loadTrackDeckB(uri) {
                    Toast.makeText(this, "Deck B Pronto!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        djMixer = DJMixer(this)

        tvTrackA = findViewById(R.id.tvTrackA)
        tvTrackB = findViewById(R.id.tvTrackB)

        setupDeckAControls()
        setupDeckBControls()
        setupMixerControls()
    }

    private fun setupDeckAControls() {
        val btnLoadA = findViewById<Button>(R.id.btnLoadA)
        val btnPlayA = findViewById<Button>(R.id.btnPlayA)
        val btnCueA = findViewById<Button>(R.id.btnCueA)
        val seekBarVolumeA = findViewById<SeekBar>(R.id.seekBarVolumeA)
        val seekBarPitchA = findViewById<SeekBar>(R.id.seekBarPitchA)

        btnLoadA.setOnClickListener { openAudioSelector(pickAudioDeckA) }

        btnPlayA.setOnClickListener {
            if (djMixer.deckA.isPlaying()) djMixer.deckA.pause() else djMixer.deckA.play()
        }

        btnCueA.setOnClickListener { djMixer.deckA.triggerCue() }

        seekBarPitchA.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val speed = 0.5f + (progress / 100.0f) * 1.0f
                    djMixer.deckA.setPitchSpeed(speed)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        seekBarVolumeA.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) djMixer.setChannelVolumeA(progress / 100.0f)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupDeckBControls() {
        val btnLoadB = findViewById<Button>(R.id.btnLoadB)
        val btnPlayB = findViewById<Button>(R.id.btnPlayB)
        val btnCueB = findViewById<Button>(R.id.btnCueB)
        val seekBarVolumeB = findViewById<SeekBar>(R.id.seekBarVolumeB)
        val seekBarPitchB = findViewById<SeekBar>(R.id.seekBarPitchB)

        btnLoadB.setOnClickListener { openAudioSelector(pickAudioDeckB) }

        btnPlayB.setOnClickListener {
            if (djMixer.deckB.isPlaying()) djMixer.deckB.pause() else djMixer.deckB.play()
        }

        btnCueB.setOnClickListener { djMixer.deckB.triggerCue() }

        seekBarPitchB.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val speed = 0.5f + (progress / 100.0f) * 1.0f
                    djMixer.deckB.setPitchSpeed(speed)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        seekBarVolumeB.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) djMixer.setChannelVolumeB(progress / 100.0f)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupMixerControls() {
        val seekBarCrossfader = findViewById<SeekBar>(R.id.seekBarCrossfader)

        seekBarCrossfader.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val crossfaderValue = (progress - 100) / 100.0f
                    djMixer.setCrossfader(crossfaderValue)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun openAudioSelector(launcher: ActivityResultLauncher<Intent>) {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "audio/*" }
        launcher.launch(intent)
    }

    private fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor: Cursor? = contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) result = it.getString(index)
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/') ?: -1
            if (cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result ?: "Música desconhecida"
    }

    override fun onDestroy() {
        super.onDestroy()
        djMixer.releaseAll()
    }
}
