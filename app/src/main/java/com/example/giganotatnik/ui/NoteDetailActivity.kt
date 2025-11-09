package com.example.giganotatnik.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.giganotatnik.R
import com.example.giganotatnik.audio.AudioPlayerManager
import com.example.giganotatnik.data.Note
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NoteDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_detail)

        val titleView = findViewById<TextView>(R.id.noteTitle)
        val timestampView = findViewById<TextView>(R.id.noteTimestamp)
        val contentView = findViewById<TextView>(R.id.noteContent)
        val playButton = findViewById<Button>(R.id.btnPlayAudio)
        val locationButton = findViewById<Button>(R.id.btnOpenLocation)

        val note = intent.getSerializableExtra("note") as? Note ?: return

        titleView.text = note.title
        timestampView.text = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(note.timestamp))

        if (note.audioPath != null) {
            contentView.text = "Notatka głosowa"
            playButton.visibility = View.VISIBLE
            playButton.setOnClickListener {
                AudioPlayerManager(this).play(note.audioPath!!)
            }
        } else {
            contentView.text = note.content
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
    }
}
