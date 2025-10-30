package com.example.leo.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.leo.data.FocusPrefs
import kotlinx.coroutines.launch

/**
 * A self-contained settings row that exposes the "Laser Focus" switch.
 * This does not change any existing composable signatures; it can be
 * dropped into SettingsScreen later like:
 *
 *    LaserFocusRow(Modifier.fillMaxWidth())
 *
 * until then it is harmless and unused.
 */
@Composable
fun LaserFocusRow(
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current
    val prefs = remember { FocusPrefs(ctx) }
    val scope = rememberCoroutineScope()

    val isLaser by prefs.laserFlow.collectAsState(initial = false)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Laser Focus",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Tighten autopilot: reduce drift, prefer exact file paths, and enforce patch protocol.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = isLaser,
            onCheckedChange = { enabled ->
                scope.launch { prefs.setLaser(enabled) }
            }
        )
    }

    Divider(modifier = Modifier.padding(start = 16.dp))
}
- 
