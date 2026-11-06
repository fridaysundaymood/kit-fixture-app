package dev.androidkit.fixture.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class TaskConvertersTest {
    private val converters = TaskConverters()

    @Test fun statusIdsAreStableAndRoundTrip() {
        assertEquals(listOf("todo", "doing", "done"), TaskStatus.entries.map { it.id })
        TaskStatus.entries.forEach { assertEquals(it, converters.idToStatus(converters.statusToId(it))) }
    }

    @Test fun priorityIdsAreStableAndRoundTrip() {
        assertEquals(listOf("low", "high"), Priority.entries.map { it.id })
        Priority.entries.forEach { assertEquals(it, converters.idToPriority(converters.priorityToId(it))) }
    }

    @Test fun localizedOrUnknownIdsAreRejected() {
        assertThrows(NoSuchElementException::class.java) { converters.idToStatus("Selesai") }
        assertThrows(NoSuchElementException::class.java) { converters.idToPriority("Tinggi") }
    }

    @Test fun remoteIdsAreGeneratedAndPreservedByCopy() {
        val board = Board(name = "General")
        val task = Task(boardId = 1, title = "Synthetic task")
        assertNotEquals(board.remoteId, Board(name = "Other").remoteId)
        assertEquals(board.remoteId, board.copy(id = 1).remoteId)
        assertEquals(task.remoteId, task.copy(id = 1).remoteId)
    }
}
