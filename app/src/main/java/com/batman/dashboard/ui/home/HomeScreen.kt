package com.batman.dashboard.ui.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.batman.dashboard.*
import com.batman.dashboard.data.db.CrimePinDao
import com.batman.dashboard.data.db.MissionDao
import com.batman.dashboard.ui.components.*
import com.batman.dashboard.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    missionDao: MissionDao,
    crimePinDao: CrimePinDao,
    onNavigate: (NavKey) -> Unit
) {
    val vm: HomeViewModel = viewModel(factory = HomeViewModel.factory(missionDao, crimePinDao))
    val state by vm.uiState.collectAsStateWithLifecycle()

    // Bat signal pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "batsignal")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "alpha"
    )
    val rainOffset by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing)),
        label = "rain"
    )

    val now = remember { Calendar.getInstance() }
    val timeStr = remember(Unit) { SimpleDateFormat("HH:mm", Locale.getDefault()).format(now.time) }
    val dateStr = remember(Unit) { SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(now.time) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BatBlack)
    ) {
        // Rain effect background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val drops = 80
            for (i in 0 until drops) {
                val seed = i * 1234567L
                val x = ((seed % size.width.toInt()).toFloat().coerceAtLeast(0f))
                val yBase = (((seed * 7) % size.height.toInt()).toFloat().coerceAtLeast(0f))
                val y = (yBase + rainOffset * size.height) % size.height
                drawLine(
                    color = BatCyan.copy(alpha = 0.04f),
                    start = Offset(x, y),
                    end = Offset(x + 1f, y + 25f),
                    strokeWidth = 1f
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 48.dp, start = 20.dp, end = 20.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Header ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        "BATMAN",
                        style = MaterialTheme.typography.displayMedium,
                        color = BatGold
                    )
                    Text(
                        "COMMAND DASHBOARD",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextSecondary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(timeStr, style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
                    Text(dateStr, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
                // Bat Signal Icon
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .graphicsLayer {
                            scaleX = pulseScale
                            scaleY = pulseScale
                            alpha = pulseAlpha
                        }
                        .drawBehind {
                            // Outer glow rings
                            drawCircle(BatGold.copy(alpha = 0.08f), radius = size.minDimension * 0.55f)
                            drawCircle(BatGold.copy(alpha = 0.15f), radius = size.minDimension * 0.45f)
                        }
                        .background(BatSurfaceVar, CircleShape)
                        .border(2.dp, BatGold.copy(alpha = 0.7f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    BatSignalCanvas(modifier = Modifier.size(44.dp))
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Threat Level ──
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PulseIndicator(
                        color = when {
                            state.threatLevel > 0.7f -> BatRed
                            state.threatLevel > 0.4f -> BatOrange
                            else -> BatGreen
                        },
                        size = 14.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("CITY STATUS", style = MaterialTheme.typography.labelLarge)
                }
                Spacer(Modifier.height(12.dp))
                ThreatLevelBar(level = state.threatLevel, modifier = Modifier.fillMaxWidth())
            }

            Spacer(Modifier.height(16.dp))

            // ── Quick Stats ──
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickStatCard(
                    modifier = Modifier.weight(1f),
                    value = state.activeMissionCount.toString(),
                    label = "ACTIVE\nMISSIONS",
                    color = BatGold,
                    icon = Icons.Default.GpsFixed
                )
                QuickStatCard(
                    modifier = Modifier.weight(1f),
                    value = state.activeCrimeCount.toString(),
                    label = "CRIME\nINCIDENTS",
                    color = BatRed,
                    icon = Icons.Default.Warning
                )
                QuickStatCard(
                    modifier = Modifier.weight(1f),
                    value = "2089",
                    label = "GOTHAM\nCITY",
                    color = BatCyan,
                    icon = Icons.Default.LocationCity
                )
            }

            Spacer(Modifier.height(20.dp))

            // ── Navigation Grid ──
            SectionHeader("OPERATIONS", modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))

            val navItems = listOf(
                NavItem("MISSIONS", "Manage active missions and quests", Icons.Default.GpsFixed, BatGold) { onNavigate(MissionsKey) },
                NavItem("COMMS", "Secure ally communication", Icons.Default.Lock, BatCyan) { onNavigate(CommsKey) },
                NavItem("EQUIPMENT", "Armor & gadget control", Icons.Default.Build, BatOrange) { onNavigate(EquipmentKey) },
                NavItem("GOTHAM MAP", "City crime intelligence", Icons.Default.Map, BatRed) { onNavigate(MapKey) },
            )

            val bonusItems = listOf(
                NavItem("MUSIC", "Gotham Nightwatch player", Icons.Default.MusicNote, BatPurple) { onNavigate(MusicKey) },
                NavItem("WAYNE ENT.", "Corporate operations", Icons.Default.Business, BatGreen) { onNavigate(WayneKey) },
                NavItem("EMERGENCY", "SOS & rapid response", Icons.Default.Emergency, BatRed) { onNavigate(EmergencyKey) },
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                navItems.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        row.forEach { item ->
                            NavCard(item = item, modifier = Modifier.weight(1f))
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            SectionHeader("EXTRAS", modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                bonusItems.forEach { item ->
                    NavCard(item = item, modifier = Modifier.weight(1f))
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Footer ──
            Text(
                "\"I am vengeance. I am the night. I am Batman.\"",
                style = MaterialTheme.typography.bodySmall,
                color = TextDisabled,
                textAlign = TextAlign.Center
            )
        }
    }
}

data class NavItem(
    val title: String,
    val subtitle: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color,
    val onClick: () -> Unit
)

@Composable
fun NavCard(item: NavItem, modifier: Modifier = Modifier) {
    GlassCard(
        modifier = modifier.clickable { item.onClick() },
        borderColor = item.color.copy(alpha = 0.4f)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(item.color.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                .border(1.dp, item.color.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(item.icon, contentDescription = null, tint = item.color, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.height(10.dp))
        Text(item.title, style = MaterialTheme.typography.titleLarge, color = item.color)
        Spacer(Modifier.height(2.dp))
        Text(item.subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
    }
}

@Composable
fun QuickStatCard(value: String, label: String, color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    GlassCard(modifier = modifier, borderColor = color.copy(alpha = 0.3f)) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        Spacer(Modifier.height(6.dp))
        Text(value, style = MaterialTheme.typography.displaySmall, color = color)
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
    }
}

@Composable
fun BatSignalCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        // ── 2089 Symmetrical Bat Emblem ──────────────────────────────────
        val path = Path().apply {
            // Start at top center neck
            moveTo(cx, cy - h * 0.25f)
            // Left ear
            lineTo(cx - w * 0.05f, cy - h * 0.45f)
            lineTo(cx - w * 0.10f, cy - h * 0.28f)
            // Top wing curve left
            cubicTo(cx - w * 0.25f, cy - h * 0.45f, cx - w * 0.45f, cy - h * 0.30f, cx - w * 0.50f, cy - h * 0.05f)
            // Wing tip left to bottom wing curves
            cubicTo(cx - w * 0.40f, cy + h * 0.05f, cx - w * 0.25f, cy + h * 0.15f, cx - w * 0.18f, cy + h * 0.35f)
            // Bottom bat tail curves left
            cubicTo(cx - w * 0.12f, cy + h * 0.22f, cx - w * 0.05f, cy + h * 0.25f, cx, cy + h * 0.45f)
            // Symmetrical Right side - Bottom bat tail curves right
            cubicTo(cx + w * 0.05f, cy + h * 0.25f, cx + w * 0.12f, cy + h * 0.22f, cx + w * 0.18f, cy + h * 0.35f)
            // Bottom wing curves right to wing tip right
            cubicTo(cx + w * 0.25f, cy + h * 0.15f, cx + w * 0.40f, cy + h * 0.05f, cx + w * 0.50f, cy - h * 0.05f)
            // Top wing curve right
            cubicTo(cx + w * 0.45f, cy - h * 0.30f, cx + w * 0.25f, cy - h * 0.45f, cx + w * 0.10f, cy - h * 0.28f)
            // Right ear
            lineTo(cx + w * 0.05f, cy - h * 0.45f)
            close()
        }

        // Outer glow
        drawPath(path, BatGold.copy(alpha = 0.18f), style = Stroke(width = 8f))
        // Inner glow
        drawPath(path, BatGold.copy(alpha = 0.40f), style = Stroke(width = 3.5f))
        // Solid gold fill
        drawPath(path, BatGold)
        // Specular highlight rim
        drawPath(path, androidx.compose.ui.graphics.Color.White.copy(alpha = 0.12f), style = Stroke(width = 1.5f))
    }
}