package com.wheezy.skyflight.core.ui.visualtransformation

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class CardNumberVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.filter { it.isDigit() }.take(16)
        if (digits.isEmpty()) {
            val identityMapping = object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int = 0
                override fun transformedToOriginal(offset: Int): Int = 0
            }
            return TransformedText(AnnotatedString(""), identityMapping)
        }
        val groups = digits.chunked(4)
        val maskedGroups = groups.mapIndexed { idx, grp ->
            if (idx < groups.lastIndex && grp.length == 4) {
                "****"
            } else {
                grp.padEnd(4, '•')
            }
        }
        val formatted = maskedGroups.joinToString(" ")

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val capped = offset.coerceIn(0, digits.length)
                val spacesBefore = capped / 4
                return (capped + spacesBefore).coerceIn(0, formatted.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val capped = offset.coerceIn(0, formatted.length)
                val blocks = capped / 5
                val orig = capped - blocks
                return orig.coerceIn(0, digits.length)
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}