package com.example.diplomwork.presentation.ui.screens.search_screen

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.diplomwork.R
import com.example.diplomwork.presentation.ui.components.LoadingSpinnerForScreen
import com.example.diplomwork.presentation.ui.components.spot_card.SpotCard
import com.example.diplomwork.presentation.ui.theme.ErrorColor
import com.example.diplomwork.presentation.viewmodel.SearchViewModel
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun SearchInputScreen(
    onBack: () -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val query by viewModel.searchQuery.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val noResults by viewModel.noResults.collectAsState()

    val context = LocalContext.current

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    LaunchedEffect(Unit) {
        viewModel.error.collect { errorMessage ->
            errorMessage?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    val listState = rememberLazyListState()

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .distinctUntilChanged()
            .collect { lastVisibleItemIndex ->
                if (lastVisibleItemIndex != null &&
                    lastVisibleItemIndex >= searchResults.lastIndex - 2 &&
                    !isLoading
                ) {
                    viewModel.performSearch(reset = false)
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SearchBar(
            query = query,
            onQueryChange = { viewModel.onSearchQueryChange(it) },
            onSearch = {
                focusManager.clearFocus()
                if (query.isNotBlank()) viewModel.performSearch(reset = true)
            },
            onBack = onBack,
            focusRequester = focusRequester
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {

                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingSpinnerForScreen()
                    }
                }

                noResults -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Нет результатов по вашему запросу",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(searchResults.size) { index ->
                            searchResults[index].let { spot ->
                                Log.e("fff", "$index - ${spot.thumbnailImageUrl}")
//                                SpotCard(
//                                    firstPicture = spot.thumbnailImageUrl,
//                                    additionalPictures = emptyList(), //additionalPictures[spot.id]?.pictures ?: emptyList(),
//                                    onLoadMore = { id, firstPicture -> }, //onLoadMore(id, firstPicture) },
//                                    picturesCount = spot.picturesCount,
//                                    username = spot.username,
//                                    title = spot.title,
//                                    placeName = spot.namePlace ?: "",
//                                    description = spot.description,
//                                    userId = spot.userId,
//                                    latitude = spot.latitude ?: 0.0,
//                                    longitude = spot.longitude ?: 0.0,
//                                    rating = spot.rating,
//                                    aspectRatio = spot.aspectRatio ?: 1f,
//                                    userProfileImageUrl = spot.userProfileImageUrl,
//                                    id = spot.id,
//                                    isCurrentUserOwner = spot.isCurrentUserOwner,
//                                    onSpotClick = { }, //onPictureClick(spot.id) },
//                                    screenName = "Spots"
//                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit,
    onSearch: () -> Unit,
    focusRequester: FocusRequester
) {

    Spacer(modifier = Modifier.height(20.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(55.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = {
                Text(
                    text = "Поиск интересных мест",
                    color = Color.Gray,
                    fontSize = 15.sp
                )
            },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    onSearch()
                }
            ),
            leadingIcon = {
                IconButton(
                    onClick = { onBack() },
                    modifier = Modifier
                        .padding(start = 3.dp)
                        .size(33.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_left),
                        contentDescription = "OnBack",
                        tint = Color.White
                    )
                }
            },
            trailingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_search),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .size(20.dp)
                        .clickable { onSearch() }
                )
            },
            modifier = Modifier
                .fillMaxSize()
                .focusRequester(focusRequester),
            singleLine = true,
            textStyle = TextStyle(fontSize = 17.sp),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.LightGray,
                errorBorderColor = ErrorColor,
                errorLabelColor = ErrorColor,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedTrailingIconColor = Color.White,
                unfocusedTrailingIconColor = Color.White,
                errorTrailingIconColor = Color.White
            )
        )
    }
}