package com.example.diplomwork.presentation.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CustomSnackbarHost(snackbarHostState: SnackbarHostState) {
    SnackbarHost(hostState = snackbarHostState) { snackbarData ->
        Snackbar(
            containerColor = Color.Black.copy(alpha = 0.9f),
            contentColor = Color.White,
            actionContentColor = Color.Red,
            shape = RoundedCornerShape(15.dp),
            action = {
                snackbarData.visuals.actionLabel?.let { actionLabel ->
                    TextButton(onClick = { snackbarData.dismiss() }) {
                        Text(actionLabel)
                    }
                }
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text(snackbarData.visuals.message)
        }
    }
}