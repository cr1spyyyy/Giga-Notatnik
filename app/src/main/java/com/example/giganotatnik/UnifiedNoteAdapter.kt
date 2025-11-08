package com.example.giganotatnik.ui

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.giganotatnik.R
import com.example.giganotatnik.audio.AudioPlayerManager
import com.example.giganotatnik.data.Note
import com.example.giganotatnik.data.NoteType
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class UnifiedNoteAdapter(
    private var notes: List<Note>,
    private val context: Context,
    private val onDelete: (Note) -> Unit,
    private val onClick: (Note) -> Unit,
    private val audioPlayerManager: AudioPlayerManager
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_TEXT = 0
        private const val TYPE_AUDIO = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (notes[position].type == NoteType.TEXT) TYPE_TEXT else TYPE_AUDIO
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_TEXT) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_note, parent, false)
            TextNoteViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_audio_note, parent, false)
            AudioNoteViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val note = notes[position]

        if (holder is TextNoteViewHolder) {
            // Tekstowa notatka
            holder.content.text = note.content
            holder.timestamp.text = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                .format(Date(note.timestamp))

            holder.shareButton.setOnClickListener {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, note.content)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Udostępnij notatkę przez"))
            }

            holder.deleteButton.setOnClickListener { onDelete(note) }
            holder.itemView.setOnClickListener { onClick(note) }

        } else if (holder is AudioNoteViewHolder) {
            // Audio notatka
            holder.fileName.text = note.title.ifEmpty { "Audio Note" }

            holder.playButton.setOnClickListener {
                note.audioPath?.let { path ->
                    audioPlayerManager.play(path)
                }
            }


            holder.deleteButton.setOnClickListener {
                // Usuń wpis z bazy
                onDelete(note)
                // Usuń plik z dysku
                note.audioPath?.let { path ->
                    val file = File(path)
                    if (file.exists()) file.delete()
                }
            }

            holder.itemView.setOnClickListener { onClick(note) }
        }
    }

    override fun getItemCount(): Int = notes.size

    fun updateNotes(newNotes: List<Note>) {
        notes = newNotes
        notifyDataSetChanged()
    }

    class TextNoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val content: TextView = itemView.findViewById(R.id.noteContent)
        val timestamp: TextView = itemView.findViewById(R.id.noteTimestamp)
        val shareButton: Button = itemView.findViewById(R.id.btnShareNote)
        val deleteButton: Button = itemView.findViewById(R.id.btnDeleteNote)
    }

    class AudioNoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fileName: TextView = itemView.findViewById(R.id.audioFileName)
        val playButton: Button = itemView.findViewById(R.id.btnPlayAudio)
        val deleteButton: Button = itemView.findViewById(R.id.btnDeleteAudio)
    }
}
