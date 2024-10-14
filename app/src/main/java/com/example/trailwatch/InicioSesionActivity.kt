package com.example.trailwatch

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
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
        setupOnClickListeners()
    }

    private fun initViews() {
        emailEditText = findViewById(R.id.InicioSesionCorreo)
        passwordEditText = findViewById(R.id.InicioSesionContrasenha)
        continuarButton = findViewById(R.id.btnContinuar)
        switchMantenermeConectado = findViewById(R.id.switchMantenermeConectado)
    }

    private fun setupOnClickListeners() {
        continuarButton.setOnClickListener {
            handleLogin()
        }
    }

    private fun handleLogin() {
        val correoIngresado = emailEditText.text.toString().trim()
        val contraseñaIngresada = passwordEditText.text.toString()

        // Leer usuarios desde el archivo JSON
        val usuarios = UsuarioUtils.leerUsuariosDesdeArchivo(this)

        // Buscar usuario por correo
        val usuarioEncontrado = usuarios.firstOrNull { it.correo == correoIngresado }

        if (usuarioEncontrado != null && usuarioEncontrado.contraseña == contraseñaIngresada) {
            // Credenciales correctas
            val mantenerConectado = switchMantenermeConectado.isChecked
            saveUserPreferences(mantenerConectado, usuarioEncontrado.username)
            navigateToActividadDeportes()
        } else {
            // Credenciales incorrectas
            Toast.makeText(this, "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveUserPreferences(mantenerConectado: Boolean, username: String) {
        val sharedPreferences = getSharedPreferences("MiPreferencia", MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putBoolean("mantenerConectado", mantenerConectado)
            putString("username", username)
            apply()
        }
    }

    private fun navigateToActividadDeportes() {
        val intent = Intent(this, ActividadDeportesActivity::class.java)
        startActivity(intent)
        finish()
    }
}
