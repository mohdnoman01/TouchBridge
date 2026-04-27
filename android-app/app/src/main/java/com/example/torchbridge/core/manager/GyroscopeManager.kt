package com.example.torchbridge.core.manager

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.example.torchbridge.core.model.ControlAction
import com.example.torchbridge.core.model.ControlEvent
import com.example.torchbridge.network.socket.SocketClient

class GyroscopeManager(
    context: Context,
    private val socketClient: SocketClient
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    var isEnabled = false
    var sensitivity = 1.5f
    var isInvertY = false
    
    // Smoothing: Moving Average
    private val bufferSize = 3
    private val xBuffer = FloatArray(bufferSize)
    private val yBuffer = FloatArray(bufferSize)
    private var bufferIndex = 0

    fun start() {
        if (gyroSensor != null) {
            sensorManager.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (!isEnabled || event == null || event.sensor.type != Sensor.TYPE_GYROSCOPE) return

        // 🔥 GYRO FIX: Map axes correctly: dx = -rawY, dy = rawX
        val rawX = event.values[0] // Rotation around X (Pitch)
        val rawY = event.values[1] // Rotation around Y (Yaw)

        // Apply Smoothing
        xBuffer[bufferIndex] = rawX
        yBuffer[bufferIndex] = rawY
        bufferIndex = (bufferIndex + 1) % bufferSize

        val smoothedX = xBuffer.average().toFloat()
        val smoothedY = yBuffer.average().toFloat()

        // Deadzone filter
        val deadzone = 0.01f
        // Correct mapping and sensitivity
        var dx = if (Math.abs(smoothedY) > deadzone) -smoothedY * sensitivity * 10f else 0f
        var dy = if (Math.abs(smoothedX) > deadzone) smoothedX * sensitivity * 10f else 0f

        if (isInvertY) {
            dy = -dy
        }

        if (dx != 0f || dy != 0f) {
            socketClient.send(
                ControlEvent(
                    action = ControlAction.AIM,
                    dx = dx,
                    dy = dy
                )
            )
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
