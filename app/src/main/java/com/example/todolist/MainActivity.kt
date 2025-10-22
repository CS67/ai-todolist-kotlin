package com.example.todolist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.todolist.ui.screens.TodoListScreen
import com.example.todolist.ui.theme.TodoListTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TodoListTheme {
                TodoListScreen()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TodoAppPreview() {
    TodoListTheme {
        TodoListScreen()
    }
}
