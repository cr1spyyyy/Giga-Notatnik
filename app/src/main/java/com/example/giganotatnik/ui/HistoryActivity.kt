package com.example.giganotatnik.ui

import android.os.Bundle
import android.widget.Button
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.giganotatnik.R

class HistoryActivity : AppCompatActivity() {

    private lateinit var viewModel: NoteViewModel
    private lateinit var noteAdapter: NoteAdapter
    private lateinit var audioAdapter: AudioNoteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        viewModel = ViewModelProvider(this)[NoteViewModel::class.java]

        setupRecyclerViews()
        setupUI()
    }

    private fun setupUI() {
        val searchView = findViewById<SearchView>(R.id.searchView)

        // Wyszukiwanie
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { noteAdapter.filterNotes(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { noteAdapter.filterNotes(it) }
                return true
            }
        })


    }

    private fun setupRecyclerViews() {
        // Notatki tekstowe
        val noteRecyclerView = findViewById<RecyclerView>(R.id.noteRecyclerView)
        noteAdapter = NoteAdapter(emptyList(), this) { note ->
            viewModel.deleteNote(note)
        }
        noteRecyclerView.layoutManager = LinearLayoutManager(this)
        noteRecyclerView.adapter = noteAdapter

        viewModel.allNotes.observe(this) { notes ->
            noteAdapter.updateNotes(notes)
        }

        // Notatki audio
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

        val audioFiles = filesDir.listFiles()?.filter { it.extension == "3gp" } ?: emptyList()
        audioAdapter.updateAudioFiles(audioFiles)

        val backButton = findViewById<Button>(R.id.btnBackToMain)
        backButton.setOnClickListener {
            finish() // zamyka HistoryActivity i wraca do MainActivity
        }

    }
}
