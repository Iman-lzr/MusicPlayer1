package com.example.musicplayer2

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

class MusicService : Service() {

    private lateinit var mediaPlayer: MediaPlayer
    private val CHANNEL_ID = "MusicPlayerChannel"
    private val handler = Handler()
    private var isPlaying = false
    private val audioTitle = "Makar - Mood (Prod. Ryder&Seno)"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        mediaPlayer = MediaPlayer.create(this, R.raw.audio5)
        mediaPlayer.setOnCompletionListener {
            stopMusic()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "PLAY" -> startMusic()
            "PAUSE" -> pauseMusic()
            "STOP" -> stopMusic()
            "SEEK_TO" -> {
                val position = intent.getIntExtra("position", 0)
                seekTo(position)
            }
        }
        return START_NOT_STICKY
    }

    private fun startMusic() {
        if (!isPlaying) {
            mediaPlayer.start()
            isPlaying = true
            showNotification()
            startUpdatingSeekBar()
            Log.d("MusicService", "Music started")
        }
    }

    private fun pauseMusic() {
        if (isPlaying) {
            mediaPlayer.pause()
            isPlaying = false
            showNotification()
            Log.d("MusicService", "Music paused")
        }
    }

    private fun stopMusic() {
        if (isPlaying) {
            mediaPlayer.stop()
            mediaPlayer.release() // Libère les ressources
            isPlaying = false
            stopForeground(true) // Supprime la notification
            stopSelf() // Arrête le service
            Log.d("MusicService", "Music stopped and notification removed")
        }
    }

    private fun seekTo(position: Int) {
        mediaPlayer.seekTo(position)
        sendSeekBarUpdate()
    }

    private fun showNotification() {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.note)
            .setContentTitle(audioTitle)
            .setContentText("Lecture en cours")
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(isPlaying)
            .addAction(
                if (isPlaying) R.drawable.pause else R.drawable.play,
                if (isPlaying) "Pause" else "Play",
                createActionIntent(if (isPlaying) "PAUSE" else "PLAY")
            )
            .addAction(R.drawable.arretez, "Stop", createActionIntent("STOP"))

        startForeground(1, notificationBuilder.build())
    }

    private fun createActionIntent(action: String): PendingIntent {
        val intent = Intent(this, MusicService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    private fun startUpdatingSeekBar() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                sendSeekBarUpdate()
                handler.postDelayed(this, 1000)
            }
        }, 1000)
    }

    fun sendSeekBarUpdate() {
        val currentPosition = mediaPlayer.currentPosition
        val totalDuration = mediaPlayer.duration

        Intent("UPDATE_SEEKBAR").apply {
            putExtra("currentPosition", currentPosition)
            putExtra("totalDuration", totalDuration)
            putExtra("audioTitle", audioTitle)
            sendBroadcast(this)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Music Player Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        mediaPlayer.release()
    }
}
