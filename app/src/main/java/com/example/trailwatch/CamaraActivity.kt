package com.example.trailwatch

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CamaraActivity : AppCompatActivity() {

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var imageCapture: ImageCapture
    private lateinit var previewView: PreviewView
    private val handler = Handler(Looper.getMainLooper())
    private val captureInterval: Long = 5000 // 5 segundos

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camara)

        previewView = findViewById(R.id.previewView)
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Solicitar permisos de cámara si no están concedidos
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Solicitar el permiso de cámara en tiempo de ejecución
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            startCamera()
        } else {
            Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }

    // Iniciar la cámara
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = androidx.camera.core.Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)

                // Iniciar la captura periódica de fotos
                startPhotoCaptureLoop()

            } catch (e: Exception) {
                Log.e("CamaraActivity", "Error al iniciar la cámara: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    // Método para tomar una foto
    private fun takePhoto() {
        val photoFile = File(getOutputDirectory(), "${System.currentTimeMillis()}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(outputOptions, cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e("CamaraActivity", "Error al tomar la foto: ${exc.message}")
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    Log.d("CamaraActivity", "Foto guardada en: ${photoFile.absolutePath}")
                }
            })
    }

    // Bucle para capturar fotos periódicamente
    private fun startPhotoCaptureLoop() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                takePhoto()
                handler.postDelayed(this, captureInterval)
            }
        }, captureInterval)
    }

    // Obtener el directorio para guardar fotos
    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return mediaDir ?: filesDir
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown() // Cerrar el hilo de la cámara
        handler.removeCallbacksAndMessages(null) // Detener el bucle de captura
    }
}
