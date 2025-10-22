package com.example.todolist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.todolist.database.TodoDatabase
import com.example.todolist.repository.TodoRepository
import com.example.todolist.ui.screens.TodoListScreen
import com.example.todolist.ui.theme.TodoListTheme
import com.example.todolist.viewmodel.TodoViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TodoListTheme {
                val database = TodoDatabase.getDatabase(this)
                val repository = TodoRepository(database.todoDao())
                val viewModelFactory = TodoViewModelFactory(repository)
                
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
