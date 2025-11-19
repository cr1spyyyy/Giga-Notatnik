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

    fun addAudioNote(
        title: String,
        audioPath: String,
        latitude: Double? = null,
        longitude: Double? = null,
        photoPaths: List<String> = emptyList()
    ) {
        val note = Note(
            title = title,
            content = "",
            type = NoteType.AUDIO,
            audioPath = audioPath,
            photoPaths = photoPaths,
            latitude = latitude,
            longitude = longitude
        )
        viewModelScope.launch {
            dao.insertNote(note)
        }
    }

    fun addPhotoNote(
        title: String,
        content: String = "",
        photoPaths: List<String>,
        latitude: Double? = null,
        longitude: Double? = null
    ) {
        viewModelScope.launch {
            val note = Note(
                title = title,
                content = content,
                photoPaths = photoPaths,
                type = NoteType.PHOTO,
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