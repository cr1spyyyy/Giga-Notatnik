package com.example.giganotatnik.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.giganotatnik.R

class InfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)
        supportActionBar?.title = "Informacje"
    }
}
