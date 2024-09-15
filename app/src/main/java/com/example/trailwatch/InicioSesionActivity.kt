package com.example.trailwatch

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity

class InicioSesionActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var continuarButton: Button
    private lateinit var switchMantenermeConectado: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inicio_sesion)

        initViews()
        setupTextWatchers()
        setupOnClickListeners()
    }

    private fun initViews() {
        emailEditText = findViewById(R.id.InicioSesionCorreo)
        passwordEditText = findViewById(R.id.InicioSesionContrasenha)
        continuarButton = findViewById(R.id.btnContinuar)
        switchMantenermeConectado = findViewById(R.id.switchMantenermeConectado)

        // Desactivar botón al inicio
        continuarButton.isEnabled = false
    }

    private fun setupTextWatchers() {
        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                checkFieldsForEmptyValues()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        emailEditText.addTextChangedListener(textWatcher)
        passwordEditText.addTextChangedListener(textWatcher)
    }

    private fun checkFieldsForEmptyValues() {
        continuarButton.isEnabled = emailEditText.text.isNotEmpty() && passwordEditText.text.isNotEmpty()
    }

    private fun setupOnClickListeners() {
        continuarButton.setOnClickListener {
            // Navegar a la pantalla con las opciones de ciclismo y caminata
            navigateToActividadDeportes()
        }

        findViewById<Button>(R.id.btnContinuar).setOnClickListener {
            handleLogin()
        }
    }

    private fun navigateToActividadDeportes() {
        val intent = Intent(this@InicioSesionActivity, ActividadDeportesActivity::class.java)
        startActivity(intent)
    }

    private fun handleLogin() {
        val mantenerConectado = switchMantenermeConectado.isChecked
        saveUserPreferences(mantenerConectado)
        navigateToPrincipalActivity(mantenerConectado)
    }

    private fun saveUserPreferences(mantenerConectado: Boolean) {
        val sharedPreferences = getSharedPreferences("MiPreferencia", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("mantenerConectado", mantenerConectado)
        editor.putString("usuario", "nombre_usuario") // Guarda los datos del usuario
        editor.apply()
    }

    private fun navigateToPrincipalActivity(mantenerConectado: Boolean) {
        val intent = Intent(this, ActividadDeportesActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)

        if (!mantenerConectado) {
            finish()
        }
    }
}
