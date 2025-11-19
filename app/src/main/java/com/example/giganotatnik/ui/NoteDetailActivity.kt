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
import java.util.*

class NoteDetailActivity : AppCompatActivity() {

    private lateinit var viewModel: NoteViewModel
    private lateinit var originalTitle: String
    private lateinit var originalContent: String

    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private var tempPhotoUri: Uri? = null
    private val photoUris = mutableListOf<Uri>()

    private lateinit var lightManager: LightSensorManager
    private var isAudioNote = false

    private lateinit var photoContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_detail)

        val titleView = findViewById<EditText>(R.id.noteTitle)
        val contentView = findViewById<EditText>(R.id.noteContent)
        val timestampView = findViewById<TextView>(R.id.noteTimestamp)
        val playButton = findViewById<Button>(R.id.btnPlayAudio)
        val locationButton = findViewById<Button>(R.id.btnOpenLocation)
        val saveButton = findViewById<Button>(R.id.btnSaveChanges)
        val toolbar = findViewById<Toolbar>(R.id.noteToolbar)
        val photoButton = findViewById<Button>(R.id.btnTakePhotoDetail)
        photoContainer = findViewById(R.id.photoContainer)

        lightManager = LightSensorManager(this)
        lightManager.start()
        saveButton.visibility = View.GONE

        val note = intent.getSerializableExtra("note") as? Note ?: return
        isAudioNote = note.audioPath != null
        originalTitle = note.title
        originalContent = note.content
        photoUris.addAll(note.photoPaths.map { it.toUri() })

        viewModel = ViewModelProvider(this)[NoteViewModel::class.java]

        titleView.setText(originalTitle)
        contentView.setText(originalContent)
        timestampView.text = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(note.timestamp))

        if (isAudioNote) {
            contentView.visibility = View.GONE
            playButton.visibility = View.VISIBLE
            playButton.setOnClickListener { AudioPlayerManager(this).play(note.audioPath!!) }
        } else {
            contentView.visibility = View.VISIBLE
            playButton.visibility = View.GONE
        }

        updatePhotoContainer()

        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && tempPhotoUri != null) {
                photoUris.add(tempPhotoUri!!)
                updatePhotoContainer()

                val updatedNote = note.copy(photoPaths = photoUris.map { it.toString() })
                viewModel.updateNote(updatedNote)

                Toast.makeText(this, "Zdjęcie dodane do notatki", Toast.LENGTH_SHORT).show()
            }
        }


        photoButton.setOnClickListener {
            val photoFile = File.createTempFile("note_photo_", ".jpg", cacheDir)
            tempPhotoUri = FileProvider.getUriForFile(this, "${packageName}.provider", photoFile)
            tempPhotoUri?.let { takePictureLauncher.launch(it) }
        }


        locationButton.visibility = if (note.latitude != null && note.longitude != null) View.VISIBLE else View.GONE
        locationButton.setOnClickListener {
            val uri = "geo:${note.latitude},${note.longitude}?q=${note.latitude},${note.longitude}(Lokalizacja notatki)".toUri()
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage("com.google.android.apps.maps")
            }
            startActivity(intent)
        }

        saveButton.setOnClickListener {
            val updatedNote = note.copy(
                title = titleView.text.toString(),
                content = if (isAudioNote) "" else contentView.text.toString(),
                photoPaths = photoUris.map { it.toString() }
            )
            viewModel.updateNote(updatedNote)
            Toast.makeText(this, "Zapisano zmiany", Toast.LENGTH_SHORT).show()
            finish()
        }

        titleView.addTextChangedListener(createWatcher(titleView, contentView, saveButton))
        if (!isAudioNote) {
            contentView.addTextChangedListener(createWatcher(titleView, contentView, saveButton))
        }

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    private fun updatePhotoContainer() {
        photoContainer.removeAllViews()

        val scrollView = findViewById<HorizontalScrollView>(R.id.photoScroll)
        if (photoUris.isEmpty()) {
            scrollView.visibility = View.GONE
            return
        } else {
            scrollView.visibility = View.VISIBLE
        }

        photoUris.forEachIndexed { index, uri ->
            val frame = FrameLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(300, 300).apply {
                    setMargins(8, 8, 8, 8)
                }
            }

            val imageView = ImageView(this).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                setImageURI(uri)
                scaleType = ImageView.ScaleType.CENTER_CROP
                setOnClickListener { showFullScreenPhoto(uri) }
            }

            val deleteButton = ImageButton(this).apply {
                layoutParams = FrameLayout.LayoutParams(80, 80).apply {
                    topMargin = 8
                    marginEnd = 8
                    gravity = android.view.Gravity.TOP or android.view.Gravity.END
                }
                setImageResource(R.drawable.ic_close)
                background = getDrawable(R.drawable.bg_delete_button)
                setOnClickListener {
                    AlertDialog.Builder(this@NoteDetailActivity)
                        .setTitle("Usuwanie zdjęcia")
                        .setMessage("Czy na pewno chcesz usunąć to zdjęcie?")
                        .setPositiveButton("Usuń") { dialog, _ ->
                            photoUris.removeAt(index)
                            updatePhotoContainer()
                            dialog.dismiss()
                        }
                        .setNegativeButton("Anuluj") { dialog, _ -> dialog.dismiss() }
                        .show()
                }
            }

            frame.addView(imageView)
            frame.addView(deleteButton)
            photoContainer.addView(frame)
        }
    }


    private fun showFullScreenPhoto(uri: Uri) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_fullscreen_photo)
        val fullImage = dialog.findViewById<ImageView>(R.id.fullscreenPhoto)
        fullImage.setImageURI(uri)
        dialog.show()
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
                saveButton.visibility = if (titleChanged || contentChanged) View.VISIBLE else View.GONE
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
