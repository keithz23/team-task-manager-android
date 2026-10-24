package com.example.taskmanager.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.taskmanager.core.database.dao.CommentDao
import com.example.taskmanager.core.database.dao.ProjectDao
import com.example.taskmanager.core.database.dao.TaskDao
import com.example.taskmanager.core.database.dao.TeamDao
import com.example.taskmanager.core.database.dao.UserDao
import com.example.taskmanager.core.database.entity.CommentEntity
import com.example.taskmanager.core.database.entity.ProjectEntity
import com.example.taskmanager.core.database.entity.TaskEntity
import com.example.taskmanager.core.database.entity.TeamEntity
import com.example.taskmanager.core.database.entity.TeamMemberEntity
import com.example.taskmanager.core.database.entity.UserEntity
import com.example.taskmanager.core.model.Priority
import com.example.taskmanager.core.model.TaskStatus
import com.example.taskmanager.core.model.TeamRole

class Converters {
    @TypeConverter fun fromRole(v: TeamRole) = v.name
    @TypeConverter fun toRole(v: String) = TeamRole.valueOf(v)
    @TypeConverter fun fromStatus(v: TaskStatus) = v.name
    @TypeConverter fun toStatus(v: String) = TaskStatus.valueOf(v)
    @TypeConverter fun fromPriority(v: Priority) = v.name
    @TypeConverter fun toPriority(v: String) = Priority.valueOf(v)
}

@Database(
    entities = [UserEntity::class, TeamEntity::class, TeamMemberEntity::class,
        ProjectEntity::class, TaskEntity::class, CommentEntity::class],
    version = 2,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun teamDao(): TeamDao
    abstract fun projectDao(): ProjectDao
    abstract fun taskDao(): TaskDao
    abstract fun commentDao(): CommentDao
}
