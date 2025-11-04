package com.example.tasks.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.tasks.data.Priority
import com.example.tasks.data.SubTask
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Todo数据库实体
 */
@Entity(tableName = "todos")
@TypeConverters(Converters::class)
data class TodoEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val isCompleted: Boolean,
    val priority: Priority,
    val dueDate: Long?,
    val subTasks: List<SubTask>,
    val createdAt: Long,
    val completedAt: Long?
)

/**
 * Room类型转换器
 */
class Converters {
    private val gson = Gson()
    
    @TypeConverter
    fun fromPriority(priority: Priority): String {
        return priority.name
    }
    
    @TypeConverter
    fun toPriority(priorityName: String): Priority {
        return Priority.valueOf(priorityName)
    }
    
    @TypeConverter
    fun fromSubTaskList(subTasks: List<SubTask>): String {
        return gson.toJson(subTasks)
    }
    
    @TypeConverter
    fun toSubTaskList(subTasksJson: String): List<SubTask> {
        val listType = object : TypeToken<List<SubTask>>() {}.type
        return gson.fromJson(subTasksJson, listType) ?: emptyList()
    }
}