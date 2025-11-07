package com.example.giganotatnik.audio

import android.content.Context
import android.media.MediaRecorder
import android.widget.Toast
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class AudioRecorderManager(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var currentFile: File? = null

    fun startRecording(): File? {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(context.filesDir, "note_$timestamp.3gp")
        currentFile = file

        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(file.absolutePath)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            prepare()
            start()
        }

        Toast.makeText(context, "Nagrywanie rozpoczęte", Toast.LENGTH_SHORT).show()
        return file
    }

    fun stopRecording(): File? {
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
        Toast.makeText(context, "Nagrywanie zakończone", Toast.LENGTH_SHORT).show()
        return currentFile
    }
}
