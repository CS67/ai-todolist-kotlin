package com.example.tasks

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.tasks.database.TodoDatabase
import com.example.tasks.repository.TodoRepository
import com.example.tasks.ui.screens.TodoListScreen
import com.example.tasks.ui.theme.TodoListTheme
import com.example.tasks.viewmodel.TodoViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TodoListTheme {
                val database = TodoDatabase.getDatabase(this)
                val repository = TodoRepository(database.todoDao())
                val viewModelFactory = TodoViewModel.Factory(repository)
                
                TodoListScreen(
                    viewModelFactory = viewModelFactory
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TodoAppPreview() {
    TodoListTheme {
        // 预览模式下不显示内容，因为需要数据库依赖
        Box(
            modifier = androidx.compose.ui.Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            androidx.compose.material3.Text("TodoList 预览模式")
        }
    }
}
