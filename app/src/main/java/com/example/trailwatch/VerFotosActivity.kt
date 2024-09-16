package com.example.trailwatch

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class VerFotosActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fotosAdapter: FotosAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ver_fotos)

        recyclerView = findViewById(R.id.recyclerViewFotos)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Cargar la lista de fotos desde el directorio
        val photoList = loadPhotosFromDirectory()

        // Inicializar el adapter
        fotosAdapter = FotosAdapter(photoList)
        recyclerView.adapter = fotosAdapter
    }

    // Método para obtener la lista de archivos de imagen del directorio
    private fun loadPhotosFromDirectory(): List<File> {
        val photosDir = File(externalMediaDirs.firstOrNull(), getString(R.string.app_name))
        return if (photosDir.exists()) {
            photosDir.listFiles { file -> file.extension == "jpg" }?.toList() ?: emptyList()
        } else {
            emptyList()
        }
    }
}