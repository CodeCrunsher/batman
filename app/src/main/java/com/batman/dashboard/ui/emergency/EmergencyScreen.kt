package com.batman.dashboard.ui.emergency

import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.batman.dashboard.data.db.EmergencyLogEntity
import com.batman.dashboard.ui.components.*
import com.batman.dashboard.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

val ALERT_TYPES = listOf(
    "MEDICAL EMERGENCY",
    "POLICE BACKUP",
    "FIRE RESPONSE",
    "TACTICAL SUPPORT",
    "AIR SUPPORT",
    "EVACUATION"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyScreen(
    viewModel: EmergencyViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val infiniteTransition = rememberInfiniteTransition(label = "sos")

    val sosPulse by infiniteTransition.animateFloat(
        initialValue = 0.95f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )
    val sosGlow by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glow"
    )

    Scaffold(
        containerColor = BatBlack,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("EMERGENCY", style = MaterialTheme.typography.headlineLarge, color = BatRed)
                        Text("SOS & RAPID RESPONSE", style = MaterialTheme.typography.headlineSmall)
                    }
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = BatGold) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BatBlack)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Success notification
            if (state.showSuccess) {
                item {
                    GlassCard(Modifier.fillMaxWidth(), borderColor = BatGreen.copy(0.6f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PulseIndicator(BatGreen, 12.dp)
                            Column {
                                Text("ALERT SENT", style = MaterialTheme.typography.headlineMedium, color = BatGreen)
                                Text("All active contacts have been notified", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                        }
                    }
                }
            }

            // Alert type selection
            item {
                GlassCard(Modifier.fillMaxWidth()) {
                    Text("ALERT TYPE", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(8.dp))
                    ALERT_TYPES.chunked(2).forEach { row ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { type ->
                                val isSelected = state.selectedAlertType == type
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.selectAlertType(type) },
                                    label = { Text(type, style = MaterialTheme.typography.labelSmall) },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = BatRed.copy(0.2f),
                                        selectedLabelColor = BatRed
                                    )
                                )
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                }
            }

            // SOS Button
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (state.isSosActive) {
                        // Countdown state
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(160.dp)
                                    .graphicsLayer { scaleX = sosPulse; scaleY = sosPulse }
                                    .drawBehind {
                                        drawCircle(BatRed.copy(alpha = 0.1f + sosGlow * 0.2f), radius = size.minDimension * 0.65f)
                                        drawCircle(BatRed.copy(alpha = 0.08f), radius = size.minDimension * 0.80f)
                                    }
                                    .background(BatRed.copy(alpha = 0.2f), CircleShape)
                                    .border(3.dp, BatRed, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        state.sosCountdown.toString(),
                                        style = MaterialTheme.typography.displayLarge,
                                        color = BatRed,
                                        fontSize = 56.sp
                                    )
                                    Text("SENDING...", style = MaterialTheme.typography.labelMedium, color = BatRed)
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = viewModel::cancelSOS,
                                border = BorderStroke(1.dp, BatRed),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = BatRed)
                            ) {
                                Icon(Icons.Default.Cancel, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("CANCEL SOS", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    } else {
                        // Ready state
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(160.dp)
                                    .clickable { viewModel.triggerSOS() }
                                    .drawBehind {
                                        drawCircle(BatRed.copy(alpha = 0.08f), radius = size.minDimension * 0.65f)
                                    }
                                    .background(BatRed, CircleShape)
                                    .border(3.dp, BatRed.copy(0.5f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Emergency, null, tint = Color.White, modifier = Modifier.size(48.dp))
                                    Text("S O S", style = MaterialTheme.typography.headlineLarge, color = Color.White, letterSpacing = 6.sp)
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Tap to send ${state.selectedAlertType}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                "5-second countdown before dispatch",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextDisabled,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Active contacts
            item { SectionHeader("EMERGENCY CONTACTS", Modifier.fillMaxWidth()) }
            if (state.contacts.isEmpty()) {
                item { Text("No contacts configured", style = MaterialTheme.typography.bodyMedium, color = TextDisabled) }
            } else {
                items(state.contacts, key = { it.id }) { contact ->
                    GlassCard(Modifier.fillMaxWidth(), borderColor = BatRed.copy(0.2f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(40.dp).background(BatRed.copy(0.15f), CircleShape).border(1.dp, BatRed.copy(0.3f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(contact.name.first().toString(), style = MaterialTheme.typography.titleLarge, color = BatRed)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(contact.name, style = MaterialTheme.typography.titleLarge)
                                Text(contact.role, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                Text(contact.phone, style = MaterialTheme.typography.bodySmall, color = TextDisabled)
                            }
                            Icon(Icons.Default.Phone, null, tint = BatGreen, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            // Recent alert log
            if (state.alertLog.isNotEmpty()) {
                item { SectionHeader("RECENT ALERTS", Modifier.fillMaxWidth()) }
                items(state.alertLog.take(5), key = { it.id }) { log ->
                    AlertLogItem(log)
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun AlertLogItem(log: EmergencyLogEntity) {
    val sdf = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault())
    GlassCard(Modifier.fillMaxWidth(), borderColor = BatRed.copy(0.15f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Warning, null, tint = BatRed, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(log.alertType, style = MaterialTheme.typography.labelMedium, color = BatRed)
                Text(sdf.format(Date(log.triggeredAt)), style = MaterialTheme.typography.bodySmall, color = TextDisabled)
            }
            StatusChip(if (log.resolvedAt != null) "RESOLVED" else "ACTIVE", if (log.resolvedAt != null) BatGreen else BatOrange)
        }
    }
}
