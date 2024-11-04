package com.example.trailwatch

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import de.hdodenhof.circleimageview.CircleImageView
import java.io.ByteArrayOutputStream

class PerfilActivity : AppCompatActivity() {

    private lateinit var nombreTextView: TextView
    private lateinit var apellidoTextView: TextView
    private lateinit var rhTextView: TextView
    private lateinit var pesoEditText: EditText
    private lateinit var edadEditText: EditText
    private lateinit var estaturaEditText: EditText
    private lateinit var enfermedadEditText: EditText
    private lateinit var correoEditText: EditText
    private lateinit var btnGuardar: Button
    private lateinit var btnEditarPeso: ImageButton
    private lateinit var btnEditarEdad: ImageButton
    private lateinit var btnEditarEstatura: ImageButton
    private lateinit var btnEditarEnfermedad: ImageButton
    private lateinit var btnEditarCorreo: ImageButton
    private lateinit var profileImageView: CircleImageView
    private lateinit var btnCerrarSesion: Button

    private lateinit var usuarioActual: Usuario

    private val CAMERA_REQUEST_CODE = 1
    private val GALLERY_REQUEST_CODE = 2
    private val PERMISSION_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        // Inicializar vistas
        nombreTextView = findViewById(R.id.textViewNombre)
        apellidoTextView = findViewById(R.id.textViewApellido)
        rhTextView = findViewById(R.id.textViewRh)
        pesoEditText = findViewById(R.id.editTextPeso)
        edadEditText = findViewById(R.id.editTextEdad)
        estaturaEditText = findViewById(R.id.editTextEstatura)
        enfermedadEditText = findViewById(R.id.editTextEnfermedad)
        correoEditText = findViewById(R.id.editTextCorreo)
        btnGuardar = findViewById(R.id.btnGuardar)
        btnEditarPeso = findViewById(R.id.btnEditarPeso)
        btnEditarEdad = findViewById(R.id.btnEditarEdad)
        btnEditarEstatura = findViewById(R.id.btnEditarEstatura)
        btnEditarEnfermedad = findViewById(R.id.btnEditarEnfermedad)
        btnEditarCorreo = findViewById(R.id.btnEditarCorreo)
        profileImageView = findViewById(R.id.profile_image)
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion)

        // Deshabilitar campos editables inicialmente
        pesoEditText.isEnabled = false
        edadEditText.isEnabled = false
        estaturaEditText.isEnabled = false
        enfermedadEditText.isEnabled = false
        correoEditText.isEnabled = false

        cargarDatosUsuario()

        // Configurar botones de edición
        configurarBotonesEdicion()

        btnGuardar.setOnClickListener {
            guardarCambios()
        }

        // Configurar clic en la imagen de perfil
        profileImageView.setOnClickListener {
            mostrarOpcionesImagen()
        }

        // Configurar botón de cierre de sesión
        btnCerrarSesion.setOnClickListener {
            cerrarSesion()
        }
    }

    private fun cargarDatosUsuario() {
        val sharedPreferences = getSharedPreferences("MiPreferencia", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", null)

        if (username != null) {
            val usuarios = UsuarioUtils.leerUsuariosDesdeArchivo(this)
            val usuarioEncontrado = usuarios.firstOrNull { it.username == username }

            if (usuarioEncontrado != null) {
                usuarioActual = usuarioEncontrado

                // Mostrar datos en las vistas
                nombreTextView.text = usuarioActual.nombre
                apellidoTextView.text = usuarioActual.apellido
                rhTextView.text = usuarioActual.rh
                pesoEditText.setText(usuarioActual.peso?.toString() ?: "")
                edadEditText.setText(usuarioActual.edad?.toString() ?: "")
                estaturaEditText.setText(usuarioActual.estatura?.toString() ?: "")
                enfermedadEditText.setText(usuarioActual.enfermedad ?: "")
                correoEditText.setText(usuarioActual.correo)

                // Mostrar imagen de perfil si existe
                if (usuarioActual.imagenPerfil != null) {
                    val bytes = Base64.decode(usuarioActual.imagenPerfil, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    profileImageView.setImageBitmap(bitmap)
                } else {
                    // Si no hay imagen, mostrar imagen predeterminada
                    profileImageView.setImageResource(R.drawable.ic_profile)
                }
            } else {
                Toast.makeText(this, "Usuario no encontrado", Toast.LENGTH_SHORT).show()
                // Redirigir al usuario a la pantalla de inicio de sesión
                val intent = Intent(this, InicioSesionActivity::class.java)
                startActivity(intent)
                finish()
            }
        } else {
            Toast.makeText(this, "Nombre de usuario no disponible", Toast.LENGTH_SHORT).show()
            // Redirigir al usuario a la pantalla de inicio de sesión
            val intent = Intent(this, InicioSesionActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun configurarBotonesEdicion() {
        btnEditarPeso.setOnClickListener {
            pesoEditText.isEnabled = true
        }

        btnEditarEdad.setOnClickListener {
            edadEditText.isEnabled = true
        }

        btnEditarEstatura.setOnClickListener {
            estaturaEditText.isEnabled = true
        }

        btnEditarEnfermedad.setOnClickListener {
            enfermedadEditText.isEnabled = true
        }

        btnEditarCorreo.setOnClickListener {
            correoEditText.isEnabled = true
        }
    }

    private fun guardarCambios() {
        // Actualizar datos del usuario
        usuarioActual.peso = pesoEditText.text.toString().toDoubleOrNull()
        usuarioActual.edad = edadEditText.text.toString().toIntOrNull()
        usuarioActual.estatura = estaturaEditText.text.toString().toDoubleOrNull()
        usuarioActual.enfermedad = enfermedadEditText.text.toString()
        usuarioActual.correo = correoEditText.text.toString()

        // Guardar cambios en el archivo JSON
        val usuarios = UsuarioUtils.leerUsuariosDesdeArchivo(this)
        val index = usuarios.indexOfFirst { it.username == usuarioActual.username }
        if (index != -1) {
            usuarios[index] = usuarioActual
            UsuarioUtils.guardarUsuariosEnArchivo(this, usuarios)
            Toast.makeText(this, "Cambios guardados", Toast.LENGTH_SHORT).show()
        }

        // Deshabilitar campos nuevamente
        pesoEditText.isEnabled = false
        edadEditText.isEnabled = false
        estaturaEditText.isEnabled = false
        enfermedadEditText.isEnabled = false
        correoEditText.isEnabled = false
    }

    // Mostrar opciones para seleccionar imagen
    private fun mostrarOpcionesImagen() {
        val options = arrayOf("Tomar foto", "Seleccionar de galería", "Cancelar")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Cambiar foto de perfil")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> {
                    // Tomar foto con la cámara
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.CAMERA),
                            PERMISSION_REQUEST_CODE
                        )
                    } else {
                        abrirCamara()
                    }
                }
                1 -> {
                    // Seleccionar de la galería
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                            PERMISSION_REQUEST_CODE
                        )
                    } else {
                        abrirGaleria()
                    }
                }
                2 -> {
                    dialog.dismiss()
                }
            }
        }
        builder.show()
    }

    // Abrir la cámara
    private fun abrirCamara() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    // Abrir la galería
    private fun abrirGaleria() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST_CODE -> {
                    try {
                        val photo = data?.extras?.get("data") as? Bitmap
                        if (photo != null) {
                            profileImageView.setImageBitmap(photo)
                            // Guardar la imagen en el usuario actual
                            guardarImagenEnUsuario(photo)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(
                            this,
                            "Error al procesar la imagen de la cámara",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                GALLERY_REQUEST_CODE -> {
                    try {
                        val imageUri = data?.data
                        if (imageUri != null) {
                            val inputStream = contentResolver.openInputStream(imageUri)
                            val bitmap = BitmapFactory.decodeStream(inputStream)
                            profileImageView.setImageBitmap(bitmap)
                            // Guardar la imagen en el usuario actual
                            guardarImagenEnUsuario(bitmap)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(
                            this,
                            "Error al procesar la imagen de la galería",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun guardarImagenEnUsuario(bitmap: Bitmap) {
        try {
            // Convertir el bitmap a Base64
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            val imageString = Base64.encodeToString(byteArray, Base64.DEFAULT)

            usuarioActual.imagenPerfil = imageString

            // Guardar cambios en el archivo JSON
            val usuarios = UsuarioUtils.leerUsuariosDesdeArchivo(this)
            val index = usuarios.indexOfFirst { it.username == usuarioActual.username }
            if (index != -1) {
                usuarios[index] = usuarioActual
                UsuarioUtils.guardarUsuariosEnArchivo(this, usuarios)
                Toast.makeText(this, "Imagen de perfil actualizada", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al guardar la imagen", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty()) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                mostrarOpcionesImagen()
            } else {
                Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun cerrarSesion() {
        val sharedPreferences = getSharedPreferences("MiPreferencia", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove("username")
        editor.apply()

        val intent = Intent(this, InicioSesionActivity::class.java)
        startActivity(intent)
        finish()
    }
}
