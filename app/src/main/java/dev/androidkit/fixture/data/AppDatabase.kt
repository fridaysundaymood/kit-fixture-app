package dev.androidkit.fixture.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import dev.androidkit.fixture.R
import java.util.UUID

enum class TaskStatus(val id: String, private val label: Int) {
    Todo("todo", R.string.status_todo),
    Doing("doing", R.string.status_doing),
    Done("done", R.string.status_done);

    fun localized(context: Context): String = context.getString(label)

    companion object {
        fun fromId(id: String): TaskStatus = entries.first { it.id == id }
    }
}

enum class Priority(val id: String, private val label: Int) {
    Low("low", R.string.priority_low),
    High("high", R.string.priority_high);

    fun localized(context: Context): String = context.getString(label)

    companion object {
        fun fromId(id: String): Priority = entries.first { it.id == id }
    }
}

@Entity(tableName = "boards", indices = [Index(value = ["name"], unique = true)])
data class Board(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val remoteId: String = UUID.randomUUID().toString(),
)

@Entity(
    tableName = "tasks",
    foreignKeys = [ForeignKey(entity = Board::class, parentColumns = ["id"], childColumns = ["boardId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("boardId")],
)
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val boardId: Long,
    val title: String,
    val status: TaskStatus = TaskStatus.Todo,
    val priority: Priority = Priority.Low,
    val remoteId: String = UUID.randomUUID().toString(),
)

class TaskConverters {
    @TypeConverter fun statusToId(value: TaskStatus): String = value.id
    @TypeConverter fun idToStatus(value: String): TaskStatus = TaskStatus.fromId(value)
    @TypeConverter fun priorityToId(value: Priority): String = value.id
    @TypeConverter fun idToPriority(value: String): Priority = Priority.fromId(value)
}

@Dao
abstract class TaskDao {
    @Query("SELECT * FROM boards ORDER BY id")
    abstract fun boards(): List<Board>

    @Query("SELECT * FROM tasks WHERE boardId = :boardId ORDER BY id")
    abstract fun tasks(boardId: Long): List<Task>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract fun insertBoard(board: Board): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract fun insertTask(task: Task): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract fun insertSeed(board: Board): Long

    @Query("SELECT id FROM boards WHERE name = 'General' LIMIT 1")
    protected abstract fun generalId(): Long

    @Transaction
    open fun seedGeneral(): Long {
        insertSeed(Board(name = "General"))
        return generalId()
    }
}

@Database(entities = [Board::class, Task::class], version = 1, exportSchema = false)
@TypeConverters(TaskConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): TaskDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun open(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "fixture.db")
                .build().also { instance = it }
        }
    }
}
