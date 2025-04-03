package com.example.diplomwork.model

import android.net.Uri

data class GalleryAlbum(
    val id: String,
    val name: String,
    val coverUri: Uri
)