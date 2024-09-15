package com.example.trailwatch

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class ActividadDeportesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deportes)

        val btnCiclismo = findViewById<ImageButton>(R.id.btnCiclismo)
        val btnCaminata = findViewById<ImageButton>(R.id.btnCaminata)
        val btnSalir = findViewById<Button>(R.id.btnSalir)

        // Cuando el usuario selecciona ciclismo
        btnCiclismo.setOnClickListener {
            val intent = Intent(this@ActividadDeportesActivity, PantallaMapaActivity::class.java)
            intent.putExtra("deporte", "ciclismo")  // Pasar "ciclismo" como deporte seleccionado
            startActivity(intent)
        }

        // Cuando el usuario selecciona caminata
        btnCaminata.setOnClickListener {
            val intent = Intent(this@ActividadDeportesActivity, PantallaMapaActivity::class.java)
            intent.putExtra("deporte", "caminata")  // Pasar "caminata" como deporte seleccionado
            startActivity(intent)
        }

        // Cuando el usuario selecciona salir
        btnSalir.setOnClickListener {
            val intent = Intent(this@ActividadDeportesActivity, MainActivity::class.java)
            startActivity(intent)
        }
    }
}
