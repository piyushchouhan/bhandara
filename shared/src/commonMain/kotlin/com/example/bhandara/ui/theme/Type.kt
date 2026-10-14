package com.example.bhandara.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import com.example.bhandara.shared.resources.Res
import com.example.bhandara.shared.resources.google_sans_bold
import com.example.bhandara.shared.resources.google_sans_medium
import com.example.bhandara.shared.resources.google_sans_regular
import org.jetbrains.compose.resources.Font

// Google Sans, loaded from shared resources (works on Android and iOS)
@Composable
fun googleSansFamily(): FontFamily = FontFamily(
    Font(Res.font.google_sans_regular, FontWeight.Normal),
    Font(Res.font.google_sans_medium, FontWeight.Medium),
    Font(Res.font.google_sans_bold, FontWeight.Bold)
)

// Set of Material typography styles to start with
@Composable
fun bhandaraTypography(): Typography {
    val googleSansFamily = googleSansFamily()
    return Typography(
        bodyLarge = TextStyle(
            fontFamily = googleSansFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp
        ),
        labelLarge = TextStyle(
            fontFamily = googleSansFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp
        ),
        titleLarge = TextStyle(
            fontFamily = googleSansFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            letterSpacing = 0.sp
        )
    )
}
