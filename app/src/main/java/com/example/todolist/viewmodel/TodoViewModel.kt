package com.example.todolist.viewmodel

import androidx.lifecycle.ViewModel
import com.example.todolist.data.Todo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Todo列表的ViewModel
 */
class TodoViewModel : ViewModel() {
    
    private val _todos = MutableStateFlow<List<Todo>>(
        // 添加一些示例数据
        listOf(
            Todo(title = "完成工作报告", description = "整理本月的工作总结和下月计划"),
            Todo(title = "买菜", description = "购买明天的午餐食材", isCompleted = true),
            Todo(title = "锻炼身体", description = "跑步30分钟"),
            Todo(title = "阅读技术书籍", description = "《Jetpack Compose实战》第3章", isCompleted = true)
        )
    )
    val todos: StateFlow<List<Todo>> = _todos.asStateFlow()
    
    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()
    
    // 折叠状态管理
    private val _isIncompleteCollapsed = MutableStateFlow(false)
    val isIncompleteCollapsed: StateFlow<Boolean> = _isIncompleteCollapsed.asStateFlow()
    
    private val _isCompletedCollapsed = MutableStateFlow(false)
    val isCompletedCollapsed: StateFlow<Boolean> = _isCompletedCollapsed.asStateFlow()
    
    // 编辑Todo相关状态
    private val _editingTodo = MutableStateFlow<Todo?>(null)
    val editingTodo: StateFlow<Todo?> = _editingTodo.asStateFlow()
    
    /**
     * 添加新的Todo
     */
    fun addTodo(title: String, description: String = "") {
        if (title.isBlank()) return
        
        val newTodo = Todo(
            title = title.trim(),
            description = description.trim()
        )
        
        _todos.value = _todos.value + newTodo
    }
    
    /**
     * 切换Todo完成状态
     */
    fun toggleTodoCompletion(todoId: String) {
        _todos.value = _todos.value.map { todo ->
            if (todo.id == todoId) {
                todo.copy(
                    isCompleted = !todo.isCompleted,
                    completedAt = if (!todo.isCompleted) System.currentTimeMillis() else null
                )
            } else {
                todo
            }
        }
    }
    
    /**
     * 删除Todo
     */
    fun deleteTodo(todoId: String) {
        _todos.value = _todos.value.filter { it.id != todoId }
    }
    
    /**
     * 显示添加对话框
     */
    fun showAddDialog() {
        _showAddDialog.value = true
    }
    
    /**
     * 隐藏添加对话框
     */
    fun hideAddDialog() {
        _showAddDialog.value = false
    }
    
    /**
     * 获取未完成的Todo数量
     */
    fun getIncompleteCount(): Int {
        return _todos.value.count { !it.isCompleted }
    }
    
    /**
     * 清除所有已完成的Todo
     */
    fun clearCompletedTodos() {
        _todos.value = _todos.value.filter { !it.isCompleted }
    }
    
    /**
     * 切换未完成任务列表的折叠状态
     */
    fun toggleIncompleteCollapsed() {
        _isIncompleteCollapsed.value = !_isIncompleteCollapsed.value
    }
    
    /**
     * 切换已完成任务列表的折叠状态
     */
    fun toggleCompletedCollapsed() {
        _isCompletedCollapsed.value = !_isCompletedCollapsed.value
    }
    
    /**
     * 开始编辑Todo
     */
    fun startEditingTodo(todo: Todo) {
        _editingTodo.value = todo
    }
    
    /**
     * 取消编辑Todo
     */
    fun cancelEditingTodo() {
        _editingTodo.value = null
    }
    
    /**
     * 更新Todo
     */
    fun updateTodo(todoId: String, title: String, description: String) {
        if (title.isBlank()) return
        
        _todos.value = _todos.value.map { todo ->
            if (todo.id == todoId) {
                todo.copy(
                    title = title.trim(),
                    description = description.trim()
                )
            } else {
                todo
            }
        }
        _editingTodo.value = null
    }
}