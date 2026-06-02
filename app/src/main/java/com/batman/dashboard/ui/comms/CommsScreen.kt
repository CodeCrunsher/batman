package com.batman.dashboard.ui.comms

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.batman.dashboard.data.db.MessageEntity
import com.batman.dashboard.ui.components.*
import com.batman.dashboard.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommsScreen(
    viewModel: CommsViewModel,
    onAllyClick: (String, String) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        containerColor = BatBlack,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("SECURE COMMS", style = MaterialTheme.typography.headlineLarge)
                        Text("ENCRYPTED CHANNEL", style = MaterialTheme.typography.headlineSmall)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BatGold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BatBlack)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                // Encryption status banner
                GlassCard(modifier = Modifier.fillMaxWidth(), borderColor = BatGreen.copy(alpha = 0.4f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PulseIndicator(BatGreen, 10.dp)
                        Text("AES-256 ENCRYPTED  •  QUANTUM-SECURED", style = MaterialTheme.typography.labelMedium, color = BatGreen)
                    }
                }
            }
            items(ALLIES) { ally ->
                AllyCard(ally = ally, onClick = { onAllyClick(ally.id, ally.name) })
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun AllyCard(ally: Ally, onClick: () -> Unit) {
    val iconColors = mapOf(
        "alfred" to BatGold, "robin" to BatRed, "nightwing" to BatCyan,
        "oracle" to BatPurple, "gordon" to BatOrange, "lucius" to BatGreen
    )
    val color = iconColors[ally.id] ?: BatGold
    GlassCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        borderColor = color.copy(alpha = 0.3f)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(color.copy(alpha = 0.15f), CircleShape)
                    .border(2.dp, color.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    ally.name.first().toString(),
                    style = MaterialTheme.typography.displaySmall,
                    color = color
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(ally.name, style = MaterialTheme.typography.titleLarge)
                Text(ally.role, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            Column(horizontalAlignment = Alignment.End) {
                PulseIndicator(if (ally.isOnline) BatGreen else TextDisabled, 8.dp)
                Spacer(Modifier.height(2.dp))
                Text(if (ally.isOnline) "ONLINE" else "OFFLINE", style = MaterialTheme.typography.labelSmall, color = if (ally.isOnline) BatGreen else TextDisabled)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    allyId: String,
    allyName: String,
    viewModel: CommsViewModel,
    onBack: () -> Unit
) {
    val messages by viewModel.loadMessages(allyId).collectAsState(initial = emptyList())
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    Scaffold(
        containerColor = BatBlack,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val iconColors = mapOf("alfred" to BatGold, "robin" to BatRed, "nightwing" to BatCyan, "oracle" to BatPurple, "gordon" to BatOrange, "lucius" to BatGreen)
                        val color = iconColors[allyId] ?: BatGold
                        Box(Modifier.size(36.dp).background(color.copy(0.2f), CircleShape).border(1.dp, color.copy(0.5f), CircleShape), contentAlignment = Alignment.Center) {
                            Text(allyName.first().toString(), style = MaterialTheme.typography.titleLarge, color = color)
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(allyName, style = MaterialTheme.typography.titleLarge)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = BatGreen, modifier = Modifier.size(10.dp))
                                Text("SECURE CHANNEL", style = MaterialTheme.typography.labelSmall, color = BatGreen)
                            }
                        }
                    }
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = BatGold) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BatBlack)
            )
        },
        bottomBar = {
            Surface(color = BatSurface, tonalElevation = 8.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp).navigationBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Encrypted message...", color = TextDisabled) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BatGold, unfocusedBorderColor = BatBorder, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = { viewModel.sendMessage(allyId, inputText); inputText = "" },
                        modifier = Modifier.size(48.dp).background(BatGold, CircleShape)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = BatBlack)
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // ── Uplink error banner ────────────────────────────────────────
            uiState.uplinkError?.let { error ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BatOrange.copy(alpha = 0.15f))
                        .border(1.dp, BatOrange.copy(alpha = 0.4f))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null,
                        tint = BatOrange, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(error, style = MaterialTheme.typography.labelSmall,
                        color = BatOrange, modifier = Modifier.weight(1f))
                    IconButton(onClick = { viewModel.clearUplinkError() },
                        modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Dismiss",
                            tint = BatOrange, modifier = Modifier.size(14.dp))
                    }
                }
            }
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { Spacer(Modifier.height(8.dp)) }
                items(messages, key = { it.id }) { msg ->
                    ChatBubble(message = msg)
                }
                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }
}

@Composable
fun ChatBubble(message: MessageEntity) {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isFromBatman) Arrangement.End else Arrangement.Start
    ) {
        Column(
            horizontalAlignment = if (message.isFromBatman) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        if (message.isFromBatman) BatGold.copy(alpha = 0.15f) else BatSurfaceVar,
                        RoundedCornerShape(
                            topStart = 16.dp, topEnd = 16.dp,
                            bottomStart = if (message.isFromBatman) 16.dp else 4.dp,
                            bottomEnd = if (message.isFromBatman) 4.dp else 16.dp
                        )
                    )
                    .border(
                        1.dp,
                        if (message.isFromBatman) BatGold.copy(0.3f) else BatBorder,
                        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = if (message.isFromBatman) 16.dp else 4.dp, bottomEnd = if (message.isFromBatman) 4.dp else 16.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Column {
                    if (!message.isFromBatman) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = BatGreen, modifier = Modifier.size(9.dp))
                            Text("ENCRYPTED", style = MaterialTheme.typography.labelSmall, color = BatGreen)
                        }
                        Spacer(Modifier.height(2.dp))
                    }
                    Text(message.content, style = MaterialTheme.typography.bodyMedium, color = if (message.isFromBatman) TextPrimary else TextSecondary)
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(sdf.format(Date(message.timestamp)), style = MaterialTheme.typography.labelSmall, color = TextDisabled)
        }
    }
}
