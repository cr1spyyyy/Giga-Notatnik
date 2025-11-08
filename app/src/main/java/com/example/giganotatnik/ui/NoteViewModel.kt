package com.example.giganotatnik.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.giganotatnik.data.Note
import com.example.giganotatnik.data.NoteDatabase
import com.example.giganotatnik.data.NoteType
import kotlinx.coroutines.launch

class NoteViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = NoteDatabase.getDatabase(application).noteDao()
    val allNotes: LiveData<List<Note>> = dao.getAllNotes()

    fun addNote(title: String, content: String) {
        viewModelScope.launch {
            val note = Note(
                title = title,
                content = content,
                type = NoteType.TEXT
            )
            dao.insertNote(note)
        }
    }

    fun addAudioNote(title: String, audioPath: String) {
        viewModelScope.launch {
            val note = Note(
                title = title,
                content = "",
                type = NoteType.AUDIO,
                audioPath = audioPath
            )
            dao.insertNote(note)
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            dao.deleteNote(note)
        }
    }
}