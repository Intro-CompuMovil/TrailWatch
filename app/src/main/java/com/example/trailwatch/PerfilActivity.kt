package com.example.trailwatch

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import de.hdodenhof.circleimageview.CircleImageView

class PerfilActivity : AppCompatActivity() {

    private lateinit var profileImageView: CircleImageView
    private lateinit var btnCambiarFoto: Button
    private lateinit var editTextUsuario: EditText
    private lateinit var editTextCorreo: EditText
    private lateinit var editTextEdad: EditText
    private lateinit var editTextEstatura: EditText
    private lateinit var btnGuardar: Button

    private val CAMERA_REQUEST_CODE = 1
    private val STORAGE_REQUEST_CODE = 2

    private var photo: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        profileImageView = findViewById(R.id.profile_image)
        btnCambiarFoto = findViewById(R.id.btnCambiarFoto)
        editTextUsuario = findViewById(R.id.editTextUsuario)
        editTextCorreo = findViewById(R.id.editTextCorreo)
        editTextEdad = findViewById(R.id.editTextEdad)
        editTextEstatura = findViewById(R.id.editTextEstatura)
        btnGuardar = findViewById(R.id.btnGuardar)

        btnCambiarFoto.setOnClickListener {
            requestCameraPermission()
        }

        btnGuardar.setOnClickListener {
            saveUserData()
        }
    }

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_REQUEST_CODE
            )
        } else {
            openCamera()
        }
    }

    private fun openCamera() {
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
        } else {
            Toast.makeText(this, "No hay cámara disponible", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                STORAGE_REQUEST_CODE
            )
        } else {
            saveImageToStorage()
        }
    }

    private fun saveImageToStorage() {
        // Implementa aquí la lógica para guardar la imagen en el almacenamiento
        // Por ejemplo, puedes guardar la imagen en el almacenamiento interno de la app
        Toast.makeText(this, "Imagen guardada", Toast.LENGTH_SHORT).show()
    }

    private fun saveUserData() {
        // Lógica para guardar los datos del usuario como nombre de usuario, correo, edad, etc.
        Toast.makeText(this, "Cambios guardados", Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
                }
            }
            STORAGE_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    saveImageToStorage()
                } else {
                    Toast.makeText(this, "Permiso de almacenamiento denegado", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            photo = data?.extras?.get("data") as? Bitmap
            if (photo != null) {
                profileImageView.setImageBitmap(photo)
                requestStoragePermission()
            }
        }
    }
}
