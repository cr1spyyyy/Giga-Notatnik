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
import java.text.SimpleDateFormat
import java.util.*

class UnifiedNoteAdapter(
    private var notes: List<Note>,
    private val context: Context,
    private val onDelete: (Note) -> Unit,
    private val onClick: (Note) -> Unit
) : RecyclerView.Adapter<UnifiedNoteAdapter.NoteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]
        val formattedDate = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(note.timestamp))

        holder.title.text = note.title.ifBlank { note.content.take(30) }
        holder.timestamp.text = formattedDate

        holder.shareButton.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, note.content.ifBlank { note.title })
            }
            context.startActivity(Intent.createChooser(shareIntent, "Udostępnij notatkę przez"))
        }

        holder.deleteButton.setOnClickListener { onDelete(note) }
        holder.itemView.setOnClickListener { onClick(note) }
    }

    override fun getItemCount(): Int = notes.size

    fun updateNotes(newNotes: List<Note>) {
        notes = newNotes
        notifyDataSetChanged()
    }

    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.noteTitle)
        val timestamp: TextView = itemView.findViewById(R.id.noteTimestamp)
        val shareButton: Button = itemView.findViewById(R.id.btnShareNote)
        val deleteButton: Button = itemView.findViewById(R.id.btnDeleteNote)
    }
}
