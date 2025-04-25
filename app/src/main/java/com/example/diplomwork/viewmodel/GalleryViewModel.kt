package com.example.diplomwork.viewmodel

import android.app.Application
import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diplomwork.model.GalleryAlbum
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val context: Application
) : ViewModel() {

    private val _images = MutableStateFlow<List<Uri>>(emptyList())
    val images: StateFlow<List<Uri>> = _images

    private val _albums = MutableStateFlow<List<GalleryAlbum>>(emptyList())
    val albums: StateFlow<List<GalleryAlbum>> = _albums


    fun loadGalleryData() {
        viewModelScope.launch(Dispatchers.IO) {
            val fetchedAlbums = fetchGalleryAlbums()
            _albums.value = fetchedAlbums

            if (fetchedAlbums.isNotEmpty()) {
                // Ищем альбом "Camera" или "Камера"
                val cameraAlbum = fetchedAlbums.firstOrNull {
                    it.name.equals("Camera", ignoreCase = true) ||
                            it.name.equals("Камера", ignoreCase = true)
                }

                // Если найден — загружаем из него, иначе — из первого
                val defaultAlbumId = cameraAlbum?.id ?: fetchedAlbums.first().id
                loadImagesFromAlbum(defaultAlbumId)
            }
        }
    }

    fun loadImagesFromAlbum(albumId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val fetchedImages = fetchGalleryImages(albumId)
            _images.value = fetchedImages
        }
    }

    private fun fetchGalleryAlbums(): List<GalleryAlbum> {
        val albumList = mutableListOf<GalleryAlbum>()
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.DATA
        )
        val orderBy = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        context.contentResolver.query(uri, projection, null, null, orderBy)?.use { cursor ->
            val albumIdColumn = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID)
            val albumNameColumn = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            val imagePathColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATA)

            val albumMap = mutableMapOf<String, GalleryAlbum>()

            while (cursor.moveToNext()) {
                val albumId = cursor.getString(albumIdColumn) ?: continue
                val albumName = cursor.getString(albumNameColumn) ?: "Без имени"
                val imagePath = cursor.getString(imagePathColumn) ?: continue
                val imageUri = Uri.fromFile(File(imagePath))

                if (!albumMap.containsKey(albumId)) {
                    albumMap[albumId] = GalleryAlbum(albumId, albumName, imageUri)
                }
            }

            albumList.addAll(albumMap.values)
        }

        return albumList
    }

    private fun fetchGalleryImages(albumId: String): List<Uri> {
        val imageList = mutableListOf<Uri>()
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val selection = "${MediaStore.Images.Media.BUCKET_ID} = ?"
        val selectionArgs = arrayOf(albumId)
        val orderBy = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        context.contentResolver.query(uri, projection, selection, selectionArgs, orderBy)?.use { cursor ->
            val imagePathColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATA)

            while (cursor.moveToNext()) {
                val imagePath = cursor.getString(imagePathColumn) ?: continue
                val imageUri = Uri.fromFile(File(imagePath))
                imageList.add(imageUri)
            }
        }

        return imageList
    }
}