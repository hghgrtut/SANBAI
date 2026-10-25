package by.rowing.sanbaiteam.training.data.trainingimport

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class TrainingImportContainerTest {

    @Test
    fun pack_singleFile_returnsOriginalBytes() {
        val file = byteArrayOf(1, 2, 3)

        assertContentEquals(file, TrainingImportContainer.pack(listOf(file)))
    }

    @Test
    fun packAndUnpack_severalFiles_roundTripsOriginalBytes() {
        val first = byteArrayOf(1, 2, 3, 4)
        val second = byteArrayOf(9, 8, 7)

        val packed = TrainingImportContainer.pack(listOf(first, second))

        assertTrue(TrainingImportContainer.isContainer(packed))
        val unpacked = TrainingImportContainer.unpack(packed)
        assertEquals(2, unpacked?.size)
        assertContentEquals(first, unpacked?.get(0))
        assertContentEquals(second, unpacked?.get(1))
    }

    @Test
    fun unpack_plainFile_returnsNull() {
        assertNull(TrainingImportContainer.unpack(byteArrayOf(1, 2, 3)))
        assertFalse(TrainingImportContainer.isContainer(byteArrayOf(1, 2, 3)))
    }

    @Test
    fun unpackOrSingle_plainFile_returnsSingleElement() {
        val file = byteArrayOf(5, 6, 7)
        val files = TrainingImportContainer.unpackOrSingle(file)

        assertEquals(1, files.size)
        assertContentEquals(file, files.single())
    }
}
