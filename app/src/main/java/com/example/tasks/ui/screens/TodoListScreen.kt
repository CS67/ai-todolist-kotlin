package com.example.tasks.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import com.example.tasks.data.Todo
import com.example.tasks.preferences.rememberAIPreferences
import com.example.tasks.ui.components.AddTodoDialog
import com.example.tasks.ui.components.AIAddTodoDialog
import com.example.tasks.ui.components.AIConfigDialog
import com.example.tasks.ui.components.CollapsibleSectionHeader
import com.example.tasks.ui.components.EditTodoDialog
import com.example.tasks.ui.components.EmptyState
import com.example.tasks.ui.components.TodoItem
import com.example.tasks.viewmodel.TodoViewModel

/**
 * 主屏幕 - TodoList界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoListScreen(
    viewModelFactory: ViewModelProvider.Factory,
    viewModel: TodoViewModel = viewModel(factory = viewModelFactory)
) {
    // 初始化示例数据
//    LaunchedEffect(Unit) {
//        viewModel.initializeSampleData()
//    }
    
    val todos by viewModel.todos.collectAsState()
    val showAddDialog by viewModel.showAddDialog.collectAsState()
    val showAIAddDialog by viewModel.showAIAddDialog.collectAsState()
    val showAIConfigDialog by viewModel.showAIConfigDialog.collectAsState()
    val editingTodo by viewModel.editingTodo.collectAsState()
    val isIncompleteCollapsed by viewModel.isIncompleteCollapsed.collectAsState()
    val isCompletedCollapsed by viewModel.isCompletedCollapsed.collectAsState()
    
    // AI偏好设置
    val aiPreferences = rememberAIPreferences()
    var apiKey by remember { mutableStateOf(aiPreferences.getApiKey()) }
    
    val completedCount = todos.count { it.isCompleted }
    val totalCount = todos.size
    val incompleteCount = viewModel.getIncompleteCount()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Tasks",
                            style = MaterialTheme.typography.headlineSmall

                        )
                        if (totalCount > 0) {
                            Text(
                                text = "还有 $incompleteCount 个未完成任务",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                },
                actions = {
                    // AI设置按钮
                    IconButton(
                        onClick = { viewModel.showAIConfigDialog() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "AI设置",
                            tint = if (aiPreferences.isAIEnabled()) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            }
                        )
                    }
                    
                    if (completedCount > 0) {
                        IconButton(
                            onClick = { viewModel.clearCompletedTodos() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "清除已完成任务"
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // AI智能添加按钮（如果已配置API）
                if (aiPreferences.isAIEnabled()) {
                    SmallFloatingActionButton(
                        onClick = { viewModel.showAIAddDialog() },
                        containerColor = MaterialTheme.colorScheme.secondary
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "AI智能添加",
                            tint = MaterialTheme.colorScheme.onSecondary
                        )
                    }
                }
                
                // 常规添加按钮
                FloatingActionButton(
                    onClick = { viewModel.showAddDialog() },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "添加任务"
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (todos.isEmpty()) {
                EmptyState(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column {
                    // 统计信息卡片
                    if (totalCount > 0) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$totalCount",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "总任务",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$completedCount",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "已完成",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$incompleteCount",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "待完成",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                    
                    // Todo列表
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp) // 为FAB留出空间
                    ) {
                        // 首先显示未完成的任务
                        val incompleteTodos = todos.filter { !it.isCompleted }
                        val completedTodos = todos.filter { it.isCompleted }
                        
                        if (incompleteTodos.isNotEmpty()) {
                            item {
                                CollapsibleSectionHeader(
                                    title = "待完成",
                                    count = incompleteTodos.size,
                                    isCollapsed = isIncompleteCollapsed,
                                    onToggle = viewModel::toggleIncompleteCollapsed
                                )
                            }
                            
                            // 使用AnimatedVisibility来添加展开/折叠动画
                            item {
                                AnimatedVisibility(
                                    visible = !isIncompleteCollapsed,
                                    enter = expandVertically(),
                                    exit = shrinkVertically()
                                ) {
                                    Column {
                                        // 按优先级和截止日期排序的未完成任务
                                        val sortedIncompleteTodos = incompleteTodos.sortedWith(
                                            compareByDescending<Todo> { todo -> todo.priority.ordinal }
                                                .thenBy { todo -> todo.dueDate ?: Long.MAX_VALUE }
                                        )
                                        sortedIncompleteTodos.forEach { todo ->
                                            TodoItem(
                                                todo = todo,
                                                onToggleComplete = viewModel::toggleTodoCompletion,
                                                onDelete = viewModel::deleteTodo,
                                                onEdit = viewModel::startEditingTodo,
                                                onToggleSubTask = viewModel::toggleSubTaskCompletion,
                                                onAddSubTask = viewModel::addSubTask
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        if (completedTodos.isNotEmpty()) {
                            item {
                                CollapsibleSectionHeader(
                                    title = "已完成",
                                    count = completedTodos.size,
                                    isCollapsed = isCompletedCollapsed,
                                    onToggle = viewModel::toggleCompletedCollapsed,
                                    modifier = Modifier.padding(
                                        top = if (incompleteTodos.isNotEmpty()) 8.dp else 0.dp
                                    )
                                )
                            }
                            
                            // 使用AnimatedVisibility来添加展开/折叠动画
                            item {
                                AnimatedVisibility(
                                    visible = !isCompletedCollapsed,
                                    enter = expandVertically(),
                                    exit = shrinkVertically()
                                ) {
                                    Column {
                                        completedTodos.forEach { todo ->
                                            TodoItem(
                                                todo = todo,
                                                onToggleComplete = viewModel::toggleTodoCompletion,
                                                onDelete = viewModel::deleteTodo,
                                                onEdit = viewModel::startEditingTodo,
                                                onToggleSubTask = viewModel::toggleSubTaskCompletion,
                                                onAddSubTask = viewModel::addSubTask
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // 添加Todo对话框
    if (showAddDialog) {
        AddTodoDialog(
            onDismiss = { viewModel.hideAddDialog() },
            onConfirm = { title, description, priority, dueDate, subTasks ->
                viewModel.addTodo(title, description, priority, dueDate, subTasks)
            }
        )
    }
    
    // AI智能添加对话框
    if (showAIAddDialog) {
        AIAddTodoDialog(
            onDismiss = { viewModel.hideAIAddDialog() },
            onConfirm = { parsedTask ->
                viewModel.addTodoFromAI(parsedTask)
            },
            apiKey = apiKey
        )
    }
    
    // AI配置对话框
    if (showAIConfigDialog) {
        AIConfigDialog(
            currentApiKey = apiKey,
            onDismiss = { viewModel.hideAIConfigDialog() },
            onSaveApiKey = { newApiKey ->
                apiKey = newApiKey
                aiPreferences.saveApiKey(newApiKey)
            }
        )
    }
    
    // 编辑Todo对话框
    editingTodo?.let { todo ->
        EditTodoDialog(
            todo = todo,
            onDismiss = { viewModel.cancelEditingTodo() },
            onConfirm = { id, title, description, priority, dueDate, subTasks ->
                viewModel.updateTodo(id, title, description, priority, dueDate, subTasks)
            }
        )
    }
}