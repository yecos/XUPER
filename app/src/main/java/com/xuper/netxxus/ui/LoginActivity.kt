package com.xuper.netxxus.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.sp
import com.xuper.netxxus.R
import com.xuper.netxxus.ui.theme.XuperHydraTheme
import com.xuper.netxxus.ui.theme.XuperRed
import androidx.compose.ui.unit.dp

/**
 * Pantalla de login.
 * Reemplaza a los diálogos `dialog_login.xml`, `dialog_phone_login.xml`, `dialog_sms_login.xml`
 * de la APK original con una pantalla Compose unificada.
 */
class LoginActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            XuperHydraTheme {
                LoginScreen()
            }
        }
    }
}

@Composable
private fun LoginScreen() {
    val account = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(64.dp)
    ) {
        Text(
            text = stringResource(R.string.login_title),
            color = Color.White,
            fontSize = 40.sp
        )
        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = account.value,
            onValueChange = { account.value = it },
            label = { Text(stringResource(R.string.login_account_hint)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors().copy(
                focusedTextColor = Color.White,
                cursorColor = XuperRed
            )
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = password.value,
            onValueChange = { password.value = it },
            label = { Text(stringResource(R.string.login_password_hint)) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { /* TODO: llamar a la API de autenticación */ },
            colors = ButtonDefaults.buttonColors(containerColor = XuperRed),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.login_button), color = Color.White)
        }
    }
}
