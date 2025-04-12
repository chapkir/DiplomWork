package com.example.diplomwork.ui.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class CustomVisualTransformationForPassword : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val maskChar = '•'

        val masked = buildAnnotatedString {
            repeat(text.length) {
                append(maskChar)
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int) = offset.coerceAtMost(masked.length)
            override fun transformedToOriginal(offset: Int) = offset.coerceAtMost(text.length)
        }

        return TransformedText(masked, offsetMapping)
    }
}