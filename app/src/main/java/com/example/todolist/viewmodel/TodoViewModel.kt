package com.example.todolist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.todolist.data.Priority
import com.example.todolist.data.SubTask
import com.example.todolist.data.Todo
import com.example.todolist.repository.TodoRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

/**
 * Todo列表的ViewModel
 */
class TodoViewModel(private val repository: TodoRepository) : ViewModel() {
    
    // 从数据库获取todos
    val todos: StateFlow<List<Todo>> = repository.getAllTodos()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
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
    fun addTodo(
        title: String, 
        description: String = "", 
        priority: Priority = Priority.MEDIUM, 
        dueDate: Long? = null,
        subTasks: List<SubTask> = emptyList()
    ) {
        if (title.isBlank()) return
        
        val newTodo = Todo(
            title = title.trim(),
            description = description.trim(),
            priority = priority,
            dueDate = dueDate,
            subTasks = subTasks
        )
        
        viewModelScope.launch {
            repository.insertTodo(newTodo)
        }
    }
    
    /**
     * 切换Todo完成状态
     */
    fun toggleTodoCompletion(todoId: String) {
        viewModelScope.launch {
            val todo = repository.getTodoById(todoId)
            if (todo != null) {
                val updatedTodo = todo.copy(
                    isCompleted = !todo.isCompleted,
                    completedAt = if (!todo.isCompleted) System.currentTimeMillis() else null
                )
                repository.updateTodo(updatedTodo)
            }
        }
    }
    
    /**
     * 删除Todo
     */
    fun deleteTodo(todoId: String) {
        viewModelScope.launch {
            repository.deleteTodoById(todoId)
        }
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
        return todos.value.count { !it.isCompleted }
    }
    
    /**
     * 清除所有已完成的Todo
     */
    fun clearCompletedTodos() {
        viewModelScope.launch {
            repository.deleteCompletedTodos()
        }
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
    fun updateTodo(
        todoId: String, 
        title: String, 
        description: String, 
        priority: Priority = Priority.MEDIUM,
        dueDate: Long? = null,
        subTasks: List<SubTask> = emptyList()
    ) {
        if (title.isBlank()) return
        
        viewModelScope.launch {
            val todo = repository.getTodoById(todoId)
            if (todo != null) {
                val updatedTodo = todo.copy(
                    title = title.trim(),
                    description = description.trim(),
                    priority = priority,
                    dueDate = dueDate,
                    subTasks = subTasks
                )
                repository.updateTodo(updatedTodo)
            }
        }
        _editingTodo.value = null
    }
    
    /**
     * 切换子任务完成状态
     */
    fun toggleSubTaskCompletion(todoId: String, subTaskId: String) {
        viewModelScope.launch {
            val todo = repository.getTodoById(todoId)
            if (todo != null) {
                val updatedSubTasks = todo.subTasks.map { subTask ->
                    if (subTask.id == subTaskId) {
                        subTask.copy(isCompleted = !subTask.isCompleted)
                    } else {
                        subTask
                    }
                }
                repository.updateTodo(todo.copy(subTasks = updatedSubTasks))
            }
        }
    }
    
    /**
     * 添加子任务
     */
    fun addSubTask(todoId: String, subTaskTitle: String) {
        if (subTaskTitle.isBlank()) return
        
        viewModelScope.launch {
            val todo = repository.getTodoById(todoId)
            if (todo != null) {
                val newSubTask = SubTask(title = subTaskTitle.trim())
                repository.updateTodo(todo.copy(subTasks = todo.subTasks + newSubTask))
            }
        }
    }
    
    /**
     * 删除子任务
     */
    fun deleteSubTask(todoId: String, subTaskId: String) {
        viewModelScope.launch {
            val todo = repository.getTodoById(todoId)
            if (todo != null) {
                val updatedSubTasks = todo.subTasks.filter { it.id != subTaskId }
                repository.updateTodo(todo.copy(subTasks = updatedSubTasks))
            }
        }
    }
    
    /**
     * 根据优先级排序todos
     */
    fun sortTodosByPriority(): List<Todo> {
        val incomplete = todos.value.filter { !it.isCompleted }
            .sortedWith(compareByDescending<Todo> { todo -> todo.priority.ordinal }.thenBy { todo -> todo.dueDate ?: Long.MAX_VALUE })
        val completed = todos.value.filter { it.isCompleted }
        return incomplete + completed
    }
    
    /**
     * 初始化示例数据（仅在数据库为空时）
     */
    fun initializeSampleData() {
        viewModelScope.launch {
            val todoCount = repository.getTodoCount()
            if (todoCount == 0) {
                val sampleTodos = listOf(
                    Todo(
                        title = "App开发",
                        description = "Todo List 待办清单开发",
                        priority = Priority.HIGH,
                        dueDate = System.currentTimeMillis() + 2 * 24 * 60 * 60 * 1000, // 2天后
                        subTasks = listOf(
                            SubTask(title = "App开发", isCompleted = true),
                            SubTask(title = "编写报告"),
                            SubTask(title = "编写PPT")
                        )
                    ),
                    Todo(
                        title = "吃饭",
                        description = "提前半小时点外卖",
                        isCompleted = true,
                        priority = Priority.LOW,
                        completedAt = System.currentTimeMillis() - 60 * 60 * 1000 // 1小时前完成
                    ),
                    Todo(
                        title = "锻炼身体", 
                        description = "跑步30分钟",
                        priority = Priority.MEDIUM,
                        dueDate = System.currentTimeMillis() + 12 * 60 * 60 * 1000 // 12小时后
                    ),
                    Todo(
                        title = "阅读书籍",
                        description = "《Jetpack Compose实战》第3章", 
                        isCompleted = true,
                        priority = Priority.LOW,
                        completedAt = System.currentTimeMillis() - 2 * 60 * 60 * 1000, // 2小时前完成
                        subTasks = listOf(
                            SubTask(title = "阅读第一节", isCompleted = true),
                            SubTask(title = "实践例子", isCompleted = true)
                        )
                    )
                )
                
                sampleTodos.forEach { todo ->
                    repository.insertTodo(todo)
                }
            }
        }
    }

    /**
     * TodoViewModel工厂类 - 合并到同一文件中
     */
    class Factory(
        private val repository: TodoRepository
    ) : ViewModelProvider.Factory {
        
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TodoViewModel::class.java)) {
                return TodoViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}