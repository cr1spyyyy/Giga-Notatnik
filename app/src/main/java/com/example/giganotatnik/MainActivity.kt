package com.example.giganotatnik.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.example.giganotatnik.R
import com.example.giganotatnik.audio.AudioRecorderManager
import com.example.giganotatnik.notifications.NotificationHelper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var recorderManager: AudioRecorderManager
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var viewModel: NoteViewModel

    private var isRecording = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val createButton = findViewById<Button>(R.id.btnGoToCreate)
        val historyButton = findViewById<Button>(R.id.btnGoToHistory)
        val recordButton = findViewById<Button>(R.id.btnRecord) // <-- dodane

        createButton.setOnClickListener {
            startActivity(Intent(this, CreateNoteActivity::class.java))
        }

        historyButton.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        recorderManager = AudioRecorderManager(this)
        notificationHelper = NotificationHelper(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        viewModel = ViewModelProvider(this)[NoteViewModel::class.java]

        recordButton.setOnClickListener {
            if (!isRecording) {
                recorderManager.startRecording()
                recordButton.text = getString(R.string.stop_recording)
            } else {
                val file = recorderManager.stopRecording()
                if (file != null) {
                    val timestamp = System.currentTimeMillis()
                    val formattedDate = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(timestamp)
                    val finalTitle = "Nagranie z $formattedDate"

                    getCurrentLocation { location ->
                        viewModel.addAudioNote(
                            title = finalTitle,
                            audioPath = file.absolutePath,
                            latitude = location?.latitude,
                            longitude = location?.longitude
                        )
                        notificationHelper.show(getString(R.string.audio_saved), finalTitle)
                    }
                }
                recordButton.text = getString(R.string.record_note)
            }
            isRecording = !isRecording
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
}
