package com.example.diplomwork.presentation.ui.screens.search_screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.diplomwork.R
import com.example.diplomwork.presentation.ui.components.LoadingSpinnerForScreen
import com.example.diplomwork.presentation.viewmodel.CategoryViewModel


enum class TitleLocation {
    Inside,
    Outside
}

@Composable
fun CategoryScreen(
    onSearchBarClick: () -> Unit,
    viewModel: CategoryViewModel = hiltViewModel()
) {

    val uiState by viewModel.uiState.collectAsState()
    val categoryList = viewModel.categoryList

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CategorySearchBar(onSearchBarClick = onSearchBarClick)
        Spacer(Modifier.height(17.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(13.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item(span = { GridItemSpan(2) }) {
                Text(
                    text = "Лето вместе с Spotsy ☀\uFE0F",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .padding(start = 4.dp, bottom = 5.dp)
                )
            }

            item(span = { GridItemSpan(2) }) {
                CategoryCardItem(
                    category = CategoryViewModel.CategoryCard(
                        "Летняя подборка ☘️",
                        "100",
                        R.drawable.summer_2
                    ),
                    titleLocation = TitleLocation.Inside,
                    onClick = {
                        viewModel.loadSpotsByCategory("Летняя подборка")
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item(span = { GridItemSpan(2) }) {
                Text(
                    text = "Популярные категории ✨",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.padding(start = 4.dp, top = 10.dp, bottom = 5.dp)
                )
            }

            items(categoryList) { category ->
                CategoryCardItem(
                    category = category,
                    titleLocation = TitleLocation.Outside,
                    onClick = {
                        viewModel.loadSpotsByCategory(category.title)
                    }
                )
            }
        }

        if (uiState.isLoading) {
            LoadingSpinnerForScreen()
        }

        uiState.errorMessage?.let {
            Text(it, color = Color.Red, modifier = Modifier.padding(top = 16.dp))
        }

        if (uiState.spots.isNotEmpty()) {
            Text(
                text = "Найдено мест: ${uiState.spots.size}",
                color = Color.Green,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun CategoryCardItem(
    category: CategoryViewModel.CategoryCard,
    modifier: Modifier = Modifier,
    titleLocation: TitleLocation = TitleLocation.Outside,
    onClick: () -> Unit,
) {
    Column(modifier) {
        Box(
            modifier = Modifier
                .aspectRatio(1.33f)
                .clip(RoundedCornerShape(16.dp))
                .clickable { onClick() },
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(category.imageRes)
                    .crossfade(300)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(16.dp))
            )

            if (titleLocation == TitleLocation.Inside) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(bottom = 16.dp, start = 20.dp)
                ) {
                    Text(
                        text = category.title,
                        fontSize = 31.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "2025",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        if (titleLocation == TitleLocation.Outside) {
            Text(
                text = category.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                modifier = Modifier.padding(top = 4.dp, start = 8.dp)
            )
            Text(
                text = category.count,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.padding(top = 2.dp, start = 8.dp)
            )
        }
    }
}

@Composable
private fun CategorySearchBar(
    onSearchBarClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp)
            .height(52.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                onSearchBarClick()
            }
    ) {
        OutlinedTextField(
            value = "",
            onValueChange = {},
            enabled = false,
            placeholder = {
                Text(
                    text = "Поиск интересных мест",
                    color = Color.Gray,
                    fontSize = 15.sp
                )
            },
            trailingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_search),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .size(20.dp)
                )
            },
            modifier = Modifier
                .fillMaxSize(),
            singleLine = true,
            textStyle = TextStyle(fontSize = 15.sp),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledBorderColor = Color.LightGray,
                disabledTextColor = Color.LightGray,
                disabledPlaceholderColor = Color.LightGray,
                disabledTrailingIconColor = Color.LightGray,
                cursorColor = Color.Transparent
            )
        )
    }
}
