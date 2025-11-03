package com.example.todolist.ai

import com.example.todolist.data.Priority
import com.example.todolist.data.SubTask
import com.example.todolist.data.Todo
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * AI任务解析器 - 使用DeepSeek API解析自然语言输入
 */
class AITaskParser(private val apiKey: String) {
    
    private val client = OkHttpClient()
    private val gson = Gson()
    
    companion object {
        private const val API_URL = "https://api.deepseek.com/v1/chat/completions"
        private const val MODEL = "deepseek-chat"
    }
    
    /**
     * 解析自然语言输入为Todo对象
     */
    suspend fun parseTask(input: String): Result<ParsedTask> {
        return try {
            val prompt = createPrompt(input)
            val response = callDeepSeekAPI(prompt)
            val parsedTask = parseResponse(response)
            Result.success(parsedTask)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 创建给AI的提示词
     */
    private fun createPrompt(input: String): String {
        val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        
        return """
你是一个任务解析助手，将自然语言转换为结构化任务数据。

当前时间：$currentTime
用户输入：$input

返回JSON格式：
{
  "title": "任务标题",
  "description": "任务描述",
  "priority": "LOW/MEDIUM/HIGH/URGENT",
  "dueDate": "YYYY-MM-DD HH:mm格式或null",
  "subTasks": ["子任务1", "子任务2"],
  "reasoning": "分析过程"
}

优先级规则：URGENT(紧急)、HIGH(重要有时限)、MEDIUM(日常)、LOW(可延后)
时间规则：今天=当前日期，明天=+1天，只有时间默认今天
子任务规则：识别"包括A、B、C"、"先做X再做Y"等表达

只返回JSON，无其他文字。
        """.trimIndent()
    }
    
    /**
     * 调用DeepSeek API
     */
    private suspend fun callDeepSeekAPI(prompt: String): String = withContext(Dispatchers.IO) {
        val requestBody = mapOf(
            "model" to MODEL,
            "messages" to listOf(mapOf("role" to "user", "content" to prompt)),
            "temperature" to 0.1,
            "max_tokens" to 500
        )
        
        val request = Request.Builder()
            .url(API_URL)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(gson.toJson(requestBody).toRequestBody("application/json".toMediaType()))
            .build()
        
        client.newCall(request).execute().use { response ->
            val body = response.body?.string() ?: throw IOException("响应体为空")
            val apiResponse = gson.fromJson(body, Map::class.java)
            val choices = apiResponse["choices"] as List<*>
            val message = (choices[0] as Map<*, *>)["message"] as Map<*, *>
            message["content"] as String
        }
    }
    
    /**
     * 解析AI返回的响应
     */
    private fun parseResponse(response: String): ParsedTask {
        val jsonStart = response.indexOf("{")
        val jsonEnd = response.lastIndexOf("}") + 1
        val jsonStr = response.substring(jsonStart, jsonEnd)
        val parsed = gson.fromJson(jsonStr, Map::class.java)
        
        return ParsedTask(
            title = parsed["title"] as? String ?: "新任务",
            description = parsed["description"] as? String ?: "",
            priority = parsePriority(parsed["priority"] as? String),
            dueDate = parseDueDate(parsed["dueDate"]),
            reasoning = parsed["reasoning"] as? String ?: "",
            subTasks = parseSubTasks(parsed["subTasks"])
        )
    }
    
    /**
     * 解析优先级
     */
    private fun parsePriority(priorityStr: String?): Priority {
        return try {
            Priority.valueOf(priorityStr?.uppercase() ?: "MEDIUM")
        } catch (e: Exception) {
            Priority.MEDIUM
        }
    }
    
    /**
     * 解析子任务
     */
    private fun parseSubTasks(subTasksObj: Any?): List<SubTask> {
        return when (subTasksObj) {
            is List<*> -> subTasksObj.mapNotNull { item ->
                (item as? String)?.takeIf { it.isNotBlank() }?.let { SubTask(title = it.trim()) }
            }
            else -> emptyList()
        }
    }
    
    /**
     * 解析截止时间
     */
    private fun parseDueDate(dueDateObj: Any?): Long? {
        return when (dueDateObj) {
            is String -> {
                val trimmed = dueDateObj.trim().removeSurrounding("\"")
                if (trimmed.isBlank() || trimmed.equals("null", true)) return null
                try {
                    SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).parse(trimmed)?.time
                } catch (e: Exception) {
                    null
                }
            }
            is Number -> dueDateObj.toLong().let { if (it < 1_000_000_000_000L) it * 1000L else it }
            else -> null
        }
    }

    /**
     * 转换为Todo对象
     */
    fun ParsedTask.toTodo(): Todo {
        return Todo(
            title = this.title,
            description = this.description,
            priority = this.priority,
            dueDate = this.dueDate,
            subTasks = this.subTasks
        )
    }
}

/**
 * 解析结果数据类
 */
data class ParsedTask(
    val title: String,
    val description: String,
    val priority: Priority,
    val dueDate: Long?,
    val reasoning: String,
    val subTasks: List<SubTask> = emptyList()
)