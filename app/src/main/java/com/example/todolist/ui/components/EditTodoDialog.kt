package com.example.todolist.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.todolist.data.Priority
import com.example.todolist.data.SubTask
import com.example.todolist.data.Todo
import java.text.SimpleDateFormat
import java.util.*

/**
 * 编辑Todo的对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTodoDialog(
    todo: Todo,
    onDismiss: () -> Unit,
    onConfirm: (id: String, title: String, description: String, priority: Priority, dueDate: Long?, subTasks: List<SubTask>) -> Unit
) {
    var title by remember { mutableStateOf(todo.title) }
    var description by remember { mutableStateOf(todo.description) }
    var selectedPriority by remember { mutableStateOf(todo.priority) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(todo.dueDate) }
    var subTasks by remember { mutableStateOf(todo.subTasks) }
    var newSubTaskTitle by remember { mutableStateOf("") }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "编辑任务",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                // 任务标题
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("任务标题 *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // 任务描述
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("任务描述（可选）") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                
                // 优先级选择
                Text(
                    text = "优先级",
                    style = MaterialTheme.typography.labelLarge
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Priority.values().forEach { priority ->
                        FilterChip(
                            onClick = { selectedPriority = priority },
                            label = { Text(priority.displayName) },
                            selected = selectedPriority == priority,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = priority.color.copy(alpha = 0.2f),
                                selectedLabelColor = priority.color
                            )
                        )
                    }
                }
                
                // 截止日期
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "截止日期",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.weight(1f)
                    )
                    
                    TextButton(
                        onClick = { showDatePicker = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "选择日期"
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (selectedDate != null) {
                                SimpleDateFormat("MM月dd日 HH:mm", Locale.getDefault())
                                    .format(Date(selectedDate!!))
                            } else {
                                "设置截止日期"
                            }
                        )
                    }
                    
                    if (selectedDate != null) {
                        IconButton(
                            onClick = { selectedDate = null }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "清除日期"
                            )
                        }
                    }
                }
                
                // 子任务
                Text(
                    text = "子任务",
                    style = MaterialTheme.typography.labelLarge
                )
                
                // 添加子任务输入框
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newSubTaskTitle,
                        onValueChange = { newSubTaskTitle = it },
                        placeholder = { Text("输入子任务标题") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    
                    IconButton(
                        onClick = {
                            if (newSubTaskTitle.isNotBlank()) {
                                subTasks = subTasks + SubTask(title = newSubTaskTitle.trim())
                                newSubTaskTitle = ""
                            }
                        },
                        enabled = newSubTaskTitle.isNotBlank()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "添加子任务"
                        )
                    }
                }
                
                // 子任务列表
                if (subTasks.isNotEmpty()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        subTasks.forEachIndexed { index, subTask ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = subTask.isCompleted,
                                    onCheckedChange = { isChecked ->
                                        subTasks = subTasks.mapIndexed { i, task ->
                                            if (i == index) task.copy(isCompleted = isChecked) else task
                                        }
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = MaterialTheme.colorScheme.secondary
                                    ),
                                    modifier = Modifier.size(20.dp)
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Text(
                                    text = subTask.title,
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                
                                IconButton(
                                    onClick = {
                                        subTasks = subTasks.filterIndexed { i, _ -> i != index }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "删除子任务",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                
                // 按钮行
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text("取消")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onConfirm(todo.id, title, description, selectedPriority, selectedDate, subTasks)
                            }
                        },
                        enabled = title.isNotBlank()
                    ) {
                        Text("保存")
                    }
                }
            }
        }
    }
    
    // 日期选择器
    if (showDatePicker) {
        DatePickerDialog(
            onDateSelected = { date ->
                selectedDate = date
                showDatePicker = false
            },
            onDismiss = {
                showDatePicker = false
            },
            initialDate = selectedDate
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit,
    initialDate: Long? = null
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate ?: System.currentTimeMillis()
    )
    
    // 从初始日期获取时间，如果没有则默认为9:00
    val initialCalendar = Calendar.getInstance().apply {
        timeInMillis = initialDate ?: System.currentTimeMillis()
    }
    var selectedHour by remember { mutableStateOf(initialCalendar.get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableStateOf(initialCalendar.get(Calendar.MINUTE)) }
    var hourText by remember { mutableStateOf(initialCalendar.get(Calendar.HOUR_OF_DAY).toString()) }
    var minuteText by remember { mutableStateOf(initialCalendar.get(Calendar.MINUTE).toString().padStart(2, '0')) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val selectedDateMillis = datePickerState.selectedDateMillis
                    if (selectedDateMillis != null) {
                        val calendar = Calendar.getInstance().apply {
                            timeInMillis = selectedDateMillis
                            set(Calendar.HOUR_OF_DAY, selectedHour)
                            set(Calendar.MINUTE, selectedMinute)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        onDateSelected(calendar.timeInMillis)
                    } else {
                        // 如果没有选择日期，使用当前日期
                        val calendar = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, selectedHour)
                            set(Calendar.MINUTE, selectedMinute)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        onDateSelected(calendar.timeInMillis)
                    }
                }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        text = {
            Column {
                DatePicker(state = datePickerState)
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                // 时间选择器
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "小时",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = hourText,
                            onValueChange = { value ->
                                hourText = value
                                value.toIntOrNull()?.let { hour ->
                                    if (hour in 0..23) selectedHour = hour
                                } ?: run {
                                    if (value.isEmpty()) selectedHour = 0
                                }
                            },
                            modifier = Modifier.width(80.dp),
                            singleLine = true
                        )
                    }
                    
                    Text(
                        text = ":",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "分钟",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = minuteText,
                            onValueChange = { value ->
                                minuteText = value
                                value.toIntOrNull()?.let { minute ->
                                    if (minute in 0..59) selectedMinute = minute
                                } ?: run {
                                    if (value.isEmpty()) selectedMinute = 0
                                }
                            },
                            modifier = Modifier.width(80.dp),
                            singleLine = true
                        )
                    }
                }
            }
        }
    )
}