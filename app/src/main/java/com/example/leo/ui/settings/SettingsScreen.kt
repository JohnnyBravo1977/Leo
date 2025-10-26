package com.example.leo.ui.settings

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isDark: Boolean,
    onToggleDark: (Boolean) -> Unit,
    items: List<SettingsItem> = defaultSettingsItems(isDark),
    onTogglePythonSkill: (Boolean) -> Unit = {}
) {
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { backDispatcher?.onBackPressed() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(items) { item ->
                when (item) {
                    is SettingsItem.DarkMode -> SettingSwitchTile(
                        title = "Dark mode",
                        checked = item.enabled,
                        onChange = onToggleDark
                    )
                    is SettingsItem.PythonSkill -> SettingSwitchTile(
                        title = "Python skill (local sandbox)",
                        checked = item.enabled,
                        onChange = onTogglePythonSkill
                    )
                    is SettingsItem.Header -> Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
                HorizontalDivider(thickness = 0.6.dp, color = DividerDefaults.color)
            }
        }
    }
}

@Composable
private fun SettingSwitchTile(
    title: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

sealed interface SettingsItem {
    data class DarkMode(val enabled: Boolean) : SettingsItem
    data class PythonSkill(val enabled: Boolean) : SettingsItem
    data class Header(val title: String) : SettingsItem
}

fun defaultSettingsItems(isDark: Boolean): List<SettingsItem> = listOf(
    SettingsItem.Header("Appearance"),
    SettingsItem.DarkMode(enabled = isDark),
    SettingsItem.Header("AI Skills"),
    SettingsItem.PythonSkill(enabled = true)
)
