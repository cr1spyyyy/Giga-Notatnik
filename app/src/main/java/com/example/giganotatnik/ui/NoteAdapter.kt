package com.example.giganotatnik.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.giganotatnik.R
import com.example.giganotatnik.data.Note
import java.text.SimpleDateFormat
import java.util.*
import android.content.Intent
import android.content.Context
import android.widget.Button


class NoteAdapter(
    private var notes: List<Note>,
    private val context: Context,
    private val onDelete: (Note) -> Unit
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    // lista aktualnie wyświetlana (może być filtrowana)
    private var displayedNotes: List<Note> = notes

    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val content: TextView = itemView.findViewById(R.id.noteContent)
        val timestamp: TextView = itemView.findViewById(R.id.noteTimestamp)
        val shareButton: Button = itemView.findViewById(R.id.btnShareNote)
        val deleteButton: Button = itemView.findViewById(R.id.btnDeleteNote)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = displayedNotes[position]
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

        holder.deleteButton.setOnClickListener {
            onDelete(note)
        }
    }

    override fun getItemCount(): Int = displayedNotes.size

    fun updateNotes(newNotes: List<Note>) {
        notes = newNotes
        displayedNotes = newNotes
        notifyDataSetChanged()
    }

    fun filterNotes(query: String) {
        displayedNotes = notes.filter { it.content.contains(query, ignoreCase = true) }
        notifyDataSetChanged()
    }

}

