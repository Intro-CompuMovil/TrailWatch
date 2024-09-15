package com.example.trailwatch

import android.Manifest
import android.app.ActivityOptions
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
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class DatosActivity : AppCompatActivity() {

    private lateinit var pesoEditText: EditText
    private lateinit var edadEditText: EditText
    private lateinit var estaturaEditText: EditText
    private lateinit var errorEdadTextView: TextView
    private lateinit var errorEstaturaTextView: TextView
    private lateinit var progresoDatos: ProgressBar
    private lateinit var imageBackground: ImageView
    private lateinit var btnAtras: Button
    private lateinit var btnSiguiente: Button
    private lateinit var seleccionContactoTextView: TextView // Nueva referencia

    companion object {
        private const val PICK_CONTACT_REQUEST = 1
        private const val PERMISSIONS_REQUEST_READ_CONTACTS = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_datos)

        // Referencias a los campos
        pesoEditText = findViewById(R.id.editTextPeso)
        edadEditText = findViewById(R.id.editTextEdad)
        estaturaEditText = findViewById(R.id.editTextEstatura)
        errorEdadTextView = findViewById(R.id.errorEdadTextView)
        errorEstaturaTextView = findViewById(R.id.errorEstaturaTextView)
        progresoDatos = findViewById(R.id.progresoDatos)
        imageBackground = findViewById(R.id.imageBackground)
        btnAtras = findViewById(R.id.btnAtras)
        btnSiguiente = findViewById(R.id.btnSiguiente)
        seleccionContactoTextView = findViewById(R.id.textViewSeleccionContacto) // Inicializa el nuevo TextView

        // Desactivar el botón Siguiente inicialmente
        btnSiguiente.isEnabled = false

        // Recibir el tipo de deporte seleccionado (ciclismo o caminata)
        val deporteSeleccionado = intent.getStringExtra("deporte")

        // Cambiar el fondo dependiendo del deporte seleccionado
        if (deporteSeleccionado == "ciclismo") {
            imageBackground.setImageResource(R.drawable.ciclismo0)
        } else if (deporteSeleccionado == "caminata") {
            imageBackground.setImageResource(R.drawable.caminata0)
        }

        // Configurar el botón "Atrás" para volver a la pantalla anterior
        btnAtras.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Configurar el botón "Siguiente" para ir a la siguiente actividad con animación
        btnSiguiente.setOnClickListener {
            val intent = Intent(this@DatosActivity, AnyEnfermedad::class.java)
            val options = ActivityOptions.makeCustomAnimation(this, R.anim.fade_in, R.anim.fade_out)
            startActivity(intent, options.toBundle())
        }

        // Configurar el botón para seleccionar el contacto de emergencia
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

    // Actualizar la barra de progreso según los campos llenados
    private fun updateProgressBar() {
        val filledFields = listOf(pesoEditText, edadEditText, estaturaEditText)
            .count { it.text.isNotEmpty() }

        val progress = (filledFields / 3.0) * 100
        progresoDatos.progress = progress.toInt()

        // Referencia al ImageView del checkmark
        val checkmarkImageView: ImageView = findViewById(R.id.checkmarkImageView)

        // Si los datos están completos, mostrar animación de éxito
        if (progress == 100.0) {
            checkmarkImageView.visibility = View.VISIBLE // Mostrar el icono de éxito
            val bounceAnimation = AnimationUtils.loadAnimation(this, R.anim.bounce)
            checkmarkImageView.startAnimation(bounceAnimation) // Ejecutar animación de rebote

            // Mostrar el mensaje para elegir un contacto de emergencia
            seleccionContactoTextView.visibility = View.VISIBLE
        } else {
            checkmarkImageView.visibility = View.GONE // Ocultar el icono si no está al 100%
            seleccionContactoTextView.visibility = View.GONE // Ocultar el mensaje si no está al 100%
        }

        // Activar el botón Siguiente solo si todos los campos están llenos
        btnSiguiente.isEnabled = filledFields == 3
    }

    private fun setupSelectContactButton() {
        val btnSelectContact: Button = findViewById(R.id.btnSiguiente)
        btnSelectContact.setOnClickListener {
            requestContactPermission()
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

        val intent = Intent(this, ActividadDeportesActivity::class.java)
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_REQUEST_READ_CONTACTS -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showContactPicker()
                } else {
                    Toast.makeText(this, "Permiso para leer contactos denegado", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
