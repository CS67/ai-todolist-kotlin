package com.example.todolist.ai

import com.example.todolist.data.Priority
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
    suspend fun parseTask(input: String): Result<ParsedTask> = withContext(Dispatchers.IO) {
        try {
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
        val currentTime = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val currentTimeStr = dateFormat.format(Date(currentTime))
        
        return """
你是一个任务解析助手，需要将用户的自然语言输入解析为结构化的任务数据。

当前时间：$currentTimeStr

用户输入：$input

请分析这段话并提取出以下信息，以JSON格式返回：
{
  "title": "任务标题（简洁明了，不超过20字）",
  "description": "任务描述（可选，补充详细信息）",
  "priority": "优先级（LOW/MEDIUM/HIGH/URGENT之一）",
  "dueDate": "截止时间（格式：YYYY-MM-DD HH:mm，如没有明确时间则为null）",
  "reasoning": "分析推理过程"
}

优先级判断规则：
- URGENT：紧急，立即需要处理的事情
- HIGH：重要且有明确时间要求的事情
- MEDIUM：一般重要性的日常任务
- LOW：可以延后处理的事情

时间解析规则：
- "今天"指当前日期
- "明天"指当前日期+1天
- "下周"指下周同一天
- 具体时间如"17:38"需要结合日期
- 如果只提到时间没提日期，默认为今天
- 请使用YYYY-MM-DD HH:mm格式返回时间，例如：2025-11-03 17:38

示例：
输入："今天晚上17:38去楼下超市买东西"
输出：
{
  "title": "去楼下超市买东西",
  "description": "购买生活用品",
  "priority": "MEDIUM",
  "dueDate": "2025-11-03 17:38",
  "reasoning": "这是一个日常购物任务，有明确的时间要求但不紧急，设为中等优先级"
}

请只返回JSON，不要包含其他文字。
        """.trimIndent()
    }
    
    /**
     * 调用DeepSeek API
     */
    private suspend fun callDeepSeekAPI(prompt: String): String {
        val requestBody = mapOf(
            "model" to MODEL,
            "messages" to listOf(
                mapOf(
                    "role" to "user",
                    "content" to prompt
                )
            ),
            "temperature" to 0.1,
            "max_tokens" to 500
        )
        
        val json = gson.toJson(requestBody)
        val body = json.toRequestBody("application/json".toMediaType())
        
        val request = Request.Builder()
            .url(API_URL)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(body)
            .build()
        
        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("API调用失败: ${response.code} ${response.message}")
                }
                
                val responseBody = response.body?.string() 
                    ?: throw IOException("响应体为空")
                
                // 解析API响应
                val apiResponse = gson.fromJson(responseBody, Map::class.java)
                val choices = apiResponse["choices"] as? List<*>
                    ?: throw IOException("API响应格式错误")
                
                val firstChoice = choices.firstOrNull() as? Map<*, *>
                    ?: throw IOException("没有找到响应选择")
                
                val message = firstChoice["message"] as? Map<*, *>
                    ?: throw IOException("没有找到消息内容")
                
                message["content"] as? String
                    ?: throw IOException("没有找到文本内容")
            }
        }
    }
    
    /**
     * 解析AI返回的响应
     */
    private fun parseResponse(response: String): ParsedTask {
        try {
            // 提取JSON部分（去除可能的markdown标记）
            val jsonStart = response.indexOf("{")
            val jsonEnd = response.lastIndexOf("}") + 1
            
            if (jsonStart == -1 || jsonEnd <= jsonStart) {
                throw JsonSyntaxException("响应中没有找到有效的JSON")
            }
            
            val jsonStr = response.substring(jsonStart, jsonEnd)
            val parsed = gson.fromJson(jsonStr, Map::class.java)
            
            return ParsedTask(
                title = parsed["title"] as? String ?: "新任务",
                description = parsed["description"] as? String ?: "",
                priority = parsePriority(parsed["priority"] as? String),
                dueDate = parseDueDate(parsed["dueDate"]),
                reasoning = parsed["reasoning"] as? String ?: ""
            )
        } catch (e: Exception) {
            throw IllegalArgumentException("解析AI响应失败: ${e.message}", e)
        }
    }
    
    /**
     * 解析优先级
     */
    private fun parsePriority(priorityStr: String?): Priority {
        return try {
            Priority.valueOf(priorityStr?.uppercase() ?: "MEDIUM")
        } catch (e: IllegalArgumentException) {
            Priority.MEDIUM
        }
    }
    
    /**
     * 解析截止时间
     */
    private fun parseDueDate(dueDateObj: Any?): Long? {
        try {
            when (dueDateObj) {
                is String -> {
                    val trimmed = dueDateObj.trim().removeSurrounding("\"")
                    if (trimmed.isBlank() || trimmed.equals("null", ignoreCase = true)) {
                        return null
                    }
                    
                    // 优先尝试标准格式：YYYY-MM-DD HH:mm
                    try {
                        val standardFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                        standardFormat.timeZone = TimeZone.getDefault()
                        return standardFormat.parse(trimmed)?.time
                    } catch (e: Exception) {
                        // 如果标准格式解析失败，尝试其他格式
                    }
                    
                    // 尝试其他常见格式
                    parseDateString(trimmed)?.time?.let { return it }
                    
                    // 如果都失败了，尝试解析为数字时间戳（兼容旧格式）
                    trimmed.toLongOrNull()?.let {
                        var v = it
                        if (v < 1_000_000_000_000L) v *= 1000L
                        return v
                    }
                    
                    return null
                }
                is Number -> {
                    // 兼容数字时间戳
                    var v = dueDateObj.toLong()
                    if (v < 1_000_000_000_000L) v *= 1000L
                    return v
                }
                null -> return null
                else -> return null
            }
        } catch (e: Exception) {
            return null
        }
    }

    /**
     * 尝试解析多种常见日期字符串（备用格式，主要用于兼容）
     */
    private fun parseDateString(s: String): Date? {
        val locale = Locale.getDefault()
        val now = Calendar.getInstance()

        // 备用格式列表（如果标准格式YYYY-MM-DD HH:mm失败时使用）
        val patterns = listOf(
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ssXXX", 
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "MM-dd HH:mm",
            "MM/dd HH:mm", 
            "HH:mm",
            "yyyy-MM-dd"
        )

        for (pat in patterns) {
            try {
                val fmt = SimpleDateFormat(pat, locale)
                fmt.timeZone = TimeZone.getDefault()
                val parsed = fmt.parse(s)
                if (parsed != null) {
                    // 如果模式不包含年份，补上当前年
                    if (!pat.contains("yyyy")) {
                        val cal = Calendar.getInstance()
                        cal.time = parsed
                        cal.set(Calendar.YEAR, now.get(Calendar.YEAR))
                        return cal.time
                    }
                    return parsed
                }
            } catch (_: Exception) {
                // 继续尝试下一个格式
            }
        }

        // 最后尝试：正则提取 MM-dd HH:mm 格式
        val regex = Regex("(\\d{1,2})[-/.](\\d{1,2})\\s+(\\d{1,2}):(\\d{2})")
        regex.find(s)?.let { match ->
            try {
                val month = match.groupValues[1].toInt()
                val day = match.groupValues[2].toInt()
                val hour = match.groupValues[3].toInt()
                val minute = match.groupValues[4].toInt()
                
                val cal = Calendar.getInstance()
                cal.set(Calendar.YEAR, now.get(Calendar.YEAR))
                cal.set(Calendar.MONTH, month - 1)
                cal.set(Calendar.DAY_OF_MONTH, day)
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                return cal.time
            } catch (_: Exception) {
                // 忽略错误
            }
        }

        return null
    }
    
    /**
     * 转换为Todo对象
     */
    fun ParsedTask.toTodo(): Todo {
        return Todo(
            title = this.title,
            description = this.description,
            priority = this.priority,
            dueDate = this.dueDate
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
    val reasoning: String
)