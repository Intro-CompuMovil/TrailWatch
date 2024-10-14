package com.example.trailwatch

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Copia el archivo JSON si no existe
        copiarArchivoJSONSiNoExiste()

        // Habilita el modo edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_main)

        // Ajusta el padding para los system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                systemBarsInsets.left,
                systemBarsInsets.top,
                systemBarsInsets.right,
                systemBarsInsets.bottom
            )
            WindowInsetsCompat.CONSUMED
        }

        setUpButton()
        checkUserSession()
    }

    private fun setUpButton() {
        val btnInicioSesion = findViewById<Button>(R.id.btnInicioSesion)
        val btnRegistro = findViewById<Button>(R.id.btnRegistro)

        btnInicioSesion.setOnClickListener {
            startActivity(Intent(this@MainActivity, InicioSesionActivity::class.java))
        }

        btnRegistro.setOnClickListener {
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

    private fun copiarArchivoJSONSiNoExiste() {
        val file = File(filesDir, "usuarios.json")
        if (!file.exists()) {
            try {
                assets.open("usuarios.json").use { inputStream ->
                    file.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                // Mostrar un mensaje indicando que el archivo fue copiado
                Toast.makeText(this, "Archivo JSON copiado exitosamente", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                e.printStackTrace()
                // Manejar el error según sea necesario
                Toast.makeText(this, "Error al copiar el archivo JSON", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Indicar que el archivo ya existe
            Toast.makeText(this, "El archivo JSON ya existe", Toast.LENGTH_SHORT).show()
        }
    }
}
