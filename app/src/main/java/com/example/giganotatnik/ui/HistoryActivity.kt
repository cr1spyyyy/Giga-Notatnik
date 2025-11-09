package com.example.giganotatnik.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.giganotatnik.R
import com.example.giganotatnik.audio.AudioPlayerManager

class HistoryActivity : AppCompatActivity() {

    private lateinit var viewModel: NoteViewModel
    private lateinit var unifiedAdapter: UnifiedNoteAdapter
    private lateinit var audioPlayerManager: AudioPlayerManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        viewModel = ViewModelProvider(this)[NoteViewModel::class.java]
        audioPlayerManager = AudioPlayerManager(this)

        setupRecyclerView()
        setupUI()
    }

    private fun setupUI() {
        val searchView = findViewById<SearchView>(R.id.searchView)

        // Wyszukiwanie w notatkach
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { unifiedAdapter.updateNotes(
                    viewModel.allNotes.value?.filter { note ->
                        note.title.contains(it, ignoreCase = true) ||
                                note.content.contains(it, ignoreCase = true)
                    } ?: emptyList()
                )}
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { unifiedAdapter.updateNotes(
                    viewModel.allNotes.value?.filter { note ->
                        note.title.contains(it, ignoreCase = true) ||
                                note.content.contains(it, ignoreCase = true)
                    } ?: emptyList()
                )}
                return true
            }
        })
    }

    private fun setupRecyclerView() {
        val unifiedRecyclerView = findViewById<RecyclerView>(R.id.unifiedRecyclerView)

        unifiedAdapter = UnifiedNoteAdapter(
            emptyList(),
            this,
            onDelete = { note ->
                viewModel.deleteNote(note)
            },
            onClick = { note ->
                val intent = Intent(this, NoteDetailActivity::class.java)
                intent.putExtra("note", note)
                startActivity(intent)
            },
            audioPlayerManager = audioPlayerManager // przekazanie managera
        )

        unifiedRecyclerView.layoutManager = LinearLayoutManager(this)
        unifiedRecyclerView.adapter = unifiedAdapter

        viewModel.allNotes.observe(this) { notes ->
            unifiedAdapter.updateNotes(notes)
        }

        val backButton = findViewById<Button>(R.id.btnBackToMain)
        backButton.setOnClickListener {
            finish()
        }
    }

}
