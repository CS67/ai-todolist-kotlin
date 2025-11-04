package com.example.tasks.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * AI设置管理器
 */
class AIPreferences(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "ai_preferences", 
        Context.MODE_PRIVATE
    )
    
    companion object {
        private const val KEY_API_KEY = "deepseek_api_key"
        private const val KEY_AI_ENABLED = "ai_enabled"
    }
    
    /**
     * 保存API密钥
     */
    fun saveApiKey(apiKey: String) {
        prefs.edit()
            .putString(KEY_API_KEY, apiKey)
            .putBoolean(KEY_AI_ENABLED, apiKey.isNotBlank())
            .apply()
    }
    
    /**
     * 获取API密钥
     */
    fun getApiKey(): String? {
        return prefs.getString(KEY_API_KEY, null)
    }
    
    /**
     * 检查AI是否已启用
     */
    fun isAIEnabled(): Boolean {
        return prefs.getBoolean(KEY_AI_ENABLED, false) && !getApiKey().isNullOrBlank()
    }
    
    /**
     * 清除API密钥
     */
    fun clearApiKey() {
        prefs.edit()
            .remove(KEY_API_KEY)
            .putBoolean(KEY_AI_ENABLED, false)
            .apply()
    }
}

/**
 * Compose中使用AI偏好设置的便捷方法
 */
@Composable
fun rememberAIPreferences(): AIPreferences {
    val context = LocalContext.current
    return remember { AIPreferences(context) }
}