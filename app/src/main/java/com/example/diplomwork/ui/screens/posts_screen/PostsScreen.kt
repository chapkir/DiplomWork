package com.example.diplomwork.ui.screens.posts_screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.diplomwork.R
import com.example.diplomwork.model.PostResponse
import com.example.diplomwork.ui.theme.ColorForBackground
import com.example.diplomwork.viewmodel.PostsScreenViewModel

@Composable
fun PostsScreen(
    viewModel: PostsScreenViewModel = hiltViewModel()
) {

    val posts by viewModel.posts.collectAsState()


        val exPosts = listOf(
        PostResponse(
            id = 1,
            userAvatar = "R.drawable.default_avatar",
            username = "Сашка Север",
            text = "Какой прекрасный вид",
            imageUrl = "R.drawable.defoult_image",
            likesCount = 10,
            comments = null
        ),
        PostResponse(
            id = 2,
            userAvatar = "R.drawable.default_avatar",
            username = "Кирюха Член",
            text = "Вот это да, офигеть, надо туда сходить всей семьей",
            imageUrl = "R.drawable.default_img_1",
            likesCount = 15,
            comments = null
        ),
        PostResponse(
            id = 3,
            userAvatar = "R.drawable.default_avatar",
            username = "Алексей Сосиска",
            text = "Ммм, как же я люблю макароны с сосисками есть, это просто блаженство, каждый день их ем и всем советую, после них какать круто!!!!",
            imageUrl = null,
            likesCount = 15,
            comments = null
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(posts) { post ->
            PostCard(post)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun PostCard(post: PostResponse) {
    var likes by remember { mutableStateOf(post.likesCount) }
    var isLiked by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = ColorForBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Аватарка и никнейм
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = post.userAvatar,
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = post.username,
                    fontWeight = FontWeight.Bold,
                    color = Color.White)
            }

            // Текст поста
            if (post.text.isNotEmpty()) {
                Text(
                    text = post.text,
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = Color.White
                )
            }

            // Картинка поста (если есть)
            post.imageUrl?.let {
                AsyncImage(
                    model = it,
                    contentDescription = "Post Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Row(
                modifier = Modifier.padding(top = 10.dp, start = 10.dp, end = 10.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { isLiked = !isLiked }
                ) {
                    Icon(
                        painter = painterResource(
                            id =
                                if (isLiked) R.drawable.ic_favs_filled
                                else R.drawable.ic_favs
                        ),
                        contentDescription = "Лайк",
                        tint = if (isLiked) Color.Red else Color.White,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "10",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_comments),
                        contentDescription = "Комментарии",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "2",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White
                    )
                }
            }
        }
    }
}