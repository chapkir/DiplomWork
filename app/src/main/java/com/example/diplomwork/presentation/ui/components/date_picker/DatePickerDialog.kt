package com.example.diplomwork.presentation.ui.components.date_picker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import java.util.Calendar

@Composable
fun CustomDatePickerDialog(
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val calendar = Calendar.getInstance()
    var day by remember { mutableIntStateOf(calendar.get(Calendar.DAY_OF_MONTH)) }
    var month by remember { mutableIntStateOf(calendar.get(Calendar.MONTH)) }
    var year by remember { mutableIntStateOf(calendar.get(Calendar.YEAR)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val formatted = String.format("%02d.%02d.%04d", day, month + 1, year)
                onDateSelected(formatted)
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        },
        title = { Text("Выберите дату рождения") },
        text = {
            Column {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    NumberPicker(1, 31, day) { day = it }
                    NumberPicker(0, 11, month) { month = it }
                    NumberPicker(1950, calendar.get(Calendar.YEAR), year) { year = it }
                }
            }
        }
    )
}