package com.example.leo.ui.chat

// -----------------------------
// Compose Foundation
// -----------------------------
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

// -----------------------------
// Compose Material 3
// -----------------------------
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult

// -----------------------------
// Compose Runtime / UI
// -----------------------------
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

// -----------------------------
// Material Icons (extended)
// -----------------------------
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings

// -----------------------------
// Time & formatting (API 26+)
// -----------------------------
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// -----------------------------
// Coroutines
// -----------------------------
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// -----------------------------
// Data / Store
// -----------------------------
import com.example.leo.data.ChatStore
import com.example.leo.data.ChatRecord
import com.example.leo.data.SyncStatus

// -----------------------------
// Brain
// -----------------------------
import com.example.leo.ai.BrainsEngine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    onOpenSettings: (() -> Unit)? = null
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    // Load history once
    val messages = remember { mutableStateListOf<ChatRecord>() }
    LaunchedEffect(Unit) {
        messages.clear()
        messages += ChatStore.read(ctx)
        if (messages.isEmpty()) {
            val hello = ChatRecord(
                id = System.currentTimeMillis(),
                isUser = false,
                text = "Hello, I'm Leo!",
                ts = System.currentTimeMillis(),
                status = SyncStatus.Sent
            )
            messages += hello
            ChatStore.append(ctx, hello)
        }
    }

    var input by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier,
        topBar = {
            // ✅ Settings on LEFT, Trash on RIGHT, title centered
            CenterAlignedTopAppBar(
                title = { Text("Little Genius") },
                colors = TopAppBarDefaults.topAppBarColors(),
                navigationIcon = {
                    IconButton(onClick = { onOpenSettings?.invoke() }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                ChatStore.clear(ctx)
                                messages.clear()
                                snackbar.showSnackbar("Chat cleared")
                            }
                        }
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = "Clear chat")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbar) },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message…") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors()
                )
                Spacer(Modifier.size(8.dp))
                Button(
                    onClick = {
                        val msg = input.trim()
                        if (msg.isNotEmpty()) {
                            input = ""
                            sendUserMessageWithRetry(
                                scope = scope,
                                messages = messages,
                                store = ChatStore,
                                ctx = ctx,
                                text = msg
                            )
                        }
                    }
                ) { Text("Send") }
            }
        }
    ) { inner ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(
                items = messages,
                key = { _, item -> item.id } // stable key for swipe state
            ) { index, item ->
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { value ->
                        if (
                            value == SwipeToDismissBoxValue.EndToStart ||
                            value == SwipeToDismissBoxValue.StartToEnd
                        ) {
                            val deleted = item
                            if (messages.isNotEmpty()) {
                                messages.removeAt(index.coerceIn(0, messages.lastIndex))
                            }
                            scope.launch { ChatStore.delete(ctx, deleted.id) }
                            scope.launch {
                                val res = snackbar.showSnackbar(
                                    message = "Message deleted",
                                    actionLabel = "UNDO",
                                    withDismissAction = true,
                                    duration = SnackbarDuration.Short
                                )
                                if (res == SnackbarResult.ActionPerformed) {
                                    val insertAt = messages.indexOfFirst { it.ts > deleted.ts }
                                        .takeIf { it >= 0 } ?: messages.size
                                    messages.add(insertAt, deleted)
                                    ChatStore.append(ctx, deleted)
                                }
                            }
                            true
                        } else false
                    }
                )
                SwipeToDismissBox(
                    state = dismissState,
                    backgroundContent = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .background(Color.Transparent)
                        )
                    },
                    enableDismissFromStartToEnd = true,
                    enableDismissFromEndToStart = true
                ) {
                    MessageRow(
                        message = item,
                        onRetry = {
                            scope.launch {
                                val idx = messages.indexOfFirst { it.id == item.id }
                                if (idx >= 0) {
                                    messages[idx] =
                                        messages[idx].copy(status = SyncStatus.Pending)
                                }
                                ChatStore.updateStatus(ctx, item.id, SyncStatus.Pending)
                                actuallySendWithPossibleFailure(
                                    scope = scope,
                                    messages = messages,
                                    store = ChatStore,
                                    ctx = ctx,
                                    userId = item.id,
                                    userText = item.text
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

// ---------------------------------------
// Sending / retry logic (now real, no echo)
// ---------------------------------------
private fun sendUserMessageWithRetry(
    scope: kotlinx.coroutines.CoroutineScope,
    messages: MutableList<ChatRecord>,
    store: ChatStore,
    ctx: android.content.Context,
    text: String
) {
    val id = System.currentTimeMillis()
    val pending = ChatRecord(
        id = id,
        isUser = true,
        text = text,
        ts = System.currentTimeMillis(),
        status = SyncStatus.Pending
    )
    messages += pending
    scope.launch { store.append(ctx, pending) }
    actuallySendWithPossibleFailure(scope, messages, store, ctx, id, text)
}

private fun actuallySendWithPossibleFailure(
    scope: kotlinx.coroutines.CoroutineScope,
    messages: MutableList<ChatRecord>,
    store: ChatStore,
    ctx: android.content.Context,
    userId: Long,
    userText: String
) {
    scope.launch {
        // brief typing delay for feel
        delay(300)

        // Mark user message as sent
        store.updateStatus(ctx, userId, SyncStatus.Sent)
        val idx = messages.indexOfFirst { it.id == userId }
        if (idx >= 0) messages[idx] = messages[idx].copy(status = SyncStatus.Sent)

        // Build history -> ask BrainsEngine
        val history: List<Pair<Boolean, String>> = messages.map { it.isUser to it.text }
        val engineReply = runCatching {
            BrainsEngine.reply(history, userText)
        }.getOrElse { e ->
            "I hit a snag: ${e.message ?: "unknown error"}"
        }

        // Append bot reply
        val reply = ChatRecord(
            id = System.currentTimeMillis(),
            isUser = false,
            text = engineReply,
            ts = System.currentTimeMillis(),
            status = SyncStatus.Sent
        )
        messages += reply
        store.append(ctx, reply)
    }
}

// -----------------------------
// UI bits
// -----------------------------
@Composable
private fun MessageRow(
    message: ChatRecord,
    onRetry: () -> Unit
) {
    val isUser = message.isUser
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.85f))
                    .align(Alignment.Bottom)
            )
            Spacer(Modifier.size(8.dp))
        }

        val containerColor =
            if (isUser) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        val contentColor =
            if (isUser) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurfaceVariant

        Card(
            colors = CardDefaults.cardColors(
                containerColor = containerColor,
                contentColor = contentColor
            ),
            shape = RoundedCornerShape(
                topStart = if (isUser) 16.dp else 4.dp,
                topEnd = if (isUser) 4.dp else 16.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(
                    text = message.text,
                    textAlign = if (isUser) TextAlign.End else TextAlign.Start,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.size(2.dp))

                val meta = buildString {
                    append(formatTs(message.ts))
                    if (message.isUser && message.status != SyncStatus.Sent) {
                        append(" • ")
                        append(
                            when (message.status) {
                                SyncStatus.Pending -> "sending…"
                                SyncStatus.Failed -> "failed"
                                else -> ""
                            }
                        )
                    }
                }
                if (meta.isNotBlank()) {
                    Text(
                        text = meta,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                    )
                }

                if (message.isUser && message.status == SyncStatus.Failed) {
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onRetry) { Text("Retry") }
                    }
                }
            }
        }

        if (isUser) {
            Spacer(Modifier.size(8.dp))
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.9f))
                    .align(Alignment.Bottom)
            )
        }
    }
}

private val timeFmt = DateTimeFormatter.ofPattern("h:mm a")
private fun formatTs(epochMs: Long): String =
    Instant.ofEpochMilli(epochMs)
        .atZone(ZoneId.systemDefault())
        .format(timeFmt)
