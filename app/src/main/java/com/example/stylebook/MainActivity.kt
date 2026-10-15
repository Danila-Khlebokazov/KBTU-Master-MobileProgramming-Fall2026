package com.example.stylebook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.stylebook.ui.StyleBookApp
import com.example.stylebook.ui.theme.StyleBookTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StyleBookTheme {
                StyleBookApp()
            }
        }
    }
}
