package com.example.todolist.data

import java.util.UUID

/**
 * Todo数据模型
 */
data class Todo(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)