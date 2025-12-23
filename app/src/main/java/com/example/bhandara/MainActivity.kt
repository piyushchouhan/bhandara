package com.example.bhandara

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import com.example.bhandara.ui.theme.BhandaraTheme
import java.util.Locale

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
                    MainScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSwitcher() {
    var currentLanguage by remember { mutableStateOf(Locale.getDefault().language) }
    
    TopAppBar(
        title = { },
        navigationIcon = {
            IconButton(onClick = {
                // Toggle between English and Hindi
                val newLocale = if (currentLanguage == "hi") "en" else "hi"
                currentLanguage = newLocale
                
                // Set app locale
                androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(
                    LocaleListCompat.forLanguageTags(newLocale)
                )
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.translate_indic_language),
                    contentDescription = "Switch Language",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    )
}

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Logo positioned independently at top center
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Bhandara Logo",
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.TopCenter)
                .padding(top = 80.dp)
        )
        
        // Buttons centered vertically
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp, end = 24.dp, top = 80.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Button 1: I am Hungry
            Button(
                onClick = { /* TODO: Add action */ },
                modifier = Modifier
                    .fillMaxWidth() // Width of the screen
                    .height(80.dp), // Extra Large height
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = stringResource(R.string.button_i_am_hungry),
                    fontSize = 24.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp)) // Space between buttons

            // Button 2: Report a Feast
            Button(
                onClick = { /* TODO: Add action */ },
                modifier = Modifier
                    .fillMaxWidth() // Width of the screen
                    .height(60.dp), // Extra Large height
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Text(
                    text = stringResource(R.string.button_report_feast),
                    fontSize = 24.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    BhandaraTheme {
        MainScreen()
    }
}
