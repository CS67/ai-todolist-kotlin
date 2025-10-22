package com.example.todolist.repository

import com.example.todolist.data.Priority
import com.example.todolist.data.SubTask
import com.example.todolist.data.Todo
import com.example.todolist.database.TodoDao
import com.example.todolist.database.TodoEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Todo数据仓库
 */
class TodoRepository(private val todoDao: TodoDao) {
    
    /**
     * 获取所有待办事项
     */
    fun getAllTodos(): Flow<List<Todo>> {
        return todoDao.getAllTodos().map { entities ->
            entities.map { it.toTodo() }
        }
    }
    
    /**
     * 根据ID获取待办事项
     */
    suspend fun getTodoById(id: String): Todo? {
        return todoDao.getTodoById(id)?.toTodo()
    }
    
    /**
     * 获取未完成的待办事项
     */
    fun getIncompleteTodos(): Flow<List<Todo>> {
        return todoDao.getIncompleteTodos().map { entities ->
            entities.map { it.toTodo() }
        }
    }
    
    /**
     * 获取已完成的待办事项
     */
    fun getCompletedTodos(): Flow<List<Todo>> {
        return todoDao.getCompletedTodos().map { entities ->
            entities.map { it.toTodo() }
        }
    }
    
    /**
     * 插入新的待办事项
     */
    suspend fun insertTodo(todo: Todo) {
        todoDao.insertTodo(todo.toEntity())
    }
    
    /**
     * 更新待办事项
     */
    suspend fun updateTodo(todo: Todo) {
        todoDao.updateTodo(todo.toEntity())
    }
    
    /**
     * 删除待办事项
     */
    suspend fun deleteTodo(todo: Todo) {
        todoDao.deleteTodo(todo.toEntity())
    }
    
    /**
     * 根据ID删除待办事项
     */
    suspend fun deleteTodoById(id: String) {
        todoDao.deleteTodoById(id)
    }
    
    /**
     * 删除所有已完成的待办事项
     */
    suspend fun deleteCompletedTodos() {
        todoDao.deleteCompletedTodos()
    }
    
    /**
     * 获取待办事项总数
     */
    suspend fun getTodoCount(): Int {
        return todoDao.getTodoCount()
    }
    
    /**
     * 获取未完成待办事项数量
     */
    suspend fun getIncompleteCount(): Int {
        return todoDao.getIncompleteCount()
    }
    
    /**
     * 获取已完成待办事项数量
     */
    suspend fun getCompletedCount(): Int {
        return todoDao.getCompletedCount()
    }
}

/**
 * TodoEntity转换为Todo的扩展函数
 */
fun TodoEntity.toTodo(): Todo {
    return Todo(
        id = id,
        title = title,
        description = description,
        isCompleted = isCompleted,
        priority = priority,
        dueDate = dueDate,
        subTasks = subTasks,
        createdAt = createdAt,
        completedAt = completedAt
    )
}

/**
 * Todo转换为TodoEntity的扩展函数
 */
fun Todo.toEntity(): TodoEntity {
    return TodoEntity(
        id = id,
        title = title,
        description = description,
        isCompleted = isCompleted,
        priority = priority,
        dueDate = dueDate,
        subTasks = subTasks,
        createdAt = createdAt,
        completedAt = completedAt
    )
}