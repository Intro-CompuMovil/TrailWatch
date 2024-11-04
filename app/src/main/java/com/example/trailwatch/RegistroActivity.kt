package com.example.trailwatch

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class RegistroActivity : AppCompatActivity() {

    private lateinit var editTextNombre: EditText
    private lateinit var editTextApellido: EditText
    private lateinit var editTextCorreo: EditText
    private lateinit var editTextUsuario: EditText
    private lateinit var editTextContrasena: EditText
    private lateinit var btnShowPassword: ImageButton
    private lateinit var btnContinuar: Button
    private lateinit var btnRegresar: Button
    private lateinit var passwordStrengthBar: ProgressBar
    private lateinit var passwordErrorTextView: TextView
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        // Referencias a los campos
        editTextNombre = findViewById(R.id.editTextNombre)
        editTextApellido = findViewById(R.id.editTextApellido)
        editTextCorreo = findViewById(R.id.editTextCorreo)
        editTextUsuario = findViewById(R.id.editTextUsuario)
        editTextContrasena = findViewById(R.id.editTextContrasena)
        btnShowPassword = findViewById(R.id.btnShowPassword)
        btnContinuar = findViewById(R.id.btnContinuar)
        btnRegresar = findViewById(R.id.btnRegresar)
        passwordStrengthBar = findViewById(R.id.passwordStrengthBar)
        passwordErrorTextView = findViewById(R.id.passwordErrorTextView)

        // Desactivar el botón Continuar inicialmente
        btnContinuar.isEnabled = false

        // Mostrar u ocultar la contraseña
        btnShowPassword.setOnClickListener {
            togglePasswordVisibility()
        }

        // Escuchar cambios en la contraseña
        editTextContrasena.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                validarCampos()
                validarContrasena(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Validación en tiempo real de los campos
        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                validarCampos()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        editTextNombre.addTextChangedListener(textWatcher)
        editTextApellido.addTextChangedListener(textWatcher)
        editTextCorreo.addTextChangedListener(textWatcher)
        editTextUsuario.addTextChangedListener(textWatcher)

        // Acción del botón Regresar
        btnRegresar.setOnClickListener {
            finish() // Regresa a la pantalla anterior
        }

        // Acción del botón Continuar
        btnContinuar.setOnClickListener {
            navegarADatosActivity()
        }
    }

    // Función para validar y actualizar la barra de fuerza de la contraseña
    private fun validarContrasena(contrasena: String) {
        // Verifica si la contraseña cumple con el requisito de iniciar con una mayúscula
        if (contrasena.isNotEmpty() && !contrasena[0].isUpperCase()) {
            // Mostrar error y mantener la barra en rojo
            passwordErrorTextView.text = "La contraseña debe comenzar con una letra mayúscula."
            passwordErrorTextView.visibility = TextView.VISIBLE
            passwordStrengthBar.progress = 25
            passwordStrengthBar.progressDrawable.setTint(Color.RED)
        } else {
            // Si cumple con el requisito de la mayúscula, oculta el mensaje de error
            passwordErrorTextView.text = ""
            passwordErrorTextView.visibility = TextView.GONE

            // Ahora calcula la fuerza de la contraseña según su longitud
            when (contrasena.length) {
                in 0..3 -> {
                    passwordStrengthBar.progress = 33
                    passwordStrengthBar.progressDrawable.setTint(Color.RED)
                }
                in 3..5 -> {
                    passwordStrengthBar.progress = 66
                    passwordStrengthBar.progressDrawable.setTint(Color.YELLOW)
                }
                else -> {
                    passwordStrengthBar.progress = 100
                    passwordStrengthBar.progressDrawable.setTint(Color.GREEN)
                }
            }
        }
    }

    // Validar los campos y habilitar el botón continuar si todos están completos
    private fun validarCampos() {
        val nombre = editTextNombre.text.toString()
        val apellido = editTextApellido.text.toString()
        val correo = editTextCorreo.text.toString()
        val usuario = editTextUsuario.text.toString()
        val contrasena = editTextContrasena.text.toString()

        // Habilitar el botón solo si todos los campos están completos y la contraseña comienza con mayúscula
        btnContinuar.isEnabled = nombre.isNotEmpty() && apellido.isNotEmpty() && correo.isNotEmpty() &&
                usuario.isNotEmpty() && contrasena.isNotEmpty() && contrasena[0].isUpperCase()
    }

    // Mostrar u ocultar la contraseña
    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            editTextContrasena.inputType =
                android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            btnShowPassword.setImageResource(R.drawable.ic_visibility_off)
        } else {
            editTextContrasena.inputType =
                android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            btnShowPassword.setImageResource(R.drawable.ic_visibility)
        }
        isPasswordVisible = !isPasswordVisible
        editTextContrasena.setSelection(editTextContrasena.text.length) // Mover cursor al final
    }

    // Método para navegar a DatosActivity pasando los datos ingresados
    private fun navegarADatosActivity() {
        val nombre = editTextNombre.text.toString().trim()
        val apellido = editTextApellido.text.toString().trim()
        val correo = editTextCorreo.text.toString().trim()
        val username = editTextUsuario.text.toString().trim()
        val contraseña = editTextContrasena.text.toString()

        // Validaciones
        if (nombre.isEmpty() || apellido.isEmpty() || correo.isEmpty() || username.isEmpty() || contraseña.isEmpty()) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        // Verificar formato del correo electrónico
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            Toast.makeText(this, "Ingrese un correo electrónico válido", Toast.LENGTH_SHORT).show()
            return
        }

        // Verifica que la contraseña comience con mayúscula
        if (!contraseña[0].isUpperCase()) {
            Toast.makeText(this, "La contraseña debe comenzar con una letra mayúscula.", Toast.LENGTH_SHORT).show()
            return
        }

        // Leer usuarios existentes
        val usuarios = UsuarioUtils.leerUsuariosDesdeArchivo(this)

        // Verificar si el nombre de usuario o correo ya existen
        if (usuarios.any { it.username == username }) {
            Toast.makeText(this, "El nombre de usuario ya está registrado", Toast.LENGTH_SHORT).show()
            return
        }
        if (usuarios.any { it.correo == correo }) {
            Toast.makeText(this, "El correo ya está registrado", Toast.LENGTH_SHORT).show()
            return
        }

        // Pasar los datos a DatosActivity
        val intent = Intent(this, DatosActivity::class.java)
        intent.putExtra("nombre", nombre)
        intent.putExtra("apellido", apellido)
        intent.putExtra("correo", correo)
        intent.putExtra("username", username)
        intent.putExtra("contraseña", contraseña)
        startActivity(intent)
        finish()
    }
}