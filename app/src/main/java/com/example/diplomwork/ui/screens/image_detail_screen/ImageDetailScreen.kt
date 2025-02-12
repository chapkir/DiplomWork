import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun ImageDetailScreen(
    imageRes: Int, // Ресурс изображения
    likesCount: Int,
    comments: List<String>,
    onLikeClick: () -> Unit
) {
    var commentText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Отображение изображения
        AsyncImage(
            model = imageRes,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Лайки
        Text(
            text = "❤️ $likesCount лайков",
            style = MaterialTheme.typography.bodyLarge
        )
        Button(onClick = onLikeClick) {
            Text(text = "Лайк")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Список комментариев
        Text(text = "Комментарии:", style = MaterialTheme.typography.bodyMedium)
        comments.forEach { comment ->
            Text(text = comment, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Поле для ввода нового комментария
        BasicTextField(
            value = commentText,
            onValueChange = { commentText = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .border(1.dp, Color.Gray),
            textStyle = TextStyle(color = Color.Black)
        )
        Button(onClick = {
            // Добавление нового комментария
        }) {
            Text(text = "Добавить комментарий")
        }
    }
}