package com.example.giganotatnik

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.giganotatnik.ui.NoteViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.giganotatnik.ui.NoteAdapter
import android.media.MediaRecorder
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.RecognitionListener
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.widget.SearchView
import androidx.appcompat.app.AppCompatDelegate
import com.example.giganotatnik.ui.AudioNoteAdapter


class MainActivity : AppCompatActivity() {

    private lateinit var noteEditText: EditText
    private lateinit var saveButton: Button

    private lateinit var viewModel: NoteViewModel

    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null

    private lateinit var audioAdapter: AudioNoteAdapter

    private lateinit var speechRecognizer: SpeechRecognizer

    private lateinit var sensorManager: SensorManager
    private var lightSensor: Sensor? = null
    private val lightThreshold = 30f
    private val lightListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            val lux = event?.values?.firstOrNull() ?: return
            val newMode = if (lux < lightThreshold) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }

            if (AppCompatDelegate.getDefaultNightMode() != newMode) {
                AppCompatDelegate.setDefaultNightMode(newMode)
                recreate() // ⬅️ wymusza restart aktywności i pełne odświeżenie motywu
            }
        }



        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        noteEditText = findViewById(R.id.editTextNote)
        saveButton = findViewById(R.id.btnSaveText)

        viewModel = ViewModelProvider(this)[NoteViewModel::class.java]

        if (!checkAudioPermission()) {
            requestAudioPermission()
        }

        // Czujnik światła
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        lightSensor?.let {
            sensorManager.registerListener(lightListener, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        // Adapter notatek tekstowych
        val noteRecyclerView = findViewById<RecyclerView>(R.id.noteRecyclerView)
        val noteAdapter = NoteAdapter(emptyList(), this) { note ->
            viewModel.deleteNote(note)
        }
        noteRecyclerView.layoutManager = LinearLayoutManager(this)
        noteRecyclerView.adapter = noteAdapter

        viewModel.allNotes.observe(this) { notes ->
            noteAdapter.updateNotes(notes)
        }

// Adapter notatek głosowych
        val audioRecyclerView = findViewById<RecyclerView>(R.id.audioRecyclerView)
        val audioFiles = filesDir.listFiles()?.filter { it.extension == "3gp" } ?: emptyList()

        val audioAdapter = AudioNoteAdapter(audioFiles, this) { file ->
            if (file.delete()) {
                val updatedFiles = filesDir.listFiles()?.filter { it.extension == "3gp" } ?: emptyList()
                audioAdapter.updateAudioFiles(updatedFiles)
                Toast.makeText(this, "Usunięto: ${file.name}", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Nie udało się usunąć pliku", Toast.LENGTH_SHORT).show()
            }
        }

        audioRecyclerView.layoutManager = LinearLayoutManager(this)
        audioRecyclerView.adapter = audioAdapter



        // Rozpoznawanie mowy
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        val speechButton = findViewById<Button>(R.id.btnSpeechToText)
        speechButton.setOnClickListener {
            startSpeechRecognition()
        }

        // Nagrywanie audio
        val recordButton = findViewById<Button>(R.id.btnRecord)
        recordButton.setOnClickListener {
            if (mediaRecorder == null) {
                startRecording()
                recordButton.text = "Zatrzymaj nagrywanie"
            } else {
                stopRecording()
                recordButton.text = "Nagraj notatkę głosową"
            }
        }

        // Sortowanie
        val sortButton = findViewById<Button>(R.id.btnSort)
        sortButton.setOnClickListener {
            val sorted = viewModel.allNotes.value?.sortedByDescending { it.timestamp } ?: emptyList()
            noteAdapter.updateNotes(sorted)
        }

        // Wyszukiwanie
        val searchView = findViewById<SearchView>(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                val filtered = viewModel.allNotes.value?.filter {
                    it.content.contains(newText.orEmpty(), ignoreCase = true)
                } ?: emptyList()
                noteAdapter.updateNotes(filtered)
                return true
            }
        })

        // Zapis notatki tekstowej
        saveButton.setOnClickListener {
            val noteText = noteEditText.text.toString()
            if (noteText.isNotBlank()) {
                viewModel.addNote(noteText)
                Toast.makeText(this, "Zapisano notatkę: $noteText", Toast.LENGTH_SHORT).show()
                noteEditText.text.clear()
                showNotification("Zapisano notatkę", noteText)
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(lightListener)
    }





    private fun checkAudioPermission(): Boolean {
        val permission = Manifest.permission.RECORD_AUDIO
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestAudioPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 100)
    }

    private fun startRecording() {
        if (!checkAudioPermission()) {
            Toast.makeText(this, "Brak uprawnień do mikrofonu", Toast.LENGTH_SHORT).show()
            requestAudioPermission()
            return
        }

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "note_$timestamp.3gp"
        audioFile = File(filesDir, fileName)

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(audioFile!!.absolutePath)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            prepare()
            start()
        }

        Toast.makeText(this, "Nagrywanie rozpoczęte", Toast.LENGTH_SHORT).show()
    }

    private fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null

        Toast.makeText(this, "Zapisano notatkę głosową: ${audioFile?.name}", Toast.LENGTH_SHORT).show()
        showNotification("Zapisano notatkę głosową", audioFile?.name ?: "Plik audio")

        val updatedFiles = filesDir.listFiles()?.filter { it.extension == "3gp" } ?: emptyList()
        audioAdapter.updateAudioFiles(
            filesDir.listFiles()?.filter { it.extension == "3gp" } ?: emptyList()
        )


    }
    private fun startSpeechRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pl-PL")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Mów teraz…")
        }

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {
                Toast.makeText(this@MainActivity, "Błąd rozpoznawania mowy: $error", Toast.LENGTH_SHORT).show()
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val spokenText = matches?.firstOrNull()
                if (!spokenText.isNullOrBlank()) {
                    noteEditText.setText(spokenText)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer.startListening(intent)
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "note_channel"
        val notificationId = System.currentTimeMillis().toInt()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Notatki",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Powiadomienia o zapisanych notatkach"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(this).notify(notificationId, notification)
    }



}