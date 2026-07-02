package com.xuper.netxxus.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.xuper.netxxus.R
import com.xuper.netxxus.ui.theme.XuperHydraTheme
import com.xuper.netxxus.ui.theme.XuperRed

/**
 * Pantalla "Mi cuenta".
 * Reemplaza a `com.mine.ui.activity.UserCenterActivity`.
 */
class MineActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            XuperHydraTheme {
                MineScreen()
            }
        }
    }
}

@Composable
private fun MineScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp)
    ) {
        Text(stringResource(R.string.nav_mine), color = Color.White, fontSize = 32.sp)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            colors = CardDefaults.cardColors(containerColor = XuperRed.copy(alpha = 0.2f))
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(stringResource(R.string.mine_membership), color = Color.White, fontSize = 22.sp)
                Text(stringResource(R.string.mine_expire, "31 dic 2026"),
                     color = MaterialTheme.colorScheme.onSurfaceVariant,
                     modifier = Modifier.padding(top = 4.dp))
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(containerColor = XuperRed),
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Text(stringResource(R.string.mine_renew), color = Color.White)
                }
            }
        }

        ListItem(stringResource(R.string.mine_history))
        ListItem(stringResource(R.string.mine_invite))
        ListItem(stringResource(R.string.nav_settings))
        ListItem(stringResource(R.string.settings_about))
        ListItem(stringResource(R.string.mine_logout))
    }
}

@Composable
private fun ListItem(label: String) {
    TextButton(onClick = {}) {
        Text(label, color = Color.White, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
    }
}
