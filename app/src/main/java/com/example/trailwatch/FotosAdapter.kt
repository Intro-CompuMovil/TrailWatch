package com.example.trailwatch

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.io.File

class FotosAdapter(private val photoList: List<File>) :
    RecyclerView.Adapter<FotosAdapter.PhotoViewHolder>() {

    class PhotoViewHolder(val imageView: ImageView) : RecyclerView.ViewHolder(imageView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val imageView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_photo, parent, false) as ImageView
        return PhotoViewHolder(imageView)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val photoFile = photoList[position]

        // Cargar la imagen en el ImageView utilizando Glide o cualquier otra librería
        Glide.with(holder.imageView.context)
            .load(photoFile)
            .into(holder.imageView)
    }

    override fun getItemCount() = photoList.size
}
