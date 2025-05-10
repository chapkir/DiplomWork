package com.example.diplomwork.presentation.ui.screens.spots_screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.example.diplomwork.R
import com.example.diplomwork.presentation.ui.components.bottom_sheets.ConfirmDeleteBottomSheet
import com.example.diplomwork.presentation.ui.components.bottom_sheets.MenuBottomSheet
import com.example.diplomwork.presentation.ui.theme.BgDefault
import com.example.diplomwork.presentation.ui.theme.BgElevated
import com.example.diplomwork.presentation.viewmodel.PicturesViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch

private const val aspectRatio = 0.75f

@Composable
fun SpotsScreen(
    picturesViewModel: PicturesViewModel = hiltViewModel(),
    onImageClick: (Long) -> Unit,
    onProfileClick: (Long, String) -> Unit
) {
    val pictures = picturesViewModel.picturesPagingFlow.collectAsLazyPagingItems()

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        picturesViewModel.deleteStatus.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    LazyColumn(
        modifier = Modifier
            .padding(top = 20.dp)
            .fillMaxSize()
            .background(BgDefault),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(pictures.itemCount) { place ->
            SpotsCard(place = place)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpotsCard(place: Place) {

    val coroutineScope = rememberCoroutineScope()
    val menuSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val confirmDeleteSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val openMenuSheet = { coroutineScope.launch { menuSheetState.show() } }
    val closeMenuSheet = { coroutineScope.launch { menuSheetState.hide() } }
    val openConfirmDeleteSheet = { coroutineScope.launch { confirmDeleteSheetState.show() } }
    val closeConfirmDeleteSheet = { coroutineScope.launch { confirmDeleteSheetState.hide() } }

    Card(
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        modifier = Modifier
            .padding(horizontal = 10.dp)
            .fillMaxWidth()
            .clickable { }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { clip = true }
                .background(BgElevated)
        ) {
            Row(
                modifier = Modifier.fillMaxSize()
            ) {
                ImagesPager(
                    imageUrls = place.imageUrls, modifier = Modifier
                        .aspectRatio(aspectRatio)
                        .weight(0.55f)
                )

                PlaceInfo(
                    place = place,
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(8.dp)
                        .weight(0.8f)
                )
            }
        }
    }

    if (menuSheetState.isVisible) {
        MenuBottomSheet(
            onDismiss = { closeMenuSheet() },
            onDelete = { openConfirmDeleteSheet() },
            sheetState = menuSheetState,
            isOwnContent = isCurrentUserOwner
        )
    }

    if (confirmDeleteSheetState.isVisible) {
        ConfirmDeleteBottomSheet(
            onDismiss = { closeConfirmDeleteSheet() },
            onDelete = {
                onSpotDelete(id)
                closeMenuSheet()
            },
            sheetState = confirmDeleteSheetState,
        )
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun ImagesPager(imageUrls: List<Int>, modifier: Modifier) {

    val pagerState = rememberPagerState()

    Column(
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(aspectRatio)
                .weight(0.55f)
        ) {
            HorizontalPager(
                count = imageUrls.size,
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(5.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(aspectRatio)
                            .graphicsLayer { clip = true }
                    ) {

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFFB8D1FF),
                                            Color(0xFF2B7EFE),
                                        )
                                    )
                                )
                                .blur(50.dp)
                        )

                        AsyncImage(
                            model = imageUrls[page],
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .matchParentSize()
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 10.dp)
        ) {
            HorizontalPagerIndicator(
                pagerState = pagerState,
                modifier = Modifier
                    .align(Alignment.BottomCenter),
                activeColor = Color.White,
                inactiveColor = Color.LightGray,
                indicatorWidth = 6.dp,
                spacing = 4.dp
            )
        }
    }
}

@Composable
fun PlaceInfo(place: Place, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
    ) {
        Text(
            text = place.title,
            fontSize = 16.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))

        // Рейтинг
        RatingBar(rating = place.rating)

        Spacer(modifier = Modifier.height(4.dp))

        // Геотег
        Text(
            text = place.location,
            fontSize = 12.sp,
            color = Color.Gray
        )




        Spacer(modifier = Modifier.height(4.dp))

        // Описание
        Text(
            text = place.description,
            fontSize = 13.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun RatingBar(rating: Int) {
    Row {
        repeat(5) { index ->
            val starColor = if (index < rating) Color.Yellow else Color.Gray
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Рейтинг",
                tint = starColor,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

data class Place(
    val id: Int,
    val title: String,
    val location: String,
    val description: String,
    val rating: Int,
    val imageUrls: List<Int>
)

// Пример данных
val samplePlaces = listOf(
    Place(
        id = 1,
        title = "Атмосферная кофейня",
        location = "Невский проспект",
        description = "Место для спокойного отдыха и чашечки кофе.",
        rating = 4,
        imageUrls = listOf(
            R.drawable.default_img_1,
            R.drawable.defoult_image
        )
    ),
    Place(
        id = 2,
        title = "Живописная галерея",
        location = "Васильевский остров",
        description = "Лучшие произведения современного искусства.",
        rating = 5,
        imageUrls = listOf(
            R.drawable.default_img_1,
            R.drawable.defoult_image,
            R.drawable.defoult_image
        )
    )
)

@Composable
fun PreviewPlacesScreen() {
    SpotsScreen(places = samplePlaces)
}
