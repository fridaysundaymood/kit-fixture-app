package dev.androidkit.fixture

import android.content.Context
import androidx.compose.material3.SnackbarHostState
import dev.androidkit.fixture.data.TaskStatus

object FixtureBehavior {
    fun statusText(context: Context, status: TaskStatus): String = status.localized(context)
    fun statusForSync(context: Context, status: TaskStatus): String = status.id
    suspend fun syncError(snackbar: SnackbarHostState, error: Exception, message: String) { snackbar.showSnackbar(message) }
    suspend fun saveError(context: Context, snackbar: SnackbarHostState, error: Exception) {
        try { throw UserFacingException(context.getString(R.string.task_could_not_be_saved), error) }
        catch (e: UserFacingException) { snackbar.showSnackbar(requireNotNull(e.message)) }
    }
}
