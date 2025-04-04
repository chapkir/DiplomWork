package com.example.diplomwork.ui.screens.add_content_screens

import android.net.Uri

sealed class ContentType {

    data class Post(
        val text: String = ""
    ) : ContentType()

    data class Picture(
        val imageUri: Uri,
        val title: String = "",
        val description: String = ""
    ) : ContentType()

}