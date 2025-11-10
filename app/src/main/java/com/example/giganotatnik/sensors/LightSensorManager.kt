package com.example.giganotatnik.sensors

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

    fun start() {
        lightSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val lux = event?.values?.firstOrNull() ?: return

        val newMode = when {
            lux < thresholdDark -> AppCompatDelegate.MODE_NIGHT_YES
            lux > thresholdLight -> AppCompatDelegate.MODE_NIGHT_NO
            else -> currentMode
        }

        if (newMode != currentMode) {
            AppCompatDelegate.setDefaultNightMode(newMode)
            currentMode = newMode
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
