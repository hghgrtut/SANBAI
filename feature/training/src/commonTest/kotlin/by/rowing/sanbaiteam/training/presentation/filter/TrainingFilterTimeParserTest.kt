package by.rowing.sanbaiteam.training.presentation.filter

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class TrainingFilterTimeParserTest {

    @Test
    fun `bare number is parsed as minutes`() {
        assertEquals(5_400_000L, TrainingFilterTimeParser.parseMillis("90"))
        assertEquals(120_000L, TrainingFilterTimeParser.parseMillis("2"))
    }

    @Test
    fun `minutes and seconds are parsed`() {
        assertEquals(90_000L, TrainingFilterTimeParser.parseMillis("1:30"))
        assertEquals(119_000L, TrainingFilterTimeParser.parseMillis("1:59"))
        assertEquals(0L, TrainingFilterTimeParser.parseMillis("0:00"))
    }

    @Test
    fun `outer whitespace is ignored`() {
        assertEquals(90_000L, TrainingFilterTimeParser.parseMillis(" 1:30 "))
    }

    @Test
    fun `invalid input returns null`() {
        listOf(
            "",
            "   ",
            "abc",
            "1:60",
            "1:",
            ":30",
            "1:2:3",
            "1.5",
            "-1",
            "1:-2",
        ).forEach { input ->
            assertNull(TrainingFilterTimeParser.parseMillis(input), "input: $input")
        }
    }
}
