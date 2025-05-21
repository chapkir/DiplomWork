package com.example.diplomwork.presentation.ui.screens.search_screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.diplomwork.R
import com.example.diplomwork.presentation.ui.theme.ErrorColor

data class CategoryCard(
    val title: String,
    val count: String,
    val imageRes: Int
)

enum class TitleLocation {
    Inside,
    Outside
}

@Composable
fun SearchScreen() {

    val categoryList = listOf(
        CategoryCard("Гастрономия", "110 мест", R.drawable.gastronomy),
        CategoryCard("Вечерние прогулки", "60 мест", R.drawable.evening),
        CategoryCard("Праздники", "186 мест", R.drawable.holidays),
        CategoryCard("Достопримечательности", "42 места", R.drawable.attractions)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        SearchBar()

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
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .padding(start = 4.dp, bottom = 5.dp)
                )
            }

            item(span = { GridItemSpan(2) }) {
                CategoryCardItem(
                    category = CategoryCard(
                        title = "Летняя подборка ☘\uFE0F",
                        count = "100",
                        imageRes = R.drawable.summer_2
                    ),
                    titleLocation = TitleLocation.Inside,
                    onClick = { /* Действие при клике */ },
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }

            item(span = { GridItemSpan(2) }) {
                Text(
                    text = "Популярные категории ✨",
                    fontSize = 18.sp,
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
                    onClick = { /* Действие при клике */ }
                )
            }
        }
    }
}

@Composable
fun CategoryCardItem(
    category: CategoryCard,
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
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp, end = 10.dp)
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
fun SearchBar(
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    onSearch: () -> Unit = {}
) {
    val focusManager = LocalFocusManager.current

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = {
                Text(
                    text = "Поиск интересных мест",
                    color = Color.Gray,
                    fontSize = 15.sp
                )
            },
            trailingIcon = {
                IconButton(onClick = { }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_search),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .size(20.dp)
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 23.dp)
                .height(52.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    focusManager.clearFocus()
                    onSearch()
                }
            ),
            textStyle = TextStyle(fontSize = 15.sp),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.White,
                errorBorderColor = ErrorColor,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.Gray,
                cursorColor = Color.White,
                focusedTrailingIconColor = Color.White,
                unfocusedTrailingIconColor = Color.Gray,
                errorTrailingIconColor = Color.White
            )
        )
    }
}
