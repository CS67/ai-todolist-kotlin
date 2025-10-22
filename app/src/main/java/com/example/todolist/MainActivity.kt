package com.example.todolist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // 这里就是你的UI
            TodoApp()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TodoApp() {
    var text by remember { mutableStateOf("") }
    var todos by remember { mutableStateOf(listOf<String>()) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("📝 待办清单", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("输入任务") }
        )

        Spacer(Modifier.height(8.dp))

        Button(onClick = {
            if (text.isNotBlank()) {
                todos = todos + text
                text = ""
            }
        }) {
            Text("添加任务")
        }

        Spacer(Modifier.height(16.dp))

        for (item in todos) {
            Text("• $item")
        }
    }
}
