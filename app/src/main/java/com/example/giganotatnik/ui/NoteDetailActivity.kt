package com.example.giganotatnik.ui

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import com.example.giganotatnik.R
import com.example.giganotatnik.audio.AudioPlayerManager
import com.example.giganotatnik.data.Note
import com.example.giganotatnik.sensors.LightSensorManager
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NoteDetailActivity : AppCompatActivity() {

    private lateinit var viewModel: NoteViewModel
    private lateinit var originalTitle: String
    private lateinit var originalContent: String

    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private var photoUri: Uri? = null

    private lateinit var lightManager: LightSensorManager
    private var isAudioNote: Boolean = false
    private var isPhotoNote: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_detail)

        // Widoki
        val titleView = findViewById<EditText>(R.id.noteTitle)
        val contentView = findViewById<EditText>(R.id.noteContent)
        val timestampView = findViewById<TextView>(R.id.noteTimestamp)
        val playButton = findViewById<Button>(R.id.btnPlayAudio)
        val locationButton = findViewById<Button>(R.id.btnOpenLocation)
        val saveButton = findViewById<Button>(R.id.btnSaveChanges)
        val toolbar = findViewById<Toolbar>(R.id.noteToolbar)
        val photoContainer = findViewById<FrameLayout>(R.id.photoContainer)
        val photoView = findViewById<ImageView>(R.id.notePhoto)
        val deletePhotoButton = findViewById<ImageButton>(R.id.btnDeletePhoto)
        val photoButton = findViewById<Button>(R.id.btnTakePhotoDetail)

        lightManager = LightSensorManager(this)
        lightManager.start()
        saveButton.visibility = View.GONE

        // Pobierz notatkę
        val note = intent.getSerializableExtra("note") as? Note ?: return
        isAudioNote = note.audioPath != null
        isPhotoNote = note.photoPath != null
        originalTitle = note.title
        originalContent = note.content

        // ViewModel
        viewModel = ViewModelProvider(this)[NoteViewModel::class.java]

        // Dane
        titleView.setText(originalTitle)
        timestampView.text = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            .format(Date(note.timestamp))

        // Konfiguracja UI zależnie od typu
        if (isAudioNote) {
            contentView.visibility = View.GONE
            playButton.visibility = View.VISIBLE
            playButton.setOnClickListener { AudioPlayerManager(this).play(note.audioPath!!) }

            if (!note.photoPath.isNullOrBlank()) {
                photoView.setImageURI(note.photoPath.toUri())
                photoView.visibility = View.VISIBLE
                deletePhotoButton.visibility = View.VISIBLE
            } else {
                photoView.visibility = View.GONE
                deletePhotoButton.visibility = View.GONE
            }

        } else if (isPhotoNote) {
            contentView.visibility = View.VISIBLE
            playButton.visibility = View.GONE

            if (!note.photoPath.isNullOrBlank()) {
                photoView.setImageURI(note.photoPath.toUri())
                photoView.visibility = View.VISIBLE
                deletePhotoButton.visibility = View.VISIBLE
            } else {
                photoView.visibility = View.GONE
                deletePhotoButton.visibility = View.GONE
            }

            contentView.setText(originalContent)
        } else {
            contentView.setText(originalContent)
            contentView.visibility = View.VISIBLE
            playButton.visibility = View.GONE
            photoView.visibility = View.GONE
            deletePhotoButton.visibility = View.GONE
        }

        // Pokazanie zdjęcia jeśli istnieje
        if (!note.photoPath.isNullOrBlank()) {
            photoView.setImageURI(note.photoPath.toUri())
            photoContainer.visibility = View.VISIBLE
            deletePhotoButton.visibility = View.VISIBLE
        } else {
            photoContainer.visibility = View.GONE
        }

        // Dodawanie zdjęcia
        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && photoUri != null) {
                val updatedNote = note.copy(
                    photoPath = photoUri.toString(),
                    title = titleView.text.toString().ifBlank { createDefaultPhotoTitle(photoUri!!) },
                    content = if (isAudioNote) "" else contentView.text.toString()
                )
                viewModel.updateNote(updatedNote)

                photoView.setImageURI(photoUri)
                photoContainer.visibility = View.VISIBLE
                deletePhotoButton.visibility = View.VISIBLE

                Toast.makeText(this, "Zdjęcie dodane do notatki", Toast.LENGTH_SHORT).show()
            }
        }

        photoButton.setOnClickListener {
            val photoFile = File.createTempFile("note_photo_", ".jpg", cacheDir)
            photoUri = FileProvider.getUriForFile(this, "${packageName}.provider", photoFile)
            photoUri?.let { uri -> takePictureLauncher.launch(uri) }
        }

        // Usuwanie zdjęcia
        deletePhotoButton.setOnClickListener {

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Usuwanie zdjęcia")
            builder.setMessage("Czy na pewno chcesz usunąć to zdjęcie?")

            // Przycisk USUŃ
            builder.setPositiveButton("Usuń") { dialog, _ ->
                val updatedNote = note.copy(photoPath = null)
                viewModel.updateNote(updatedNote)
                photoContainer.visibility = View.GONE
                Toast.makeText(this, "Zdjęcie usunięte", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            builder.setNegativeButton("Anuluj") { dialog, _ ->
                dialog.dismiss()
            }

            builder.create().show()
        }


        // Powiększanie zdjęcia
        photoView.setOnClickListener {
            if (!note.photoPath.isNullOrBlank()) {
                val dialog = Dialog(this)
                dialog.setContentView(R.layout.dialog_fullscreen_photo)
                val fullImage = dialog.findViewById<ImageView>(R.id.fullscreenPhoto)
                fullImage.setImageURI(note.photoPath.toUri())
                dialog.show()
            }
        }

        // Lokalizacja
        if (note.latitude != null && note.longitude != null) {
            locationButton.visibility = View.VISIBLE
            locationButton.setOnClickListener {
                val uri =
                    "geo:${note.latitude},${note.longitude}?q=${note.latitude},${note.longitude}(Lokalizacja notatki)".toUri()
                val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                    setPackage("com.google.android.apps.maps")
                }
                startActivity(intent)
            }
        } else {
            locationButton.visibility = View.GONE
        }

        // Zapis zmian
        saveButton.setOnClickListener {
            val updatedTitle = titleView.text.toString()
            val updatedContent = contentView.text.toString()
            val updatedNote = when {
                isAudioNote -> note.copy(title = updatedTitle)
                isPhotoNote -> note.copy(title = updatedTitle, content = updatedContent)
                else -> note.copy(title = updatedTitle, content = updatedContent)
            }
            viewModel.updateNote(updatedNote)
            Toast.makeText(this, "Zapisano zmiany", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Watchery
        titleView.addTextChangedListener(createWatcher(titleView, contentView, saveButton))
        if (!isAudioNote) {
            contentView.addTextChangedListener(createWatcher(titleView, contentView, saveButton))
        }

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun createWatcher(
        titleView: EditText,
        contentView: EditText,
        saveButton: Button
    ): TextWatcher {
        return object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val currentTitle = titleView.text.toString()
                val currentContent = contentView.text.toString()
                val titleChanged = currentTitle != originalTitle
                val contentChanged = currentContent != originalContent
                val shouldShow = if (isAudioNote) {
                    titleChanged
                } else {
                    titleChanged || contentChanged
                }
                saveButton.visibility = if (shouldShow) View.VISIBLE else View.GONE
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
    }

    private fun createDefaultPhotoTitle(uri: Uri): String {
        return "Zdjęcie ${SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date())}"
    }
}
