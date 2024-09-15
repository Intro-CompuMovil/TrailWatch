package com.example.trailwatch

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SOSActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var tvContador: TextView
    private lateinit var btnCancelar: Button
    private lateinit var btnHistorial: Button
    private lateinit var contacto: TextView
    private var contador = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sos)

        val sharedPreferences = getSharedPreferences("EmergencyContact", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Referencia a los elementos
        contacto = findViewById(R.id.tvContactosEmergencia)
        progressBar = findViewById(R.id.circularProgress)
        tvContador = findViewById(R.id.tvContador)
        btnCancelar = findViewById(R.id.btnCancelar)
        btnHistorial = findViewById(R.id.btnHistorial)

        // Iniciar contador con intermitencia
        val timer = object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                contador--
                tvContador.text = contador.toString()
                progressBar.progress = 10 - contador
                // Intermitencia del texto
                tvContador.alpha = if (contador % 2 == 0) 1.0f else 0.5f
            }

            override fun onFinish() {
                // Simular alerta enviada
                tvContador.text = "¡Alerta Enviada!"
                // Aquí podrías agregar lógica para enviar la alerta real
            }
        }
        timer.start()

        // Cancelar la alerta
        btnCancelar.setOnClickListener {
            timer.cancel()
            startActivity(Intent(this@SOSActivity, PantallaMapaActivity::class.java))
        }

        // Ver el historial de alertas
        btnHistorial.setOnClickListener {
            startActivity(Intent(this@SOSActivity, HistorialActivity::class.java))
        }

        val nombre = sharedPreferences.getString("contact_name","VALOR POR DEFECTO")
        val numero = sharedPreferences.getString("contact_number","VALOR POR DEFECTO")
        contacto.text ="$nombre $numero"



    }
}
