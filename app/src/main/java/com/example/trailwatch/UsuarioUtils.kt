package com.example.trailwatch

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object UsuarioUtils {

    fun leerUsuariosDesdeArchivo(context: Context): MutableList<Usuario> {
        val usuarios = mutableListOf<Usuario>()
        val file = File(context.filesDir, "usuarios.json")
        if (!file.exists()) return usuarios

        val jsonString = file.readText()
        val jsonObject = JSONObject(jsonString)
        val jsonArray = jsonObject.getJSONArray("usuarios")

        for (i in 0 until jsonArray.length()) {
            val usuarioJSON = jsonArray.getJSONObject(i)
            val usuario = Usuario(
                username = usuarioJSON.getString("username"),
                nombre = usuarioJSON.getString("nombre"),
                apellido = usuarioJSON.getString("apellido"),
                correo = usuarioJSON.getString("correo"),
                contraseña = usuarioJSON.getString("contraseña"),
                peso = if (usuarioJSON.has("peso")) usuarioJSON.getDouble("peso") else null,
                edad = if (usuarioJSON.has("edad")) usuarioJSON.getInt("edad") else null,
                estatura = if (usuarioJSON.has("estatura")) usuarioJSON.getDouble("estatura") else null,
                rh = if (usuarioJSON.has("rh")) usuarioJSON.getString("rh") else null,
                enfermedad = if (usuarioJSON.has("enfermedad")) usuarioJSON.getString("enfermedad") else null,
                imagenPerfil = if (usuarioJSON.has("imagenPerfil")) usuarioJSON.getString("imagenPerfil") else null
            )
            usuarios.add(usuario)
        }
        return usuarios
    }

    fun guardarUsuariosEnArchivo(context: Context, usuarios: List<Usuario>) {
        val file = File(context.filesDir, "usuarios.json")
        val jsonObject = JSONObject()
        val jsonArray = JSONArray()

        usuarios.forEach { usuario ->
            val usuarioJSON = JSONObject()
            usuarioJSON.put("username", usuario.username)
            usuarioJSON.put("nombre", usuario.nombre)
            usuarioJSON.put("apellido", usuario.apellido)
            usuarioJSON.put("correo", usuario.correo)
            usuarioJSON.put("contraseña", usuario.contraseña)
            usuario.peso?.let { usuarioJSON.put("peso", it) }
            usuario.edad?.let { usuarioJSON.put("edad", it) }
            usuario.estatura?.let { usuarioJSON.put("estatura", it) }
            usuario.rh?.let { usuarioJSON.put("rh", it) }
            usuario.enfermedad?.let { usuarioJSON.put("enfermedad", it) }
            usuario.imagenPerfil?.let { usuarioJSON.put("imagenPerfil", it) }
            jsonArray.put(usuarioJSON)
        }

        jsonObject.put("usuarios", jsonArray)
        file.writeText(jsonObject.toString())
    }
}
