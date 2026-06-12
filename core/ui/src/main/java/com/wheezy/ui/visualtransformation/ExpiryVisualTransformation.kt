package com.wheezy.skyflight.core.ui.visualtransformation

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class ExpiryVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.filter { it.isDigit() }.take(4)

        val formatted = when {
            digits.length <= 2 -> digits
            else -> digits.substring(0, 2) + "/" + digits.substring(2)
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val capped = offset.coerceIn(0, digits.length)
                return if (capped <= 2) capped else capped + 1
            }

            override fun transformedToOriginal(offset: Int): Int {
                return if (offset <= 2) offset else (offset - 1).coerceAtMost(digits.length)
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}