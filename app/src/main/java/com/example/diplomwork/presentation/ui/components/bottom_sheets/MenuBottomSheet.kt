package com.example.diplomwork.presentation.ui.components.bottom_sheets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diplomwork.presentation.ui.theme.BgDefault

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuBottomSheet(
    onDismiss: () -> Unit,
    isOwnContent: Boolean,
    onDelete: () -> Unit,
    sheetState: SheetState
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
        containerColor = BgDefault,
        scrimColor = Color.Black.copy(alpha = 0.5f),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 15.dp, bottom = 70.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            HorizontalDivider(color = Color.LightGray)
            Spacer(modifier = Modifier.height(24.dp))
            MenuItem(title = "Скачать изображение", onClick = {})
            MenuItem(title = "Пожаловаться на место", onClick = {})
            MenuItem(title = "Скрыть место", onClick = {})
            if (isOwnContent) MenuItem(title = "Удалить", onClick = { onDelete() })
        }
    }
}

@Composable
fun MenuItem(
    title: String,
    onClick: () -> Unit,
) {
    Text(
        text = title,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
            .padding(horizontal = 25.dp, vertical = 12.dp),
        color =
            if (title != "Удалить") Color.LightGray
            else Color.Red,
        fontSize = 20.sp,
        fontWeight = FontWeight.Medium,
    )
}