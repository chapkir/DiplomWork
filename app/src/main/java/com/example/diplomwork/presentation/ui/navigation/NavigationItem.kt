package com.example.diplomwork.presentation.ui.navigation

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
object Pictures : Screen

@Serializable
object Spots : Screen

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
object Settings : Screen

@Serializable
object Search : Screen

@Serializable
object Map : Screen

@Serializable
object ViewPost : Screen

@Serializable
object Licenses : Screen

@Serializable
object ManagementAccount : Screen

@Serializable
data class PictureDetailScreenData(
    val pictureId: Long
) : Screen

@Serializable
data class GalleryScreenData(
    val spotName: String? = null,
    val spotAddress: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
) : Screen

@Serializable
data class CreateSpotScreenData(
    val spotName: String? = null,
    val spotAddress: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val imageUrls: List<String> = emptyList(),
) : Screen

@Serializable
data class OtherProfileScreenData(
    val userId: Long,
    val username: String
) : Screen
