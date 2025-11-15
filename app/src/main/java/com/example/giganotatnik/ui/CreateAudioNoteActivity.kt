package com.example.giganotatnik.ui

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.example.giganotatnik.R
import com.example.giganotatnik.audio.AudioRecorderManager
import com.example.giganotatnik.notifications.NotificationHelper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class CreateAudioNoteActivity : AppCompatActivity() {

    private lateinit var recorderManager: AudioRecorderManager
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var viewModel: NoteViewModel
    private lateinit var recordButton: Button
    private lateinit var photoButton: Button
    private lateinit var titleField: EditText
    private lateinit var photoPreview: ImageView
    private var isRecording = false
    private var photoUri: Uri? = null
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_audio_note)
        val toolbar = findViewById<Toolbar>(R.id.createNoteToolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        recorderManager = AudioRecorderManager(this)
        notificationHelper = NotificationHelper(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        viewModel = ViewModelProvider(this)[NoteViewModel::class.java]

        titleField = findViewById(R.id.editTextAudioTitle)
        recordButton = findViewById(R.id.btnStartRecording)
        photoButton = findViewById(R.id.btnTakePhotoAudio)

        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && photoUri != null) {
                Toast.makeText(this, "Zdjęcie zapisane", Toast.LENGTH_SHORT).show()
            }
        }
        recordButton.setOnClickListener {
            if (!isRecording) {
                recorderManager.startRecording()
                isRecording = true
                recordButton.text = getString(R.string.stop_recording)
            } else {
                val file = recorderManager.stopRecording()
                if (file != null) {
                    val timestamp = System.currentTimeMillis()
                    val formattedDate = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(timestamp)
                    val finalTitle = titleField.text.toString().ifBlank { "Nagranie z $formattedDate" }

                    getCurrentLocation { location ->
                        viewModel.addAudioNote(
                            title = finalTitle,
                            audioPath = file.absolutePath,
                            latitude = location?.latitude,
                            longitude = location?.longitude,
                            photoPath = photoUri?.toString() ?: ""
                        )
                        notificationHelper.show(getString(R.string.audio_saved), finalTitle)
                        finish()
                    }
                }
                isRecording = false
                recordButton.text = getString(R.string.record_note)
            }
        }
        photoPreview = findViewById(R.id.photoPreview)

        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && photoUri != null) {
                Toast.makeText(this, "Zdjęcie zapisane", Toast.LENGTH_SHORT).show()
                photoPreview.setImageURI(photoUri)
                photoPreview.visibility = View.VISIBLE
            }
        }

        photoButton.setOnClickListener {
            val photoFile = File.createTempFile("note_photo_", ".jpg", cacheDir)
            photoUri = FileProvider.getUriForFile(this, "${packageName}.provider", photoFile)
            photoUri?.let { takePictureLauncher.launch(it) }
        }
    }

    private fun getCurrentLocation(onLocationReady: (Location?) -> Unit) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                100
            )
            onLocationReady(null)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            onLocationReady(location)
        }.addOnFailureListener {
            onLocationReady(null)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Uprawnienia lokalizacji przyznane", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Brak uprawnień do lokalizacji", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

}
