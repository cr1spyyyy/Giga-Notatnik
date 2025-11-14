package com.example.giganotatnik.ui

import android.content.Intent
import android.hardware.lights.LightsManager
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.giganotatnik.R
import com.example.giganotatnik.sensors.LightSensorManager
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var themeHelper: ThemeToggleHelper
    private lateinit var lightSensorManager: LightSensorManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lightSensorManager = LightSensorManager(this)
        themeHelper = ThemeToggleHelper(this, lightSensorManager)

        val createButton = findViewById<Button>(R.id.btnGoToCreate)
        val historyButton = findViewById<Button>(R.id.btnGoToHistory)
        val recordButton = findViewById<Button>(R.id.btnRecord)
        val themeButton = findViewById<FloatingActionButton>(R.id.themeToggleFab)

        createButton.setOnClickListener {
            startActivity(Intent(this, CreateNoteActivity::class.java))
        }

        historyButton.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        recordButton.setOnClickListener {
            startActivity(Intent(this, CreateAudioNoteActivity::class.java))
        }

        themeHelper.setupThemeToggle(themeButton)
    }
    override fun onDestroy() {
        super.onDestroy()
        lightSensorManager.stop()
    }
}
