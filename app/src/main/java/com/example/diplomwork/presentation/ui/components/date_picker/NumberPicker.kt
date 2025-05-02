package com.example.diplomwork.presentation.ui.components.date_picker

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun NumberPicker(from: Int, to: Int, selected: Int, onValueChange: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        TextButton(onClick = { expanded = true }) {
            Text(selected.toString())
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            (from..to).forEach {
                DropdownMenuItem(
                    text = { Text(it.toString()) },
                    onClick = {
                        onValueChange(it)
                        expanded = false
                    }
                )
            }
        }
    }
}