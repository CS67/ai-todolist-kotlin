package com.example.todolist.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.todolist.data.Priority
import com.example.todolist.data.Todo
import java.text.SimpleDateFormat
import java.util.*

/**
 * 单个Todo项组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoItem(
    todo: Todo,
    onToggleComplete: (String) -> Unit,
    onDelete: (String) -> Unit,
    onEdit: (Todo) -> Unit,
    onToggleSubTask: ((String, String) -> Unit)? = null,
    onAddSubTask: ((String, String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .animateContentSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (todo.isCompleted) 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            else 
                MaterialTheme.colorScheme.surface
        ),
        border = if (todo.isOverdue || todo.isDueSoon) 
            androidx.compose.foundation.BorderStroke(2.dp, 
                if (todo.isOverdue) MaterialTheme.colorScheme.error 
                else MaterialTheme.colorScheme.tertiary
            ) else null
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // 主要内容行
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                // 完成状态复选框
                Checkbox(
                    checked = todo.isCompleted,
                    onCheckedChange = { onToggleComplete(todo.id) },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary
                    )
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Todo内容
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { 
                            if (todo.subTasks.isNotEmpty()) {
                                expanded = !expanded
                            } else {
                                onEdit(todo)
                            }
                        }
                ) {
                    // 标题和优先级
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = todo.title,
                            style = MaterialTheme.typography.bodyLarge,
                            textDecoration = if (todo.isCompleted) TextDecoration.LineThrough else null,
                            color = if (todo.isCompleted) 
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            else 
                                MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                            maxLines = if (expanded) Int.MAX_VALUE else 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        // 优先级标签
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(todo.priority.color)
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        Text(
                            text = todo.priority.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = todo.priority.color,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    if (todo.description.isNotBlank()) {
                        Text(
                            text = todo.description,
                            style = MaterialTheme.typography.bodyMedium,
                            textDecoration = if (todo.isCompleted) TextDecoration.LineThrough else null,
                            color = if (todo.isCompleted) 
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            else 
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            maxLines = if (expanded) Int.MAX_VALUE else 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    
                    // 截止日期
                    todo.dueDate?.let { dueDate ->
                        val dateFormat = SimpleDateFormat("MM月dd日 HH:mm", Locale.getDefault())
                        val dueDateText = "截止: ${dateFormat.format(Date(dueDate))}"
                        val color = when {
                            todo.isOverdue -> MaterialTheme.colorScheme.error
                            todo.isDueSoon -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "截止日期",
                                tint = color,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = dueDateText,
                                style = MaterialTheme.typography.bodySmall,
                                color = color,
                                fontWeight = if (todo.isOverdue || todo.isDueSoon) FontWeight.Medium else FontWeight.Normal
                            )
                        }
                    }
                    
                    // 子任务进度
                    if (todo.subTasks.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 6.dp)
                        ) {
                            LinearProgressIndicator(
                                progress = { todo.subTaskProgress },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${todo.completedSubTasksCount}/${todo.subTasks.size}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            
                            Icon(
                                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = if (expanded) "收起" else "展开",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    

                }
                
                // 操作按钮
                Column {
                    IconButton(
                        onClick = { onEdit(todo) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "编辑",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    IconButton(
                        onClick = { onDelete(todo.id) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "删除",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            // 展开的子任务列表
            if (expanded && todo.subTasks.isNotEmpty()) {
                Divider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                
                Column(
                    modifier = Modifier.padding(start = 32.dp, end = 16.dp, bottom = 16.dp, top = 8.dp)
                ) {
                    Text(
                        text = "子任务",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    todo.subTasks.forEach { subTask ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        ) {
                            Checkbox(
                                checked = subTask.isCompleted,
                                onCheckedChange = { 
                                    onToggleSubTask?.invoke(todo.id, subTask.id)
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colorScheme.secondary
                                ),
                                modifier = Modifier.size(20.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = subTask.title,
                                style = MaterialTheme.typography.bodyMedium,
                                textDecoration = if (subTask.isCompleted) TextDecoration.LineThrough else null,
                                color = if (subTask.isCompleted) 
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                else 
                                    MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}