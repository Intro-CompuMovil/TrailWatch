package com.example.trailwatch

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.io.File
import java.io.IOException
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class PantallaMapaActivity : AppCompatActivity(), SensorEventListener,LocationListener {

    private lateinit var imageBackground: ImageView
    private lateinit var sensorManager: SensorManager
    private var lightSensor: Sensor? = null
    private var cameraManager: CameraManager? = null
    private var isFlashOn = false

    private var originMarker: Marker? = null
    private var destinationMarker: Marker? = null

    private lateinit var mapView: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var editTextOrigen: EditText
    private lateinit var editTextDestino: EditText

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val SMS_PERMISSION_REQUEST_CODE = 100
    }

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var imageCapture: ImageCapture
    private lateinit var previewView: PreviewView
    private val handler = Handler(Looper.getMainLooper())
    private val captureInterval: Long = 5000 // 5 segundos

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pantalla_mapa)

        // Referencia a los elementos
        editTextOrigen = findViewById(R.id.editTextOrigen)
        editTextDestino = findViewById<EditText>(R.id.editTextDestino)
        val btnVerFotos = findViewById<Button>(R.id.btnVerFotos)
        val btnSos = findViewById<Button>(R.id.btnSos)
        imageBackground = findViewById(R.id.imageBackground)

        // Recibir el tipo de deporte seleccionado
        val deporteSeleccionado = intent.getStringExtra("deporte")

        // Cambiar el fondo dependiendo del deporte seleccionado
        if (deporteSeleccionado == "ciclismo") {
            imageBackground.setImageResource(R.drawable.ciclismo0)
        } else if (deporteSeleccionado == "caminata") {
            imageBackground.setImageResource(R.drawable.caminata0)
        }

        // Configurar el botón "Ver Fotos"
        btnVerFotos.setOnClickListener {
            val intent = Intent(this@PantallaMapaActivity, VerFotosActivity::class.java)
            startActivity(intent)
        }

        // Navegar a la pantalla de SOS
        btnSos.setOnClickListener {
            val intent = Intent(this@PantallaMapaActivity, SOSActivity::class.java)
            startActivity(intent)
        }

        // Inicializar SensorManager y CameraManager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

        // Obtener el sensor de luz
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        if (lightSensor == null) {
            Toast.makeText(this, "El dispositivo no tiene sensor de luz", Toast.LENGTH_SHORT).show()
        }

        // Inicializar FusedLocationProviderClient para obtener la ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        // Configurar osmdroid
        Configuration.getInstance().load(applicationContext, getSharedPreferences("osmdroid", Context.MODE_PRIVATE))

        // Inicializar MapView
        mapView = findViewById(R.id.mapView)
        mapView.setMultiTouchControls(true)

        // Centrar mapa en una ubicación por defecto
        val startPoint = GeoPoint(40.416775, -3.703790) // Madrid
        val mapController = mapView.controller
        mapController.setZoom(9.5)
        mapController.setCenter(startPoint)

        // Solicitar permisos de ubicación
        requestLocationPermission()
        requestSmsPermissions()

        previewView = findViewById(R.id.previewView)
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Solicitar permisos de cámara si no están concedidos
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        val btnBuscarRuta = findViewById<Button>(R.id.btnBuscarRuta)
        btnBuscarRuta.setOnClickListener {
            val address = editTextDestino.text.toString()
            if (address.isNotEmpty()) {
                searchAddress(address)
            } else {
                Toast.makeText(this, "Por favor ingrese un destino", Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onResume() {
        super.onResume()
        lightSensor?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
        mapView.onResume()
    }

    private fun addMarker(geoPoint: GeoPoint) {
        val marker = Marker(mapView)
        marker.position = geoPoint
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = "Marcador"
        mapView.overlays.add(marker)
        mapView.invalidate() // Para actualizar el mapa y mostrar el marcador
    }


    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
        mapView.onPause()
    }

    // Solicitar permisos de ubicación
    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            getLastKnownLocation()
        }
    }

    private fun requestSmsPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {

            // Solicitar permisos
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.SEND_SMS, Manifest.permission.READ_SMS),
                SMS_PERMISSION_REQUEST_CODE)
        } else {
            // Los permisos ya han sido concedidos
            Toast.makeText(this, "Permisos de SMS ya concedidos", Toast.LENGTH_SHORT).show()
        }
    }

    // Manejar el resultado de la solicitud de permisos
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLastKnownLocation()
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
            }
        }
        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permisos de SMS concedidos", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permisos de SMS denegados", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Obtener la última ubicación conocida
    private fun getLastKnownLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        // Mostrar coordenadas en un Toast
                        val latitude = location.latitude
                        val longitude = location.longitude
                        Toast.makeText(this, "Lat: $latitude, Lon: $longitude", Toast.LENGTH_LONG).show()
                        val userLocation = GeoPoint(location.latitude, location.longitude)
                        mapView.controller.setCenter(userLocation)
                        mapView.controller.setZoom(15.0)

                        // Añadir un marcador en la ubicación del usuario
                        val marker = Marker(mapView)
                        marker.position = userLocation
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        marker.title = "Tu ubicación"
                        mapView.overlays.add(marker)
                    } else {
                        Toast.makeText(this, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun setDefaultOriginMarker() {
        // Obtener la última ubicación conocida
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val currentLocation = GeoPoint(location.latitude, location.longitude)
                    // Colocar marcador de origen en la ubicación actual
                    if (originMarker == null) {
                        originMarker = Marker(mapView)
                        originMarker?.position = currentLocation
                        originMarker?.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        originMarker?.title = "YOUR LOCATION"
                        mapView.overlays.add(originMarker)
                    }
                    // Mover la cámara para centrar el mapa en la ubicación actual
                    mapView.controller.setCenter(currentLocation)
                    mapView.controller.setZoom(15.0)
                } else {
                    Toast.makeText(this, "No se pudo obtener la ubicación actual", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun searchAddress(address: String) {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocationName(address, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val location = addresses[0]
                val destinationGeoPoint = GeoPoint(location.latitude, location.longitude)

                // Eliminar el marcador de destino previo si existe
                destinationMarker?.let {
                    mapView.overlays.remove(it)
                }

                // Colocar marcador de destino
                destinationMarker = Marker(mapView)
                destinationMarker?.position = destinationGeoPoint
                destinationMarker?.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                destinationMarker?.title = address
                mapView.overlays.add(destinationMarker)

                // Mover la cámara y enfocar en el marcador de destino
                mapView.controller.setCenter(destinationGeoPoint)
                mapView.controller.setZoom(15.0)

                // Obtener la última ubicación conocida
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                        if (location != null) {
                            val currentLocation = GeoPoint(location.latitude, location.longitude)
                            // Dibujar la ruta desde la ubicación actual hasta el destino
                            drawRoute(currentLocation, destinationGeoPoint)
                        } else {
                            Toast.makeText(this, "No se pudo obtener la ubicación actual", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Dirección no encontrada", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error en la búsqueda de la dirección", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }


    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val lux = it.values[0] // Valor de la luz ambiental en lux

            // Cambia el valor 10 por el umbral de luz que desees
            if (lux < 10 && !isFlashOn) {
                toggleFlashlight(true)
            } else if (lux >= 10 && isFlashOn) {
                toggleFlashlight(false)
            }
        }
    }

    private fun toggleFlashlight(turnOn: Boolean) {
        val cameraId = cameraManager?.cameraIdList?.find {
            cameraManager?.getCameraCharacteristics(it)
                ?.get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        }

        cameraId?.let {
            try {
                cameraManager?.setTorchMode(it, turnOn)
                isFlashOn = turnOn
                Toast.makeText(this, if (turnOn) "Linterna encendida" else "Linterna apagada", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Error al cambiar el estado de la linterna", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No se necesita manejar cambios en la precisión para el sensor de luz
    }

    override fun onLocationChanged(location: Location) {
        // Update the user location on the map
        val userLocation = GeoPoint(location.latitude, location.longitude)
        mapView.controller.setCenter(userLocation)

        // Update the marker position or add a new marker if necessary
        val marker = Marker(mapView)
        marker.position = userLocation
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = "Tu ubicación actual"
        mapView.overlays.add(marker)
        mapView.invalidate() // Refresh the map
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

    private fun drawRoute(from: GeoPoint, to: GeoPoint) {
        // URL de la API de OSRM para obtener la ruta
        val url = "https://router.project-osrm.org/route/v1/driving/${from.longitude},${from.latitude};${to.longitude},${to.latitude}?overview=full&geometries=geojson"
        // Realizamos la solicitud
        val request = Request.Builder().url(url).build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Ruta", "Error en la solicitud: ${e.message}")
                runOnUiThread {
                    Toast.makeText(this@PantallaMapaActivity, "Error al obtener la ruta: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }


            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    runOnUiThread {
                        // Mostrar mensaje si la respuesta no es exitosa
                        Toast.makeText(this@PantallaMapaActivity, "Error en la respuesta de la ruta", Toast.LENGTH_SHORT).show()
                    }
                    return
                }

                // Obtener la respuesta de la API
                response.body?.string()?.let { responseData ->
                    try {
                        // Parseamos el JSON de la respuesta
                        val json = JSONObject(responseData)
                        val routes = json.optJSONArray("routes")

                        // Verificamos si hay rutas en la respuesta
                        if (routes != null && routes.length() > 0) {
                            // Obtenemos la geometría de la primera ruta
                            val geometry = routes.getJSONObject(0).getJSONObject("geometry")
                            val coordinates = geometry.getJSONArray("coordinates")
                            val polylinePoints = mutableListOf<GeoPoint>()

                            // Parseamos las coordenadas y las agregamos a la lista de puntos
                            for (i in 0 until coordinates.length()) {
                                val point = coordinates.getJSONArray(i)
                                val lon = point.getDouble(0)
                                val lat = point.getDouble(1)
                                polylinePoints.add(GeoPoint(lat, lon))
                            }

                            // Creamos la Polyline y la agregamos al mapa
                            runOnUiThread {
                                val polyline = Polyline()
                                polyline.setPoints(polylinePoints)
                                mapView.overlays.add(polyline)
                                mapView.invalidate() // Actualizar el mapa para mostrar la ruta
                            }
                        } else {
                            // Mostrar un mensaje si no se encuentra una ruta
                            runOnUiThread {
                                Toast.makeText(this@PantallaMapaActivity, "No se encontró una ruta", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        // Mostrar error si ocurre una excepción al procesar la respuesta
                        e.printStackTrace()
                        runOnUiThread {
                            Toast.makeText(this@PantallaMapaActivity, "Error al procesar la respuesta de la ruta", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
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



