package com.example.flashlightshaker
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.Manifest
import android.hardware.camera2.CameraCharacteristics
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.flashlightshaker.ui.theme.FlashlightShakerTheme

class MainActivity : ComponentActivity() {

    private lateinit var sensorManager: SensorManager
    private var accelerometerSensor: Sensor? = null
    private lateinit var shakeDetector: ShakeDetector
    private var isFlashOn = false
    private lateinit var cameraManager: CameraManager

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FlashlightShakerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting()
                }
            }
        }

        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

        // Inicializa el SensorManager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        // Obtiene el sensor de acelerómetro
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // Inicializa el ShakeDetector
        shakeDetector = ShakeDetector {
            // Aquí se implementará la lógica para encender/apagar el flash
            toggleFlashlight()
        }

        // Comprueba y solicita el permiso de la cámara si es necesario
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
        }
    }

    private fun toggleFlashlight() {
        try {
            if (!hasFlash()) {
                // Manejar dispositivos sin flash. Podrías mostrar un mensaje al usuario.
                return
            }

            // Verifica si el permiso de la cámara está concedido
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                val cameraId = cameraManager.cameraIdList[0] // Obtén el ID de la cámara (deberías manejar esto de manera más robusta)
                isFlashOn = !isFlashOn // Cambia el estado del flash
                cameraManager.setTorchMode(cameraId, isFlashOn) // Enciende o apaga el flash
            } else {
                // Manejar la situación donde el permiso no está concedido
            }
        } catch (e: CameraAccessException) {
            // Manejar cualquier error de acceso a la cámara
        }
    }

    private fun hasFlash(): Boolean {
        val cameraId: String = cameraManager.cameraIdList[0] // Asumimos el primer ID de cámara
        val characteristics = cameraManager.getCameraCharacteristics(cameraId)
        return characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permiso concedido
                } else {
                    // Permiso negado
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }


    override fun onResume() {
        super.onResume()
        accelerometerSensor?.also { accelerometer ->
            sensorManager.registerListener(shakeDetector, accelerometer, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(shakeDetector)
    }
}

@Composable
fun Greeting() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Agita el dispositivo para encender/apagar el flash", style = MaterialTheme.typography.headlineMedium)
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    FlashlightShakerTheme {
        Greeting()
    }
}
