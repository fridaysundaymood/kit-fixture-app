package dev.androidkit.fixture

import android.content.Context
import androidx.compose.material3.SnackbarHostState
import dev.androidkit.fixture.data.TaskStatus

object FixtureBehavior {
    fun statusText(context: Context, status: TaskStatus): String = status.localized(context)
    fun statusForSync(context: Context, status: TaskStatus): String = status.id
    suspend fun syncError(snackbar: SnackbarHostState, error: Exception, message: String) { snackbar.showSnackbar(message) }
    suspend fun saveError(snackbar: SnackbarHostState, error: Exception, message: String) { snackbar.showSnackbar(message) }
}
