package com.example.giganotatnik.sensors

import android.app.Dialog
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatDelegate

class LightSensorManager(
    private val context: Context,
    private val thresholdDark: Float = 30f,
    private val thresholdLight: Float = 100f
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    private var currentMode = AppCompatDelegate.getDefaultNightMode()
    private var dialog: Dialog? = null

    fun start() {
        lightSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
        dialog?.dismiss()
        dialog = null
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val lux = event?.values?.firstOrNull() ?: return

        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val userMode = prefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

        // sensor działa tylko w trybie Auto
        if (userMode != AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) return

        val newMode = when {
            lux < thresholdDark -> AppCompatDelegate.MODE_NIGHT_YES
            lux > thresholdLight -> AppCompatDelegate.MODE_NIGHT_NO
            else -> currentMode
        }

        if (newMode != currentMode) {
            currentMode = newMode
            AppCompatDelegate.setDefaultNightMode(newMode)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
