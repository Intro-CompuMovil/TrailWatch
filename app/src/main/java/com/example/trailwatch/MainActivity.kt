package com.example.trailwatch

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setUpButton()
        checkUserSession()
    }

    private fun setUpButton() {
        val btnInicioSesion = findViewById<Button>(R.id.btnInicioSesion)
        val btnRegistro = findViewById<Button>(R.id.btnRegistro)

        btnInicioSesion.setOnClickListener() {
            startActivity(Intent(this@MainActivity, InicioSesionActivity::class.java))
        }

        btnRegistro.setOnClickListener() {
            startActivity(Intent(this@MainActivity, RegistroActivity::class.java))
        }

    }

    private fun checkUserSession() {
        val sharedPreferences = getSharedPreferences("MiPreferencia", MODE_PRIVATE)
        val mantenerConectado = sharedPreferences.getBoolean("mantenerConectado", false)

        if (mantenerConectado) {
            val intent = Intent(this, ActividadDeportesActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
        }
    }
}