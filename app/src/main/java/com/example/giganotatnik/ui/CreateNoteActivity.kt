package com.example.giganotatnik.ui

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.giganotatnik.R
import com.example.giganotatnik.audio.AudioRecorderManager
import com.example.giganotatnik.notifications.NotificationHelper
import com.example.giganotatnik.sensors.LightSensorManager
import com.example.giganotatnik.speech.SpeechRecognitionManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CreateNoteActivity : AppCompatActivity() {

    private lateinit var viewModel: NoteViewModel
    private lateinit var recorderManager: AudioRecorderManager
    private lateinit var speechManager: SpeechRecognitionManager
    private lateinit var lightManager: LightSensorManager
    private lateinit var notificationHelper: NotificationHelper

    private var isRecording = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_note)

        // Inicjalizacja logiki
        viewModel = ViewModelProvider(this)[NoteViewModel::class.java]
        recorderManager = AudioRecorderManager(this)
        speechManager = SpeechRecognitionManager(this)
        lightManager = LightSensorManager(this)
        notificationHelper = NotificationHelper(this)

        lightManager.start()

        setupUI()
    }

    private fun setupUI() {
        val noteEditText = findViewById<EditText>(R.id.editTextNote)
        val saveButton = findViewById<Button>(R.id.btnSaveText)
        val recordButton = findViewById<Button>(R.id.btnRecord)
        val speechButton = findViewById<Button>(R.id.btnSpeechToText)
        val backButton = findViewById<Button>(R.id.btnBackToMain)
        val titleEditText = findViewById<EditText>(R.id.editTextTitle)

        backButton.setOnClickListener {
            finish() // zamyka CreateNoteActivity i wraca do MainActivity
        }

        // Rozpoznawanie mowy
        speechButton.setOnClickListener {
            speechManager.startListening { text -> noteEditText.setText(text) }
        }

// Nagrywanie audio
        recordButton.setOnClickListener {
            if (!isRecording) {
                recorderManager.startRecording()
                recordButton.text = getString(R.string.stop_recording)
            } else {
                val file = recorderManager.stopRecording()
                if (file != null) {
                    val userTitle = titleEditText.text.toString()
                    val timestamp = System.currentTimeMillis()
                    val formattedDate = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(timestamp))
                    val finalTitle = if (userTitle.isBlank()) "Nagranie z $formattedDate" else userTitle

                    viewModel.addAudioNote(
                        title = finalTitle,
                        audioPath = file.absolutePath
                    )

                    notificationHelper.show(getString(R.string.audio_saved), finalTitle)
                    titleEditText.text.clear()
                }
                recordButton.text = getString(R.string.record_note)
            }
            isRecording = !isRecording
        }

        // Zapis notatki tekstowej
        saveButton.setOnClickListener {
            val title = titleEditText.text.toString()
            val text = noteEditText.text.toString()

            if (text.isNotBlank()) {
                viewModel.addNote(title, text)
                notificationHelper.show(getString(R.string.note_saved), title.ifBlank { text.take(20) })
                titleEditText.text.clear()
                noteEditText.text.clear()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        lightManager.stop()
    }

}
