package by.rowing.sanbaiteam.athlete.presentation.common

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/** Вставка точек в дату формата `ддммгггг` при вводе. */
internal class AthleteDateTransformation : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        val formattedText = buildAnnotatedString {
            for (i in originalText.indices) {
                append(originalText[i])
                if ((i == FIRST_DELIMITER_OFFSET - 1 || i == SECOND_DELIMITER_OFFSET) && i < originalText.lastIndex) {
                    append(".")
                }
            }
        }
        return TransformedText(
            text = formattedText,
            offsetMapping = object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int = when {
                    offset <= FIRST_DELIMITER_OFFSET -> offset
                    offset <= SECOND_DELIMITER_OFFSET + 1 -> offset + 1
                    offset <= DATE_FORMAT_LENGTH -> offset + 2
                    else -> DATE_FORMAT_LENGTH + 2
                }

                override fun transformedToOriginal(offset: Int): Int = when {
                    offset <= FIRST_DELIMITER_OFFSET - 1 -> offset
                    offset <= SECOND_DELIMITER_OFFSET - 1 -> offset - 1
                    offset <= DATE_FORMAT_LENGTH + 1 -> offset - 2
                    else -> DATE_FORMAT_LENGTH
                }.coerceIn(0, formattedText.length)
            }
        )
    }

    companion object {

        private const val FIRST_DELIMITER_OFFSET = 2
        private const val SECOND_DELIMITER_OFFSET = 3
        private const val DATE_FORMAT_LENGTH = 8
    }
}
