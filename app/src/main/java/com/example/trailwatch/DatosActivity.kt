package com.example.trailwatch

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class DatosActivity : AppCompatActivity() {

    private lateinit var pesoEditText: EditText
    private lateinit var edadEditText: EditText
    private lateinit var estaturaEditText: EditText
    private lateinit var enfermedadEditText: EditText
    private lateinit var enfermedadCounterTextView: TextView
    private lateinit var errorEdadTextView: TextView
    private lateinit var errorEstaturaTextView: TextView
    private lateinit var progresoDatos: ProgressBar
    private lateinit var imageBackground: ImageView
    private lateinit var btnAtras: Button
    private lateinit var btnSiguiente: Button
    private lateinit var radioGroupEnfermedad: RadioGroup
    private lateinit var radioSi: RadioButton
    private lateinit var radioNo: RadioButton
    private lateinit var spinnerRh: Spinner

    private var grupoSeleccionado = false

    // Variables para almacenar datos recibidos de RegistroActivity
    private var nombre: String? = null
    private var apellido: String? = null
    private var correo: String? = null
    private var username: String? = null
    private var contraseña: String? = null

    companion object {
        private const val PICK_CONTACT_REQUEST = 1
        private const val PERMISSIONS_REQUEST_READ_CONTACTS = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Establecer el contenido de la vista
        setContentView(R.layout.activity_datos)

        // Referencias a los campos
        pesoEditText = findViewById(R.id.editTextPeso)
        edadEditText = findViewById(R.id.editTextEdad)
        estaturaEditText = findViewById(R.id.editTextEstatura)
        enfermedadEditText = findViewById(R.id.editTextEnfermedad)
        enfermedadCounterTextView = findViewById(R.id.enfermedadCounterTextView)
        errorEdadTextView = findViewById(R.id.errorEdadTextView)
        errorEstaturaTextView = findViewById(R.id.errorEstaturaTextView)
        progresoDatos = findViewById(R.id.progresoDatos)
        imageBackground = findViewById(R.id.imageBackground)
        btnAtras = findViewById(R.id.btnAtras)
        btnSiguiente = findViewById(R.id.btnSiguiente)
        radioGroupEnfermedad = findViewById(R.id.radioGroupEnfermedad)
        radioSi = findViewById(R.id.radioSi)
        radioNo = findViewById(R.id.radioNo)
        spinnerRh = findViewById(R.id.spinnerGrupoSanguineo)

        // Desactivar el botón Siguiente inicialmente
        btnSiguiente.isEnabled = false

        // Obtener datos pasados desde RegistroActivity
        nombre = intent.getStringExtra("nombre")
        apellido = intent.getStringExtra("apellido")
        correo = intent.getStringExtra("correo")
        username = intent.getStringExtra("username")
        contraseña = intent.getStringExtra("contraseña")

        // Configurar el Spinner
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.grupos_sanguineos,
            android.R.layout.simple_spinner_item // Puedes personalizar el layout
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRh.adapter = adapter

        // Detectar selección en el Spinner
        spinnerRh.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                grupoSeleccionado = position != 0 // Verifica que no sea "ELIGE TU GRUPO"
                updateProgressBar()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                grupoSeleccionado = false
                updateProgressBar()
            }
        }

        // Configurar el comportamiento de los botones de radio
        radioGroupEnfermedad.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioSi -> {
                    enfermedadEditText.visibility = View.VISIBLE
                    enfermedadCounterTextView.visibility = View.VISIBLE
                }
                R.id.radioNo -> {
                    enfermedadEditText.visibility = View.GONE
                    enfermedadCounterTextView.visibility = View.GONE
                    enfermedadEditText.text.clear()
                }
            }
        }

        // Limitar el campo de texto de la enfermedad a 200 caracteres
        enfermedadEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val remainingChars = 200 - (s?.length ?: 0)
                enfermedadCounterTextView.text = "Te quedan $remainingChars caracteres"
                if (remainingChars < 0) {
                    enfermedadEditText.error = "No puedes escribir más de 200 caracteres"
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Configurar el botón "Atrás" para volver a la pantalla anterior
        btnAtras.setOnClickListener {
            finish()
        }

        // Configurar el botón "Siguiente" para ir a la siguiente actividad o seleccionar contacto
        setupSelectContactButton()

        // Validaciones y barra de progreso
        validarEdad()
        validarEstatura()
        updateProgressBar()
    }

    // Validar la edad en tiempo real
    private fun validarEdad() {
        edadEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val edadTexto = s.toString()
                if (!edadTexto.matches(Regex("\\d+"))) {
                    errorEdadTextView.text = "VALORES NO VÁLIDOS!"
                    errorEdadTextView.setTextColor(Color.RED)
                } else {
                    errorEdadTextView.text = ""
                }
                updateProgressBar()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    // Validar la estatura en tiempo real
    private fun validarEstatura() {
        estaturaEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val estaturaTexto = s.toString()
                if (!estaturaTexto.matches(Regex("\\d+"))) {
                    errorEstaturaTextView.text = "VALORES NO VÁLIDOS!"
                    errorEstaturaTextView.setTextColor(Color.RED)
                } else {
                    errorEstaturaTextView.text = ""
                }
                updateProgressBar()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    // Actualizar la barra de progreso y activar el botón de continuar si se cumplen las condiciones
    private fun updateProgressBar() {
        val filledFields = listOf(pesoEditText, edadEditText, estaturaEditText)
            .count { it.text.isNotEmpty() } + if (grupoSeleccionado) 1 else 0

        // Calcular el progreso
        val progress = (filledFields / 4.0) * 100 // 4 campos necesarios: peso, edad, estatura, y grupo sanguíneo

        progresoDatos.progress = progress.toInt()

        // Activar el botón Siguiente si todos los campos están llenos
        btnSiguiente.isEnabled = progress == 100.0
    }

    // Configurar botón para seleccionar contacto de emergencia
    private fun setupSelectContactButton() {
        btnSiguiente.setOnClickListener {
            if (btnSiguiente.isEnabled) {
                // Guardar los datos del usuario antes de abrir los contactos
                guardarUsuario()
                requestContactPermission()
            }
        }
    }

    private fun requestContactPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.READ_CONTACTS),
                PERMISSIONS_REQUEST_READ_CONTACTS)
        } else {
            showContactPicker()
        }
    }

    private fun showContactPicker() {
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
        startActivityForResult(intent, PICK_CONTACT_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_CONTACT_REQUEST && resultCode == RESULT_OK) {
            val contactUri: Uri = data?.data ?: return
            handleSelectedContact(contactUri)
        } else {
            // Si el usuario cancela la selección de contacto, ir a la siguiente actividad
            navigateToActividadDeportes()
        }
    }

    private fun handleSelectedContact(contactUri: Uri) {
        val cursor = contentResolver.query(contactUri, null, null, null, null)
        if (cursor != null && cursor.moveToFirst()) {
            val contactIdIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID)
            val nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
            val hasPhoneNumberIndex = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)

            if (contactIdIndex != -1 && hasPhoneNumberIndex != -1 && nameIndex != -1) {
                val contactId = cursor.getString(contactIdIndex)
                val contactName = cursor.getString(nameIndex)
                val hasPhoneNumber = cursor.getInt(hasPhoneNumberIndex)

                if (hasPhoneNumber > 0) {
                    fetchPhoneNumber(contactId, contactName)
                }
            }
            cursor.close()
        }
    }

    private fun fetchPhoneNumber(contactId: String, contactName: String) {
        val phoneCursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
            arrayOf(contactId),
            null
        )

        if (phoneCursor != null && phoneCursor.moveToFirst()) {
            val phoneNumberIndex = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            if (phoneNumberIndex != -1) {
                val phoneNumber = phoneCursor.getString(phoneNumberIndex)
                saveEmergencyContact(phoneNumber, contactName)
            }
            phoneCursor.close()
        }
    }

    private fun saveEmergencyContact(phoneNumber: String, contactName: String) {
        val sharedPreferences = getSharedPreferences("EmergencyContact", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("contact_number", phoneNumber)
        editor.putString("contact_name", contactName)
        editor.apply()

        Toast.makeText(this, "Contacto de emergencia guardado: $contactName", Toast.LENGTH_SHORT).show()

        navigateToActividadDeportes()
    }

    private fun navigateToActividadDeportes() {
        val intent = Intent(this, ActividadDeportesActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun guardarUsuario() {
        // Obtener los datos ingresados en esta actividad
        val peso = pesoEditText.text.toString().toDoubleOrNull()
        val edad = edadEditText.text.toString().toIntOrNull()
        val estatura = estaturaEditText.text.toString().toDoubleOrNull()
        val rh = spinnerRh.selectedItem.toString()
        val tieneEnfermedad = radioSi.isChecked
        val enfermedad = if (tieneEnfermedad) enfermedadEditText.text.toString() else null

        // Crear nuevo usuario
        val nuevoUsuario = Usuario(
            username = username ?: "",
            nombre = nombre ?: "",
            apellido = apellido ?: "",
            correo = correo ?: "",
            contraseña = contraseña ?: "",
            peso = peso,
            edad = edad,
            estatura = estatura,
            rh = rh,
            enfermedad = enfermedad
        )

        // Leer usuarios existentes
        val usuarios = UsuarioUtils.leerUsuariosDesdeArchivo(this)

        // Verificar si el usuario ya existe
        val usuarioExistente = usuarios.firstOrNull { it.username == nuevoUsuario.username || it.correo == nuevoUsuario.correo }

        if (usuarioExistente != null) {
            Toast.makeText(this, "El usuario o correo ya está registrado", Toast.LENGTH_SHORT).show()
            return
        }

        // Agregar el nuevo usuario a la lista
        usuarios.add(nuevoUsuario)

        // Guardar la lista actualizada en el archivo JSON
        UsuarioUtils.guardarUsuariosEnArchivo(this, usuarios)

        // Guardar el nombre de usuario en SharedPreferences
        val sharedPreferences = getSharedPreferences("MiPreferencia", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("username", nuevoUsuario.username)
        editor.apply()

        Toast.makeText(this, "Datos guardados", Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_REQUEST_READ_CONTACTS -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showContactPicker()
                } else {
                    Toast.makeText(this, "Permiso para leer contactos denegado", Toast.LENGTH_SHORT).show()
                    // Si el permiso es denegado, ir a la siguiente actividad
                    navigateToActividadDeportes()
                }
            }
        }
    }
}
