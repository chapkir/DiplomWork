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
object Notification : Screen

@Serializable
object OwnProfile : Screen

@Serializable
object EditProfile : Screen

@Serializable
object ViewPost : Screen

@Serializable
data class PictureDetailScreenData(
    val pictureId: Long,
    val imageUrl: String
) : Screen

@Serializable
data class CreateContentScreenData(
    val imageUrl: String,
    val whatContentCreate: String,
) : Screen

@Serializable
data class GalleryScreenData(
    val whatContentCreate: String,
) : Screen

@Serializable
data class OtherProfileScreenData(
    val userId: Long?,
    val username: String
) : Screen
