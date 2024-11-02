package com.example.musicplayer2

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log

import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var playButton: ImageView
    private lateinit var noteImage: ImageView
    private lateinit var seekBar: SeekBar
    private lateinit var textCurrentTime: TextView
    private lateinit var textArtiste: TextView
    private lateinit var textTotalTime: TextView
    private var isPlaying = false
    private var noteAnimator: ObjectAnimator? = null

    private val updateSeekBarReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val currentPosition = intent.getIntExtra("currentPosition", 0)
            val totalDuration = intent.getIntExtra("totalDuration", 0)
            val audioTitle = intent.getStringExtra("audioTitle")

            Log.d("MainActivity", "Current Position: $currentPosition, Total Duration: $totalDuration, Audio Title: $audioTitle")

            seekBar.max = totalDuration
            seekBar.progress = currentPosition

            textCurrentTime.text = formatTime(currentPosition)
            textTotalTime.text = formatTime(totalDuration)

            textArtiste.text = audioTitle ?: "Titre inconnu"

            if (currentPosition >= totalDuration) {
                handleMusicEnded()
            }
        }
    }

    private val musicEndedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            handleMusicEnded()
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        playButton = findViewById(R.id.play)
        noteImage = findViewById(R.id.poster)
        seekBar = findViewById(R.id.seekBar)
        textCurrentTime = findViewById(R.id.textCurrentTime)
        textTotalTime = findViewById(R.id.textTotalTime)
        textArtiste = findViewById(R.id.artiste)

        playButton.setOnClickListener {
            if (isPlaying) {
                pauseMusic()
            } else {
                playMusic()
            }
        }

        registerReceiver(updateSeekBarReceiver, IntentFilter("UPDATE_SEEKBAR"))
        registerReceiver(musicEndedReceiver, IntentFilter("MUSIC_ENDED"))

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    startService(Intent(this@MainActivity, MusicService::class.java).apply {
                        action = "SEEK_TO"
                        putExtra("position", progress)
                    })
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                stopNoteAnimation()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (isPlaying) {
                    startNoteAnimation()
                }
            }
        })
    }

    private fun formatTime(time: Int): String {
        val minutes = time / 1000 / 60
        val seconds = time / 1000 % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun startNoteAnimation() {
        noteAnimator = ObjectAnimator.ofFloat(noteImage, "scaleY", 1f, 1.1f).apply {
            duration = 300
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            start()
        }
    }

    private fun stopNoteAnimation() {
        noteAnimator?.cancel()
        noteImage.scaleY = 1f
        noteAnimator = null
    }

    private fun playMusic() {
        startService(Intent(this, MusicService::class.java).apply { action = "PLAY" })
        playButton.setImageResource(R.drawable.pause)
        startNoteAnimation()
        isPlaying = true
    }

    private fun pauseMusic() {
        startService(Intent(this, MusicService::class.java).apply { action = "PAUSE" })
        playButton.setImageResource(R.drawable.play)
        stopNoteAnimation()
        isPlaying = false
    }

    private fun handleMusicEnded() {
        stopNoteAnimation()
        isPlaying = false
        playButton.setImageResource(R.drawable.play)
        seekBar.progress = seekBar.max
    }

    override fun onDestroy() {
        super.onDestroy()
        playButton.setImageResource(R.drawable.arretez)
        unregisterReceiver(updateSeekBarReceiver)
        unregisterReceiver(musicEndedReceiver)
        stopService(Intent(this, MusicService::class.java))
        stopNoteAnimation()
    }
}
