package com.example.giganotatnik.ui

import android.content.Context
import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.giganotatnik.R
import java.io.File

class AudioNoteAdapter(private var audioFiles: List<File>,
                       private val context: Context,
                       private val onDelete: (File) -> Unit
) : RecyclerView.Adapter<AudioNoteAdapter.AudioNoteViewHolder>() {

    class AudioNoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fileName: TextView = itemView.findViewById(R.id.audioFileName)
        val playButton: Button = itemView.findViewById(R.id.btnPlayAudio)

        val deleteButton: Button = itemView.findViewById(R.id.btnDeleteAudio)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioNoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_audio_note, parent, false)
        return AudioNoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: AudioNoteViewHolder, position: Int) {
        val file = audioFiles[position]
        holder.fileName.text = file.name

        holder.playButton.setOnClickListener {
            val mediaPlayer = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                prepare()
                start()
            }
        }

        holder.deleteButton.setOnClickListener {
            onDelete(file)
        }

    }

    fun updateAudioFiles(newFiles: List<File>) {
        audioFiles = newFiles
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = audioFiles.size
}
