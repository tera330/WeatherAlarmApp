package com.example.weatheralarmapp.sound

import android.content.Context
import android.net.Uri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.weatheralarmapp.R

object ExoPlayerManager {
    private var player: ExoPlayer? = null

    fun getPlayer(context: Context): ExoPlayer {
        if (player == null) {
            player =
                ExoPlayer.Builder(context).build().apply {
                    val mediaUri = Uri.parse("android.resource://${context.packageName}/${R.raw.alarm}")
                    val mediaItem = MediaItem.fromUri(mediaUri)
                    setMediaItem(mediaItem)
                    repeatMode = ExoPlayer.REPEAT_MODE_ONE
                    val audioAttributes =
                        AudioAttributes
                            .Builder()
                            .setUsage(C.USAGE_ALARM)
                            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                            .build()
                    setAudioAttributes(audioAttributes, false)
                }
        }
        return player!!
    }

    fun releasePlayer() {
        player?.release()
        player = null
    }
}
