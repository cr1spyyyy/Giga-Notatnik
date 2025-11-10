package com.example.giganotatnik.ui

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.example.giganotatnik.R
import com.example.giganotatnik.notifications.NotificationHelper
import com.example.giganotatnik.sensors.LightSensorManager
import com.example.giganotatnik.speech.SpeechRecognitionManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class CreateNoteActivity : AppCompatActivity() {

    private lateinit var viewModel: NoteViewModel
    private lateinit var speechManager: SpeechRecognitionManager
    private lateinit var lightManager: LightSensorManager
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_note)

        viewModel = ViewModelProvider(this)[NoteViewModel::class.java]
        speechManager = SpeechRecognitionManager(this)
        lightManager = LightSensorManager(this)
        notificationHelper = NotificationHelper(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val toolbar = findViewById<Toolbar>(R.id.createNoteToolbar)
        setSupportActionBar(toolbar)

        // Włącz strzałkę "wstecz"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        lightManager.start()
        setupUI()
    }

    private fun setupUI() {
        val noteEditText = findViewById<EditText>(R.id.editTextNote)
        val saveButton = findViewById<Button>(R.id.btnSaveText)
        val speechButton = findViewById<Button>(R.id.btnSpeechToText)
        val titleEditText = findViewById<EditText>(R.id.editTextTitle)

        // Obsługa speech-to-text
        speechButton.setOnClickListener {
            speechManager.startListening { text -> noteEditText.setText(text) }
        }

        // Obsługa zapisu notatki tekstowej
        saveButton.setOnClickListener {
            val title = titleEditText.text.toString()
            val text = noteEditText.text.toString()

            if (text.isNotBlank()) {
                getCurrentLocation { location ->
                    viewModel.addNote(
                        title = title,
                        content = text,
                        latitude = location?.latitude,
                        longitude = location?.longitude
                    )
                    notificationHelper.show(
                        getString(R.string.note_saved),
                        title.ifBlank { text.take(20) }
                    )
                    titleEditText.text.clear()
                    noteEditText.text.clear()
                }
            }
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Uprawnienia lokalizacji przyznane", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Brak uprawnień do lokalizacji", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        lightManager.stop()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish() // wraca do poprzedniego ekranu (np. lista notatek)
        return true
    }
}
