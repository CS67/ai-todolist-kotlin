package com.example.tasks.data

import java.util.UUID

/**
 * 优先级枚举
 */
enum class Priority(val displayName: String, val color: androidx.compose.ui.graphics.Color) {
    LOW("低", androidx.compose.ui.graphics.Color(0xFF4CAF50)),
    MEDIUM("中", androidx.compose.ui.graphics.Color(0xFFFFC107)),
    HIGH("高", androidx.compose.ui.graphics.Color(0xFFFF9800)),
    URGENT("紧急", androidx.compose.ui.graphics.Color(0xFFF44336))
}

/**
 * 子任务数据模型
 */
data class SubTask(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Todo数据模型
 */
data class Todo(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val priority: Priority = Priority.MEDIUM,
    val dueDate: Long? = null, // 截止日期时间戳
    val subTasks: List<SubTask> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
) {
    /**
     * 获取已完成的子任务数量
     */
    val completedSubTasksCount: Int
        get() = subTasks.count { it.isCompleted }
    
    /**
     * 获取子任务完成进度 (0.0 - 1.0)
     */
    val subTaskProgress: Float
        get() = if (subTasks.isEmpty()) 1f else completedSubTasksCount.toFloat() / subTasks.size
    
    /**
     * 检查是否过期
     */
    val isOverdue: Boolean
        get() = dueDate != null && dueDate < System.currentTimeMillis() && !isCompleted
    
    /**
     * 检查是否即将到期 (24小时内)
     */
    val isDueSoon: Boolean
        get() = dueDate != null && !isCompleted && 
                dueDate > System.currentTimeMillis() && 
                dueDate <= System.currentTimeMillis() + 24 * 60 * 60 * 1000
}