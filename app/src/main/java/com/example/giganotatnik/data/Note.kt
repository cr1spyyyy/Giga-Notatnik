package com.example.giganotatnik.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String = "", // domyślnie = content
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val type: NoteType = NoteType.TEXT, // TEXT lub AUDIO
    val audioPath: String? = null, // null dla tekstowych
    val photoPaths: List<String> = emptyList(),
    val latitude: Double? = null,
    val longitude: Double? = null
): Serializable

enum class NoteType {
    TEXT,
    AUDIO,
    PHOTO
}
