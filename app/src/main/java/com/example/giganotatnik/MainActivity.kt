package com.example.giganotatnik.ui

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.giganotatnik.R
import com.example.giganotatnik.audio.AudioRecorderManager
import com.example.giganotatnik.notifications.NotificationHelper
import com.example.giganotatnik.sensors.LightSensorManager
import com.example.giganotatnik.speech.SpeechRecognitionManager

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: NoteViewModel
    private lateinit var recorderManager: AudioRecorderManager
    private lateinit var speechManager: SpeechRecognitionManager
    private lateinit var lightManager: LightSensorManager
    private lateinit var notificationHelper: NotificationHelper

    private lateinit var noteAdapter: NoteAdapter
    private lateinit var audioAdapter: AudioNoteAdapter

    private var isRecording = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicjalizacja komponentów logiki
        viewModel = ViewModelProvider(this)[NoteViewModel::class.java]
        recorderManager = AudioRecorderManager(this)
        speechManager = SpeechRecognitionManager(this)
        lightManager = LightSensorManager(this)
        notificationHelper = NotificationHelper(this)

        lightManager.start()

        setupUI()
        setupRecyclerViews()
    }

    private fun setupUI() {
        val noteEditText = findViewById<EditText>(R.id.editTextNote)
        val saveButton = findViewById<Button>(R.id.btnSaveText)
        val recordButton = findViewById<Button>(R.id.btnRecord)
        val speechButton = findViewById<Button>(R.id.btnSpeechToText)

        // Rozpoznawanie mowy → wpisuje tekst do pola
        speechButton.setOnClickListener {
            speechManager.startListening { text -> noteEditText.setText(text) }
        }

        // Nagrywanie audio
        recordButton.setOnClickListener {
            if (!isRecording) {
                recorderManager.startRecording()
                recordButton.text = "Zatrzymaj nagrywanie"
            } else {
                val file = recorderManager.stopRecording()
                notificationHelper.show("Zapisano notatkę głosową", file?.name ?: "audio")
                recordButton.text = "Nagraj notatkę"

                // Po nagraniu — odśwież listę plików
                val updatedFiles = filesDir.listFiles()?.filter { it.extension == "3gp" } ?: emptyList()
                audioAdapter.updateAudioFiles(updatedFiles)
            }
            isRecording = !isRecording
        }

        // Zapis notatki tekstowej
        saveButton.setOnClickListener {
            val text = noteEditText.text.toString()
            if (text.isNotBlank()) {
                viewModel.addNote(text)
                notificationHelper.show("Zapisano notatkę", text)
                noteEditText.text.clear()
            }
        }
    }

    private fun setupRecyclerViews() {
        // === Notatki tekstowe ===
        val noteRecyclerView = findViewById<RecyclerView>(R.id.noteRecyclerView)
        noteAdapter = NoteAdapter(emptyList(), this) { note ->
            viewModel.deleteNote(note)
        }
        noteRecyclerView.layoutManager = LinearLayoutManager(this)
        noteRecyclerView.adapter = noteAdapter

        // automatycznie aktualizuje listę
        viewModel.allNotes.observe(this) { notes ->
            noteAdapter.updateNotes(notes)
        }

        //  Notatki audio
        val audioRecyclerView = findViewById<RecyclerView>(R.id.audioRecyclerView)
        audioAdapter = AudioNoteAdapter(emptyList(), this) { file ->
            if (file.delete()) {
                val updatedFiles = filesDir.listFiles()?.filter { it.extension == "3gp" } ?: emptyList()
                audioAdapter.updateAudioFiles(updatedFiles)
                Toast.makeText(this, "Usunięto: ${file.name}", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Nie udało się usunąć pliku", Toast.LENGTH_SHORT).show()
            }
        }
        audioRecyclerView.layoutManager = LinearLayoutManager(this)
        audioRecyclerView.adapter = audioAdapter

        // Wczytaj istniejące nagrania audio przy starcie
        val audioFiles = filesDir.listFiles()?.filter { it.extension == "3gp" } ?: emptyList()
        audioAdapter.updateAudioFiles(audioFiles)
    }

    override fun onDestroy() {
        super.onDestroy()
        lightManager.stop()
    }
}
