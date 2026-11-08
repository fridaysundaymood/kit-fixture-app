package dev.androidkit.fixture

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.androidkit.fixture.data.AppDatabase
import dev.androidkit.fixture.data.Board
import dev.androidkit.fixture.data.Priority
import dev.androidkit.fixture.data.SyncClient
import dev.androidkit.fixture.data.Task
import dev.androidkit.fixture.data.TaskStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        val language = newBase.getSharedPreferences("settings", MODE_PRIVATE).getString("language", "en") ?: "en"
        val configuration = Configuration(newBase.resources.configuration)
        configuration.setLocale(Locale(language))
        super.attachBaseContext(newBase.createConfigurationContext(configuration))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { Taskboard(this) } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Taskboard(activity: MainActivity) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = remember { AppDatabase.open(context.applicationContext) }
    val dao = remember { database.dao() }
    val sync = remember { SyncClient(context.applicationContext) }
    val snackbar = remember { SnackbarHostState() }
    var page by remember { mutableStateOf("boards") }
    var boards by remember { mutableStateOf<List<Board>>(emptyList()) }
    var tasks by remember { mutableStateOf<List<Task>>(emptyList()) }
    var selected by remember { mutableStateOf<Board?>(null) }
    var title by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(TaskStatus.Todo) }
    var priority by remember { mutableStateOf(Priority.Low) }
    var endpoint by remember { mutableStateOf("") }
    var key by remember { mutableStateOf("") }
    val defects = BuildConfig.FLAVOR == "defects"
    val syncFailed = stringResource(R.string.sync_failed)
    val syncComplete = stringResource(R.string.sync_complete)

    suspend fun refresh() {
        val result = withContext(Dispatchers.IO) {
            dao.seedGeneral()
            val all = dao.boards()
            all to (selected?.let { dao.tasks(it.id) } ?: emptyList())
        }
        boards = result.first
        tasks = result.second
    }
    LaunchedEffect(selected) { refresh() }
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(when (page) {
                "boards" -> R.string.boards
                "settings" -> R.string.settings
                "add" -> R.string.add_task
                else -> R.string.app_name
            })) }, navigationIcon = {
                if (page != "boards") TextButton(onClick = { page = if (page == "add") "board" else "boards" }) { Text(stringResource(R.string.back)) }
            }, actions = {
                if (page == "boards") IconButton(onClick = { page = "settings" }) {
                    Icon(Icons.Default.Settings, contentDescription = if (defects) null else stringResource(R.string.settings))
                }
            })
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            when (page) {
                "boards" -> {
                    Text(stringResource(R.string.boards), style = MaterialTheme.typography.headlineMedium)
                    boards.forEach { board ->
                        Text(board.name, Modifier.fillMaxWidth().clickable { selected = board; page = "board" }.padding(vertical = 16.dp), style = MaterialTheme.typography.titleLarge)
                    }
                    Button(onClick = {
                        scope.launch {
                            try {
                                withContext(Dispatchers.IO) {
                                    val allTasks = boards.flatMap { dao.tasks(it.id) }
                                    sync.sync(boards, allTasks) { FixtureBehavior.statusForSync(context, it) }
                                }
                                snackbar.showSnackbar(syncComplete)
                            } catch (e: Exception) {
                                FixtureBehavior.syncError(snackbar, e, syncFailed)
                            }
                        }
                    }) { Text(stringResource(R.string.sync)) }
                }
                "board" -> {
                    Text(selected?.name.orEmpty(), style = MaterialTheme.typography.headlineMedium)
                    if (tasks.isEmpty()) Text(stringResource(R.string.no_tasks_yet))
                    LazyColumn(Modifier.weight(1f)) {
                        items(tasks, key = { it.id }) { task ->
                            Column(Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                                Text(task.title, style = MaterialTheme.typography.titleMedium)
                                Text(FixtureBehavior.statusText(context, task.status))
                                Text(task.priority.localized(context))
                            }
                        }
                    }
                    Button(onClick = { page = "add" }) { Text(stringResource(R.string.add_task)) }
                }
                "add" -> {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text(stringResource(R.string.task_title)) }, singleLine = true)
                    Text(stringResource(R.string.select_status))
                    Row { TaskStatus.entries.forEach { value -> TextButton(onClick = { status = value }) { Text(value.localized(context) + if (status == value) " ✓" else "") } } }
                    Text(stringResource(R.string.select_priority))
                    Row { Priority.entries.forEach { value -> TextButton(onClick = { priority = value }) { Text(value.localized(context) + if (priority == value) " ✓" else "") } } }
                    Button(enabled = title.isNotBlank(), onClick = {
                        scope.launch {
                            try {
                                val board = checkNotNull(selected)
                                withContext(Dispatchers.IO) { dao.insertTask(Task(boardId = board.id, title = title.trim(), status = status, priority = priority)) }
                                title = ""
                                refresh()
                                page = "board"
                            } catch (e: Exception) {
                                FixtureBehavior.saveError(context, snackbar, e)
                            }
                        }
                    }) { Text(stringResource(R.string.save)) }
                }
                "settings" -> {
                    Text(stringResource(R.string.language), style = MaterialTheme.typography.titleLarge)
                    TextButton(onClick = { context.getSharedPreferences("settings", Context.MODE_PRIVATE).edit().putString("language", "en").apply(); activity.recreate() }) { Text(stringResource(R.string.english)) }
                    TextButton(onClick = { context.getSharedPreferences("settings", Context.MODE_PRIVATE).edit().putString("language", "in").apply(); activity.recreate() }) { Text(stringResource(R.string.bahasa_indonesia)) }
                    Text(stringResource(R.string.members), style = MaterialTheme.typography.titleLarge)
                    Text(stringResource(R.string.no_members))
                    if (BuildConfig.DEBUG) {
                        Text(stringResource(R.string.debug_options), style = MaterialTheme.typography.titleLarge)
                        OutlinedTextField(endpoint, { endpoint = it }, label = { Text(stringResource(R.string.endpoint)) }, singleLine = true)
                        OutlinedTextField(key, { key = it }, label = { Text(stringResource(R.string.api_key)) }, singleLine = true)
                        TextButton(onClick = { sync.setDebugOverride(endpoint, key) }) { Text(stringResource(R.string.apply)) }
                        if (defects) TextButton(onClick = { error("Synthetic crash") }) { Text(stringResource(R.string.crash_test)) }
                    }
                }
            }
        }
    }
}
