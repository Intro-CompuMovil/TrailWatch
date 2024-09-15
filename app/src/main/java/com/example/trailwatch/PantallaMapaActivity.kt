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
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class PantallaMapaActivity : AppCompatActivity(), SensorEventListener,LocationListener {

    private lateinit var imageBackground: ImageView
    private lateinit var sensorManager: SensorManager
    private var lightSensor: Sensor? = null
    private var cameraManager: CameraManager? = null
    private var isFlashOn = false

    private lateinit var mapView: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var editTextOrigen: EditText

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pantalla_mapa)

        // Referencia a los elementos
        editTextOrigen = findViewById(R.id.editTextOrigen)
        val editTextDestino = findViewById<EditText>(R.id.editTextDestino)
        val btnPaginaInicio = findViewById<Button>(R.id.btnPaginaInicio)
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

        // Configurar el botón "PÁGINA DE INICIO"
        btnPaginaInicio.setOnClickListener {
            val intent = Intent(this@PantallaMapaActivity, MainActivity::class.java)
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

        // Inicializar FusedLocationProviderClient para obtener la ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Configurar osmdroid
        Configuration.getInstance().load(applicationContext, getSharedPreferences("osmdroid", Context.MODE_PRIVATE))

        // Inicializar MapView
        mapView = findViewById(R.id.mapView)
        mapView.setBuiltInZoomControls(true)
        mapView.setMultiTouchControls(true)

        // Centrar mapa en una ubicación por defecto
        val startPoint = GeoPoint(40.416775, -3.703790) // Madrid
        val mapController = mapView.controller
        mapController.setZoom(9.5)
        mapController.setCenter(startPoint)

        // Solicitar permisos de ubicación
        requestLocationPermission()


    }

    override fun onResume() {
        super.onResume()
        lightSensor?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
        mapView.onResume()
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

    override fun onLocationChanged(p0: Location) {
        TODO("Not yet implemented")
    }


}
