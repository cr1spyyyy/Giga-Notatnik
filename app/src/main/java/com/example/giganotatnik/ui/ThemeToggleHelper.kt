package com.example.giganotatnik.ui

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.giganotatnik.R
import com.example.giganotatnik.sensors.LightSensorManager

class ThemeToggleHelper(
    private val context: Context,
    private val lightSensorManager: LightSensorManager
) {

    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    private val editor = prefs.edit()

    fun setupThemeToggle(fab: FloatingActionButton) {
        val savedMode = prefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        updateFabIcon(fab, savedMode)

        // uruchom sensor tylko jeśli tryb Auto
        if (savedMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
            lightSensorManager.start()
        } else {
            lightSensorManager.stop()
        }

        fab.setOnClickListener {
            val currentMode = prefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            val nextMode = when (currentMode) {
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> AppCompatDelegate.MODE_NIGHT_NO
                AppCompatDelegate.MODE_NIGHT_NO -> AppCompatDelegate.MODE_NIGHT_YES
                AppCompatDelegate.MODE_NIGHT_YES -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }

            // ustaw motyw tylko jeśli realnie się zmienia
            if (nextMode != AppCompatDelegate.getDefaultNightMode()) {
                AppCompatDelegate.setDefaultNightMode(nextMode)
            }

            editor.putInt("theme_mode", nextMode).apply()
            updateFabIcon(fab, nextMode)

            if (nextMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
                lightSensorManager.start()
            } else {
                lightSensorManager.stop()
            }
        }
    }


    private fun updateFabIcon(fab: FloatingActionButton, mode: Int) {
        when (mode) {
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> fab.setImageResource(R.drawable.ic_theme_auto)
            AppCompatDelegate.MODE_NIGHT_NO -> fab.setImageResource(R.drawable.ic_theme_light)
            AppCompatDelegate.MODE_NIGHT_YES -> fab.setImageResource(R.drawable.ic_theme_dark)
            else -> fab.setImageResource(R.drawable.ic_theme_auto)
        }
    }
}

