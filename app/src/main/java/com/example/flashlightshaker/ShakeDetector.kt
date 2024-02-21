package com.example.flashlightshaker

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import kotlin.math.pow
import kotlin.math.sqrt

class ShakeDetector(private val onShake: () -> Unit) : SensorEventListener {
    private var lastUpdate: Long = 0
    private var last_x: Float = 0.0f
    private var last_y: Float = 0.0f
    private var last_z: Float = 0.0f
    private val shakeThreshold = 800 // Este valor determina la fuerza de la sacudida necesaria

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // No es necesario para este caso
    }

    override fun onSensorChanged(event: SensorEvent) {
        val curTime = System.currentTimeMillis()
        // Solo permite una actualización cada 100ms.
        if ((curTime - lastUpdate) > 100) {
            val diffTime = curTime - lastUpdate
            lastUpdate = curTime

            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val speed = sqrt((x - last_x).toDouble().pow(2.0) + (y - last_y).toDouble().pow(2.0) + (z - last_z).toDouble().pow(2.0)) / diffTime * 10000

            if (speed > shakeThreshold) {
                onShake()
            }

            last_x = x
            last_y = y
            last_z = z
        }
    }
}