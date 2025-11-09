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

    fun addNote( title: String,
                 content: String,
                 latitude: Double? = null,
                 longitude: Double? = null) {
        viewModelScope.launch {
            val note = Note(
                title = title,
                content = content,
                latitude = latitude,
                longitude = longitude
            )
            dao.insertNote(note)
        }
    }

    fun addAudioNote( title: String,
                      audioPath: String,
                      latitude: Double? = null,
                      longitude: Double? = null) {
        viewModelScope.launch {
            val note = Note(
                title = title,
                content = "",
                audioPath = audioPath,
                type = NoteType.AUDIO,
                latitude = latitude,
                longitude = longitude
            )
            dao.insertNote(note)
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            dao.deleteNote(note)
        }
    }
    fun updateNote(note: Note) {
        viewModelScope.launch {
            dao.update(note)
        }
    }

}