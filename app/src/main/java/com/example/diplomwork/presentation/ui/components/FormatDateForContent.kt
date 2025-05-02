package com.example.diplomwork.presentation.ui.components

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

fun formatDate(isoString: String): String {
    return try {
        val parsed = LocalDateTime.parse(isoString, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val now = LocalDate.now()

        val formatter = if (parsed.year == now.year) {
            DateTimeFormatter.ofPattern("HH:mm dd.MM")
        } else {
            DateTimeFormatter.ofPattern("HH:mm dd.MM.yyyy")
        }

        parsed.format(formatter)
    } catch (e: Exception) {
        ""
    }
}