package by.rowing.sanbaiteam.athlete.domain.weight

import by.rowing.sanbaiteam.core.util.formatTenths

internal const val MIN_WEIGHT_TENTHS = 200
internal const val MAX_WEIGHT_TENTHS = 3000

internal const val TENTHS_IN_KILOGRAM = 10.0

private val WEIGHT_INPUT_REGEX = Regex(pattern = """\d{1,3}(\.\d)?""")
private const val MAX_WHOLE_DIGITS = 3
private const val MAX_FRACTION_DIGITS = 1
private const val DECIMAL_SEPARATOR = '.'
private const val COMMA_SEPARATOR = ','

/** Оставляет цифры и один десятичный разделитель; запятая нормализуется в точку. */
internal fun sanitizeWeightInput(text: String): String {
    val normalized = text.replace(COMMA_SEPARATOR, DECIMAL_SEPARATOR)
    val separatorIndex = normalized.indexOf(DECIMAL_SEPARATOR)
    if (separatorIndex < 0) {
        return normalized.filter { it.isDigit() }.take(MAX_WHOLE_DIGITS)
    }
    val whole = normalized.substring(0, separatorIndex)
        .filter { it.isDigit() }
        .take(MAX_WHOLE_DIGITS)
    val fraction = normalized.substring(separatorIndex + 1)
        .filter { it.isDigit() }
        .take(MAX_FRACTION_DIGITS)
    return "$whole$DECIMAL_SEPARATOR$fraction"
}

/** Парсит вес в десятых килограмма; `null`, если значение пустое или вне 20–300 кг. */
internal fun parseWeightTenths(text: String): Int? {
    val normalized = text.trim()
        .replace(COMMA_SEPARATOR, DECIMAL_SEPARATOR)
        .removeSuffix(DECIMAL_SEPARATOR.toString())
    if (!WEIGHT_INPUT_REGEX.matches(normalized)) return null
    val parts = normalized.split(DECIMAL_SEPARATOR)
    val whole = parts[0].toIntOrNull() ?: return null
    val fraction = parts.getOrNull(1)?.toIntOrNull() ?: 0
    val tenths = whole * 10 + fraction
    return tenths.takeIf { it in MIN_WEIGHT_TENTHS..MAX_WEIGHT_TENTHS }
}

internal fun formatWeightTenths(tenths: Int): String = formatTenths(value = tenths / TENTHS_IN_KILOGRAM)