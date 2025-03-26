package com.example.diplomwork.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen

@Serializable
object Splash : Screen

@Serializable
object Login : Screen

@Serializable
object Register : Screen

@Serializable
object Home : Screen

@Serializable
object Posts : Screen

@Serializable
object AddContent : Screen

@Serializable
object CreatePicture : Screen

@Serializable
object CreatePost : Screen

@Serializable
object Notification : Screen

@Serializable
object Profile : Screen

@Serializable
object ViewPicture : Screen

@Serializable
object ViewPost : Screen

@Serializable
data class ViewPictureDetailScreenData(
    val pictureId: String,
    val imageUrl: String
) : Screen
