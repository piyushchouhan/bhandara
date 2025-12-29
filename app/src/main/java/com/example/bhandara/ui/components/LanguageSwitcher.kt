package com.example.bhandara.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.core.os.LocaleListCompat
import com.example.bhandara.R
import java.util.Locale

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
