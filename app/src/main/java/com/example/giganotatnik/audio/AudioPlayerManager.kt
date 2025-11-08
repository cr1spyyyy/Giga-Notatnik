package com.example.giganotatnik.audio

import android.content.Context
import android.media.MediaPlayer
import android.widget.Toast
import java.io.File

class AudioPlayerManager(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null

    fun play(path: String) {
        val file = File(path)
        if (!file.exists()) {
            Toast.makeText(context, "Plik audio nie istnieje", Toast.LENGTH_SHORT).show()
            return
        }

        stop() // zatrzymaj poprzednie odtwarzanie, jeśli trwa

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(path)
                prepare()
                start()
                setOnCompletionListener {
                    stop()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Nie można odtworzyć pliku audio", Toast.LENGTH_SHORT).show()
        }
    }

    fun stop() {
        mediaPlayer?.let {
            if (it.isPlaying) it.stop()
            it.release()
        }
        mediaPlayer = null
    }
}
