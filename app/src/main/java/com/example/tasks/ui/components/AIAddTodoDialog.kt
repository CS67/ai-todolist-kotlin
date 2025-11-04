package com.example.tasks.ui.components

import android.Manifest
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.tasks.ai.AITaskParser
import com.example.tasks.ai.ParsedTask
import kotlinx.coroutines.launch

/**
 * AI智能添加任务对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIAddTodoDialog(
    onDismiss: () -> Unit,
    onConfirm: (ParsedTask) -> Unit,
    apiKey: String?
) {
    var userInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var parsedTask by remember { mutableStateOf<ParsedTask?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isListening by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    // 语音识别权限请求
    val speechPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // 权限获得后，启动语音识别
            isListening = true
        } else {
            errorMessage = "需要麦克风权限才能使用语音输入"
            isListening = false
        }
    }
    
    // 语音识别结果处理
    val speechRecognitionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isListening = false
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!matches.isNullOrEmpty()) {
                userInput = matches[0]
            }
        } else {
            errorMessage = "语音识别取消或失败"
        }
    }
    
    // 启动语音识别的函数
    fun startVoiceInput() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            errorMessage = "设备不支持语音识别"
            return
        }
        
        errorMessage = null
        speechPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }
    
    // 当获得权限后，启动语音识别
    LaunchedEffect(isListening) {
        if (isListening) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN")
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "zh-CN")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "请说出您要添加的任务...")
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            speechRecognitionLauncher.launch(intent)
        }
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.8f)
                .padding(8.dp),
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 标题
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "AI助手",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI智能添加",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "关闭"
                        )
                    }
                }
                
                // API密钥检查
                if (apiKey.isNullOrBlank()) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = "⚠️ 请先在设置中配置DeepSeek API密钥",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                } else {
                    // 用户输入区域
                    Text(
                        text = "描述你要添加的任务：",
                        style = MaterialTheme.typography.labelLarge
                    )
                    
                    // 输入框和语音按钮
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        OutlinedTextField(
                            value = userInput,
                            onValueChange = { userInput = it },
                            placeholder = { 
                                Text(
                                    text = "例如：今天晚上17:38去楼下超市买东西",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            },
                            modifier = Modifier.weight(1f),
                            minLines = 3,
                            maxLines = 5,
                            enabled = !isLoading && !isListening
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // 语音输入按钮
                        FloatingActionButton(
                            onClick = { startVoiceInput() },
                            modifier = Modifier.size(48.dp),
                            containerColor = if (isListening) 
                                MaterialTheme.colorScheme.error 
                            else 
                                MaterialTheme.colorScheme.primary,
                            elevation = FloatingActionButtonDefaults.elevation(0.dp)
                        ) {
                            Text(
                                text = if (isListening) "⏹" else "🎤",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                    
                    // 示例提示
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "💡 示例：明天上午10点开会 • 紧急：处理投诉 • 完成报告，包括数据分析、写总结、制作PPT",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "🎤 点击麦克风按钮进行中文语音输入",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                            )
                        }
                    }
                    
                    // 语音识别状态提示
                    if (isListening) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "🎤 正在听取语音输入，请说话...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                    
                    // 解析按钮
                    Button(
                        onClick = {
                            if (userInput.isNotBlank()) {
                                scope.launch {
                                    isLoading = true
                                    errorMessage = null
                                    parsedTask = null
                                    
                                    try {
                                        val parser = AITaskParser(apiKey)
                                        val result = parser.parseTask(userInput)
                                        
                                        if (result.isSuccess) {
                                            parsedTask = result.getOrNull()
                                        } else {
                                            errorMessage = result.exceptionOrNull()?.message ?: "解析失败"
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = "解析出错：${e.message}"
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = userInput.isNotBlank() && !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AI解析中...")
                        } else {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AI解析")
                        }
                    }
                    
                    // 错误信息
                    errorMessage?.let { error ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = "❌ $error",
                                modifier = Modifier.padding(12.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    
                    // 解析结果预览
                    parsedTask?.let { task ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "✨ AI解析结果",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                // 任务信息
                                TaskInfoRow("标题", task.title)
                                if (task.description.isNotBlank()) {
                                    TaskInfoRow("描述", task.description)
                                }
                                TaskInfoRow("优先级", task.priority.displayName)
                                if (task.dueDate != null) {
                                    val dateFormat = java.text.SimpleDateFormat("MM-dd HH:mm", java.util.Locale.getDefault())
                                    TaskInfoRow("截止时间", dateFormat.format(java.util.Date(task.dueDate)))
                                }
                                
                                // 显示子任务
                                if (task.subTasks.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "📋 子任务：",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    task.subTasks.forEach { subTask ->
                                        Text(
                                            text = "  • ${subTask.title}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                            modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                                        )
                                    }
                                }
                                
                                if (task.reasoning.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "💭 AI分析：${task.reasoning}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                        
                        // 确认按钮
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { 
                                    parsedTask = null
                                    errorMessage = null
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("重新解析")
                            }
                            
                            Button(
                                onClick = { onConfirm(task) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("确认添加")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label：",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}