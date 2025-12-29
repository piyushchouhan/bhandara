package com.example.bhandara

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.bhandara.ui.components.LanguageSwitcher
import com.example.bhandara.ui.screens.HomeScreen
import com.example.bhandara.ui.theme.BhandaraTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BhandaraTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = { LanguageSwitcher() }
                ) { innerPadding ->
                    HomeScreen(
                        modifier = Modifier.padding(innerPadding),
                        onHungryClick = { /* TODO: Navigate to hungry screen */ },
                        onReportFeastClick = { /* TODO: Navigate to report feast screen */ }
                    )
                }
            }
        }
    }
}
