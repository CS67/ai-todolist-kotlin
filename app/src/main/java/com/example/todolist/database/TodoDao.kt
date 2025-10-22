package com.example.todolist.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Todo数据访问对象
 */
@Dao
interface TodoDao {
    
    /**
     * 获取所有待办事项
     */
    @Query("SELECT * FROM todos ORDER BY createdAt DESC")
    fun getAllTodos(): Flow<List<TodoEntity>>
    
    /**
     * 根据ID获取待办事项
     */
    @Query("SELECT * FROM todos WHERE id = :id")
    suspend fun getTodoById(id: String): TodoEntity?
    
    /**
     * 获取未完成的待办事项
     */
    @Query("SELECT * FROM todos WHERE isCompleted = 0 ORDER BY priority DESC, dueDate ASC")
    fun getIncompleteTodos(): Flow<List<TodoEntity>>
    
    /**
     * 获取已完成的待办事项
     */
    @Query("SELECT * FROM todos WHERE isCompleted = 1 ORDER BY completedAt DESC")
    fun getCompletedTodos(): Flow<List<TodoEntity>>
    
    /**
     * 插入新的待办事项
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodo(todo: TodoEntity)
    
    /**
     * 更新待办事项
     */
    @Update
    suspend fun updateTodo(todo: TodoEntity)
    
    /**
     * 删除待办事项
     */
    @Delete
    suspend fun deleteTodo(todo: TodoEntity)
    
    /**
     * 根据ID删除待办事项
     */
    @Query("DELETE FROM todos WHERE id = :id")
    suspend fun deleteTodoById(id: String)
    
    /**
     * 删除所有已完成的待办事项
     */
    @Query("DELETE FROM todos WHERE isCompleted = 1")
    suspend fun deleteCompletedTodos()
    
    /**
     * 获取待办事项总数
     */
    @Query("SELECT COUNT(*) FROM todos")
    suspend fun getTodoCount(): Int
    
    /**
     * 获取未完成待办事项数量
     */
    @Query("SELECT COUNT(*) FROM todos WHERE isCompleted = 0")
    suspend fun getIncompleteCount(): Int
    
    /**
     * 获取已完成待办事项数量
     */
    @Query("SELECT COUNT(*) FROM todos WHERE isCompleted = 1")
    suspend fun getCompletedCount(): Int
}