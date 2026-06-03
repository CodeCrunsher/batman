package com.batman.dashboard.ui.equipment

import androidx.compose.animation.*
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
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.batman.dashboard.data.db.EquipmentEntity
import com.batman.dashboard.ui.components.*
import com.batman.dashboard.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentScreen(
    viewModel: EquipmentViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = BatBlack,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("EQUIPMENT", style = MaterialTheme.typography.headlineLarge)
                        Text("ARMOR & GADGET CONTROL", style = MaterialTheme.typography.headlineSmall)
                    }
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = BatGold) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BatBlack)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                val activeCount = state.items.count { it.isEnabled }
                val critCount = state.items.count { it.batteryLevel < 20 }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    GlassCard(Modifier.weight(1f), borderColor = BatGreen.copy(0.3f)) {
                        Text("$activeCount", style = MaterialTheme.typography.displaySmall, color = BatGreen)
                        Text("ONLINE", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    GlassCard(Modifier.weight(1f), borderColor = BatRed.copy(0.3f)) {
                        Text("$critCount", style = MaterialTheme.typography.displaySmall, color = BatRed)
                        Text("CRITICAL", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    GlassCard(Modifier.weight(1f), borderColor = BatGold.copy(0.3f)) {
                        Text("${state.items.size}", style = MaterialTheme.typography.displaySmall, color = BatGold)
                        Text("TOTAL", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            }
            items(state.items, key = { it.id }) { item ->
                EquipmentCard(
                    item = item,
                    onToggle = { viewModel.toggleEquipment(item) },
                    onCharge = { viewModel.chargeBattery(item) }
                )
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun EquipmentCard(
    item: EquipmentEntity,
    onToggle: () -> Unit,
    onCharge: () -> Unit
) {
    val statusColor = when (item.status) {
        "ACTIVE"   -> BatGreen
        "STANDBY"  -> BatGold
        "CHARGING" -> BatCyan
        "OFFLINE"  -> BatRed
        else       -> TextSecondary
    }
    val icon = equipmentIcon(item.iconKey)

    // ── Power-up animation ────────────────────────────────────────────────────
    val scanProgress = remember { Animatable(0f) }
    val glowAlpha   = remember { Animatable(0f) }
    var prevEnabled by remember { mutableStateOf(item.isEnabled) }

    LaunchedEffect(item.isEnabled) {
        if (item.isEnabled && !prevEnabled) {
            // Sweep a scanline from left to right, then fade out
            glowAlpha.snapTo(1f)
            scanProgress.snapTo(0f)
            scanProgress.animateTo(1.4f, animationSpec = tween(900, easing = FastOutSlowInEasing))
            glowAlpha.animateTo(0f, animationSpec = tween(450))
        } else if (!item.isEnabled) {
            glowAlpha.snapTo(0f)
            scanProgress.snapTo(0f)
        }
        prevEnabled = item.isEnabled
    }

    val animBorderColor = if (glowAlpha.value > 0.05f)
        BatCyan.copy(alpha = glowAlpha.value * 0.9f)
    else
        statusColor.copy(alpha = if (item.isEnabled) 0.5f else 0.2f)

    // Wrap in a Box so we can overlay the Canvas scanline
    Box(modifier = Modifier.fillMaxWidth()) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = animBorderColor
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            if (item.isEnabled) statusColor.copy(0.15f) else BatBorder.copy(0.3f),
                            RoundedCornerShape(12.dp)
                        )
                        .border(1.dp, statusColor.copy(if (item.isEnabled) 0.4f else 0.1f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null,
                        tint = if (item.isEnabled) statusColor else TextDisabled,
                        modifier = Modifier.size(26.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(item.name, style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(4.dp))
                    StatusChip(item.status, statusColor)
                    Spacer(Modifier.height(6.dp))
                    BatteryBar(level = item.batteryLevel, label = "POWER", modifier = Modifier.fillMaxWidth(0.85f))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Switch(
                        checked = item.isEnabled,
                        onCheckedChange = { onToggle() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = BatBlack,
                            checkedTrackColor = BatGold,
                            uncheckedThumbColor = TextDisabled,
                            uncheckedTrackColor = BatBorder
                        )
                    )
                    if (item.batteryLevel < 30) {
                        Spacer(Modifier.height(4.dp))
                        TextButton(
                            onClick = onCharge,
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.BatteryChargingFull, null, tint = BatCyan, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(2.dp))
                            Text("CHARGE", style = MaterialTheme.typography.labelSmall, color = BatCyan)
                        }
                    }
                }
            }
        }

        // Scanline overlay — drawn on top of GlassCard
        if (glowAlpha.value > 0.01f) {
            Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                val x = scanProgress.value * size.width
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            BatCyan.copy(alpha = glowAlpha.value * 0.45f),
                            BatGold.copy(alpha = glowAlpha.value * 0.20f),
                            Color.Transparent
                        ),
                        startX = (x - 120f).coerceAtLeast(0f),
                        endX  = (x + 120f)
                    ),
                    size = size
                )
            }
        }
    }
}

fun equipmentIcon(key: String) = when (key) {
    "suit"     -> Icons.Default.Shield
    "car"      -> Icons.Default.DirectionsCar
    "plane"    -> Icons.Default.AirplanemodeActive
    "batarang" -> Icons.Default.Star
    "grapple"  -> Icons.Default.Link
    "goggles"  -> Icons.Default.Visibility
    "emp"      -> Icons.Default.FlashOn
    "comm"     -> Icons.Default.SettingsInputAntenna
    else       -> Icons.Default.Build
}
