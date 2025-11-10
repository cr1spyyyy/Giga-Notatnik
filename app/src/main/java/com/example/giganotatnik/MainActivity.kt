package com.example.giganotatnik.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.giganotatnik.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val createButton = findViewById<Button>(R.id.btnGoToCreate)
        val historyButton = findViewById<Button>(R.id.btnGoToHistory)
        val recordButton = findViewById<Button>(R.id.btnRecord)

        createButton.setOnClickListener {
            startActivity(Intent(this, CreateNoteActivity::class.java))
        }

        historyButton.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        recordButton.setOnClickListener {
            startActivity(Intent(this, CreateAudioNoteActivity::class.java))
        }
    }
}
