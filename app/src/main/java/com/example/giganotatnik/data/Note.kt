package com.example.giganotatnik.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String = "",                        // domyślnie = content
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val type: NoteType = NoteType.TEXT,            // TEXT lub AUDIO
    val audioPath: String? = null                  // null dla tekstowych
)

enum class NoteType {
    TEXT,
    AUDIO
}
