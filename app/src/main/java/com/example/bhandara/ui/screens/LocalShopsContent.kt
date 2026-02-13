package com.example.bhandara.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.size
import com.example.bhandara.R

import com.example.bhandara.ui.theme.FoodShopPrimary

@Composable
fun LocalShopsContent(
    onHungryClick: () -> Unit = {},
    onAddShopClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 24.dp, end = 24.dp, top = 80.dp, bottom = 100.dp), // Added bottom padding for toggle
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Button 1: I am Hungry
        Button(
            onClick = onHungryClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = FoodShopPrimary,
                contentColor = Color.White
            )
        ) {
            Text(
                text = stringResource(R.string.button_i_am_hungry),
                fontSize = 24.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Button 2: Add Food Shop
        OutlinedButton(
            onClick = onAddShopClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Text(
                text = stringResource(R.string.button_add_food_shop),
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                painter = painterResource(id = R.drawable.baseline_storefront_24),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
