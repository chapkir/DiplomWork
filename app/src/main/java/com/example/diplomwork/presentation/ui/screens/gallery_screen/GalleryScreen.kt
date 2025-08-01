package com.example.diplomwork.presentation.ui.screens.gallery_screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.diplomwork.R
import com.example.diplomwork.presentation.ui.components.CustomTabPager
import com.example.diplomwork.presentation.ui.components.checkGalleryPermission
import com.example.diplomwork.presentation.ui.components.requestGalleryPermission
import com.example.diplomwork.presentation.ui.theme.ButtonPrimary
import com.example.diplomwork.presentation.viewmodel.GalleryViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
@Composable
fun GalleryScreen(
    onImageSelected: (List<String>) -> Unit,
    onBack: () -> Unit,
    viewModel: GalleryViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val images by viewModel.images.collectAsState()
    val selectedImages by viewModel.selectedImages.collectAsState()
    val albums by viewModel.albums.collectAsState()
    var hasPermission by remember { mutableStateOf(checkGalleryPermission(context)) }

    val selectedTabIndex by remember { mutableIntStateOf(0) }
    var onTabSelected by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Все фото", "Альбомы")

    val pagerState = rememberPagerState(initialPage = selectedTabIndex)

    val uriStrings = selectedImages.map { it.toString() }

    LaunchedEffect(pagerState.currentPage) {
        onTabSelected = pagerState.currentPage
    }

    LaunchedEffect(Unit) {
        if (hasPermission) {
            viewModel.loadGalleryData()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onBack() },
                modifier = Modifier
                    .padding(start = 7.dp, end = 5.dp)
                    .size(32.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_left),
                    contentDescription = "Back",
                    tint = Color.White,
                )
            }
            Text(
                text = "Выберите до 5 фото одного места",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(15.dp))

        CustomTabPager(
            tabTitles = tabTitles,
            pagerState = pagerState,
            modifier = Modifier.fillMaxSize(),
            lineOffset = 2.27
        ) { page ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                if (hasPermission) {
                    when (page) {
                        0 -> {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(5.dp)
                            ) {
                                items(images) { imageUri ->
                                    val isSelected = imageUri in selectedImages
                                    val selectedIndex = selectedImages.indexOf(imageUri) + 1

                                    Box(
                                        modifier = Modifier
                                            .size(120.dp)
                                            .padding(3.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isSelected) Color.White.copy(alpha = 0.2f)
                                                else Color.Transparent
                                            )
                                            .clickable {
                                                if (isSelected) {
                                                    viewModel.removeImage(imageUri)
                                                } else if (selectedImages.size < 5) {
                                                    viewModel.addImage(imageUri)
                                                }
                                            },
                                        contentAlignment = Alignment.BottomEnd
                                    ) {
                                        Image(
                                            painter = rememberAsyncImagePainter(imageUri),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )

                                        if (isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(Color.Gray.copy(alpha = 0.5f))
                                            )

                                            Text(
                                                text = selectedIndex.toString(),
                                                color = Color.White,
                                                fontSize = 32.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier
                                                    .align(Alignment.Center)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        1 -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(8.dp)
                            ) {
                                items(albums) { album ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.loadImagesFromAlbum(album.id)
                                                scope.launch {
                                                    pagerState.animateScrollToPage(0)
                                                }
                                            }
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Image(
                                            painter = rememberAsyncImagePainter(album.coverUri),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(64.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(album.name, color = Color.White, fontSize = 16.sp)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Для выбора фото необходимо разрешение",
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = {
                                requestGalleryPermission(context) { granted ->
                                    hasPermission = granted
                                    if (granted) viewModel.loadGalleryData()
                                }
                            }) {
                                Text("Разрешить")
                            }
                        }
                    }
                }
            }
        }
    }

    if (selectedImages.isNotEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            FloatingActionButton(
                onClick = { onImageSelected(uriStrings) },
                modifier = Modifier.width(100.dp),
                containerColor = ButtonPrimary,
            ) {
                Text(
                    text = "Далее",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}
