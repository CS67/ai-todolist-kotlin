package com.example.tasks.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.tasks.data.Priority
import com.example.tasks.data.SubTask
import com.example.tasks.data.Todo
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
    var selectedDate by remember { mutableStateOf(todo.dueDate) }
    var subTasks by remember { mutableStateOf(todo.subTasks) }
    var newSubTaskTitle by remember { mutableStateOf("") }
    
    val context = LocalContext.current
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.98f)
                .fillMaxHeight(0.8f)
                .padding(8.dp),
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
                        onClick = {
                            // 使用系统日期选择器
                            val calendar = Calendar.getInstance()
                            selectedDate?.let { calendar.timeInMillis = it }
                            
                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    val selectedCalendar = Calendar.getInstance().apply {
                                        set(year, month, dayOfMonth)
                                        // 保持之前设置的时间，如果没有则默认为9:00
                                        if (selectedDate == null) {
                                            set(Calendar.HOUR_OF_DAY, 9)
                                            set(Calendar.MINUTE, 0)
                                        } else {
                                            val prevCalendar = Calendar.getInstance().apply { 
                                                timeInMillis = selectedDate!! 
                                            }
                                            set(Calendar.HOUR_OF_DAY, prevCalendar.get(Calendar.HOUR_OF_DAY))
                                            set(Calendar.MINUTE, prevCalendar.get(Calendar.MINUTE))
                                        }
                                        set(Calendar.SECOND, 0)
                                        set(Calendar.MILLISECOND, 0)
                                    }
                                    
                                    // 选择完日期后，弹出时间选择器
                                    TimePickerDialog(
                                        context,
                                        { _, hourOfDay, minute ->
                                            selectedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                                            selectedCalendar.set(Calendar.MINUTE, minute)
                                            selectedDate = selectedCalendar.timeInMillis
                                        },
                                        selectedCalendar.get(Calendar.HOUR_OF_DAY),
                                        selectedCalendar.get(Calendar.MINUTE),
                                        true
                                    ).show()
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }
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
}