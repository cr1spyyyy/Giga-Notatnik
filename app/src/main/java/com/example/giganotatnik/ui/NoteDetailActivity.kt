package com.example.giganotatnik.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.giganotatnik.R
import com.example.giganotatnik.audio.AudioPlayerManager
import com.example.giganotatnik.data.Note
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NoteDetailActivity : AppCompatActivity() {

    private lateinit var viewModel: NoteViewModel
    private lateinit var originalTitle: String
    private lateinit var originalContent: String
    private var isAudioNote: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_detail)

        // Widoki
        val titleView = findViewById<EditText>(R.id.noteTitle)
        val contentView = findViewById<EditText>(R.id.noteContent)
        val timestampView = findViewById<TextView>(R.id.noteTimestamp)
        val playButton = findViewById<Button>(R.id.btnPlayAudio)
        val locationButton = findViewById<Button>(R.id.btnOpenLocation)
        val saveButton = findViewById<Button>(R.id.btnSaveChanges)
        saveButton.visibility = View.GONE

        // Pobierz notatkę z Intentu
        val note = intent.getSerializableExtra("note") as? Note ?: return
        isAudioNote = note.audioPath != null
        originalTitle = note.title
        originalContent = note.content

        // Inicjalizacja ViewModel
        viewModel = ViewModelProvider(this)[NoteViewModel::class.java]

        // Ustaw dane
        titleView.setText(originalTitle)
        timestampView.text = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(note.timestamp))

        if (isAudioNote) {
            contentView.visibility = View.GONE
            playButton.visibility = View.VISIBLE
            playButton.setOnClickListener {
                AudioPlayerManager(this).play(note.audioPath!!)
            }
        } else {
            contentView.setText(originalContent)
            contentView.visibility = View.VISIBLE
            playButton.visibility = View.GONE
        }

        if (note.latitude != null && note.longitude != null) {
            locationButton.visibility = View.VISIBLE
            locationButton.setOnClickListener {
                val uri = Uri.parse("geo:${note.latitude},${note.longitude}?q=${note.latitude},${note.longitude}(Lokalizacja notatki)")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.setPackage("com.google.android.apps.maps")
                startActivity(intent)
            }
        } else {
            locationButton.visibility = View.GONE
        }

        // Zapisz zmiany
        saveButton.setOnClickListener {
            val updatedTitle = titleView.text.toString()
            val updatedContent = contentView.text.toString()

            val updatedNote = if (isAudioNote) {
                note.copy(title = updatedTitle)
            } else {
                note.copy(title = updatedTitle, content = updatedContent)
            }

            viewModel.updateNote(updatedNote)
            Toast.makeText(this, "Zapisano zmiany", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Nasłuchiwanie zmian
        titleView.addTextChangedListener(createWatcher(titleView, contentView, saveButton))
        if (!isAudioNote) {
            contentView.addTextChangedListener(createWatcher(titleView, contentView, saveButton))
        }
    }

    private fun createWatcher(
        titleView: EditText,
        contentView: EditText,
        saveButton: Button
    ): TextWatcher {
        return object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val currentTitle = titleView.text.toString()
                val currentContent = contentView.text.toString()

                val titleChanged = currentTitle != originalTitle
                val contentChanged = currentContent != originalContent

                val shouldShow = if (isAudioNote) {
                    titleChanged
                } else {
                    titleChanged || contentChanged
                }

                saveButton.visibility = if (shouldShow) View.VISIBLE else View.GONE
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
    }
}
