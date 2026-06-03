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
    missionDao:      MissionDao,
    crimePinDao:     CrimePinDao,
    isStealthMode:   Boolean,
    onToggleStealth: () -> Unit,
    onThreatUpdate:  (Float) -> Unit,
    onNavigate:      (NavKey) -> Unit
) {
    val vm: HomeViewModel = viewModel(factory = HomeViewModel.factory(missionDao, crimePinDao))
    val state by vm.uiState.collectAsStateWithLifecycle()

    // Push threat level up to MainActivity so the theme can react
    LaunchedEffect(state.threatLevel) {
        onThreatUpdate(state.threatLevel)
    }

    // ── Resolve dynamic colours from the current theme ──
    val isAlarm = state.threatLevel >= 0.75f
    val accentColor = when {
        isStealthMode -> Color(0xFFFF0000)
        isAlarm       -> BatRed
        else          -> BatGold
    }
    val bgColor = if (isStealthMode) Color(0xFF0A0000) else BatBlack

    // ── Rain / Matrix speed: slow in stealth ──
    val rainDuration = if (isStealthMode) 8000 else 3000

    val infiniteTransition = rememberInfiniteTransition(label = "home_anim")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(if (isStealthMode) 4000 else 2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(if (isStealthMode) 4000 else 2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "alpha"
    )
    val rainOffset by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(rainDuration, easing = LinearEasing)),
        label = "rain"
    )

    // Alarm breathing for BatRed mode
    val alarmAlpha by infiniteTransition.animateFloat(
        initialValue = 0.05f, targetValue = 0.20f,
        animationSpec = infiniteRepeatable(tween(700, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "alarm"
    )

    val now    = remember { Calendar.getInstance() }
    val timeStr = remember { SimpleDateFormat("HH:mm",              Locale.getDefault()).format(now.time) }
    val dateStr = remember { SimpleDateFormat("EEE, dd MMM yyyy",   Locale.getDefault()).format(now.time) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        // ── Background: rain / matrix drops ──
        Canvas(modifier = Modifier.fillMaxSize()) {
            val drops = if (isStealthMode) 40 else 80
            val dropColor = if (isStealthMode) Color(0xFFFF0000) else BatCyan
            for (i in 0 until drops) {
                val seed  = i * 1234567L
                val x     = ((seed % size.width.toInt()).toFloat().coerceAtLeast(0f))
                val yBase = (((seed * 7) % size.height.toInt()).toFloat().coerceAtLeast(0f))
                val y     = (yBase + rainOffset * size.height) % size.height
                drawLine(
                    color       = dropColor.copy(alpha = if (isStealthMode) 0.07f else 0.04f),
                    start       = Offset(x, y),
                    end         = Offset(x + 1f, y + 25f),
                    strokeWidth = 1f
                )
            }
            // Red alarm vignette when threat is critical
            if (isAlarm && !isStealthMode) {
                drawRect(color = BatRed.copy(alpha = alarmAlpha))
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
                        color = accentColor
                    )
                    Text(
                        if (isStealthMode) "STEALTH MODE ACTIVE" else "COMMAND DASHBOARD",
                        style = MaterialTheme.typography.headlineSmall,
                        color = if (isStealthMode) Color(0xFFCC0000) else TextSecondary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(timeStr, style = MaterialTheme.typography.headlineMedium, color = if (isStealthMode) Color(0xFFFF0000) else TextPrimary)
                    Text(dateStr, style = MaterialTheme.typography.bodySmall,      color = if (isStealthMode) Color(0xFFCC0000) else TextSecondary)
                }

                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // ── Stealth Toggle Button ──
                    IconButton(
                        onClick = onToggleStealth,
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                if (isStealthMode) Color(0xFF3D0000) else BatSurfaceVar,
                                CircleShape
                            )
                            .border(
                                1.dp,
                                if (isStealthMode) Color(0xFFFF0000) else BatGold.copy(alpha = 0.5f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = if (isStealthMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle Stealth Mode",
                            tint = if (isStealthMode) Color(0xFFFF0000) else BatGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Bat Signal Icon
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .graphicsLayer {
                                scaleX = pulseScale
                                scaleY = pulseScale
                                alpha  = pulseAlpha
                            }
                            .drawBehind {
                                drawCircle(accentColor.copy(alpha = 0.08f), radius = size.minDimension * 0.55f)
                                drawCircle(accentColor.copy(alpha = 0.15f), radius = size.minDimension * 0.45f)
                            }
                            .background(BatSurfaceVar, CircleShape)
                            .border(2.dp, accentColor.copy(alpha = 0.7f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        BatSignalCanvas(modifier = Modifier.size(40.dp), color = accentColor, scanPhase = rainOffset)
                    }
                }
            }

            // ── Stealth Mode Banner ──
            if (isStealthMode) {
                Spacer(Modifier.height(12.dp))
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = Color(0xFFFF0000).copy(alpha = 0.6f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.VisibilityOff, contentDescription = null,
                            tint = Color(0xFFFF0000), modifier = Modifier.size(16.dp))
                        Text(
                            "Stealth mode active",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFFFF0000)
                        )
                    }
                }
            }


            if (isAlarm && !isStealthMode) {
                Spacer(Modifier.height(12.dp))
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = BatRed.copy(alpha = 0.8f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null,
                            tint = BatRed, modifier = Modifier.size(16.dp))
                        Text(
                            "⚠ CRITICAL THREAT LEVEL — EMERGENCY PROTOCOLS ENGAGED",
                            style = MaterialTheme.typography.labelMedium,
                            color = BatRed
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Threat Level ──
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PulseIndicator(
                        color = when {
                            state.threatLevel > 0.7f -> if (isStealthMode) Color(0xFFFF0000) else BatRed
                            state.threatLevel > 0.4f -> BatOrange
                            else                     -> BatGreen
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
                    value    = state.activeMissionCount.toString(),
                    label    = "ACTIVE\nMISSIONS",
                    color    = accentColor,
                    icon     = Icons.Default.GpsFixed
                )
                QuickStatCard(
                    modifier = Modifier.weight(1f),
                    value    = state.activeCrimeCount.toString(),
                    label    = "CRIME\nINCIDENTS",
                    color    = BatRed,
                    icon     = Icons.Default.Warning
                )
                QuickStatCard(
                    modifier = Modifier.weight(1f),
                    value    = "2089",
                    label    = "GOTHAM\nCITY",
                    color    = BatCyan,
                    icon     = Icons.Default.LocationCity
                )
            }

            Spacer(Modifier.height(20.dp))

            // ── Navigation Grid ──
            SectionHeader("OPERATIONS", modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))

            val navItems = listOf(
                NavItem("MISSIONS",   "Manage active missions and quests", Icons.Default.GpsFixed,      accentColor) { onNavigate(MissionsKey) },
                NavItem("COMMS",      "Secure ally communication",         Icons.Default.Lock,           BatCyan)    { onNavigate(CommsKey) },
                NavItem("EQUIPMENT",  "Armor & gadget control",            Icons.Default.Build,          BatOrange)  { onNavigate(EquipmentKey) },
                NavItem("GOTHAM MAP", "City crime intelligence",           Icons.Default.Map,            BatRed)     { onNavigate(MapKey) },
            )

            val bonusItems = listOf(
                NavItem("MUSIC",     "Gotham Nightwatch player",  Icons.Default.MusicNote,  BatPurple) { onNavigate(MusicKey) },
                NavItem("WAYNE ENT.","Corporate operations",      Icons.Default.Business,   BatGreen)  { onNavigate(WayneKey) },
                NavItem("EMERGENCY", "SOS & rapid response",      Icons.Default.Emergency,  BatRed)    { onNavigate(EmergencyKey) },
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

            Text(
                "\"I am vengeance. I am the night. I am Batman.\"",
                style = MaterialTheme.typography.bodySmall,
                color = if (isStealthMode) Color(0xFF660000) else TextDisabled,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─────────────────── Helper composables ───────────────────

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
        modifier    = modifier.clickable { item.onClick() },
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
        Text(item.title,    style = MaterialTheme.typography.titleLarge, color = item.color)
        Spacer(Modifier.height(2.dp))
        Text(item.subtitle, style = MaterialTheme.typography.bodySmall,  color = TextSecondary)
    }
}

@Composable
fun QuickStatCard(
    value: String,
    label: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier, borderColor = color.copy(alpha = 0.3f)) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        Spacer(Modifier.height(6.dp))
        Text(value, style = MaterialTheme.typography.displaySmall, color = color)
        Text(label, style = MaterialTheme.typography.bodySmall,    color = TextSecondary)
    }
}

@Composable
fun BatSignalCanvas(
    modifier:  Modifier = Modifier,
    color:     Color    = BatGold,
    scanPhase: Float    = 0f,
) {
    Canvas(modifier = modifier) {
        val w   = size.width
        val h   = size.height
        val cx  = w / 2f
        val cy  = h / 2f
        val r   = minOf(w, h) / 2f
        val dashOffset = scanPhase * 40f


        val ringCount = 4
        for (i in 1..ringCount) {
            val ringR  = r * i / ringCount.toFloat()
            val alpha  = if (i == ringCount) 0.35f else 0.12f + i * 0.03f
            val sw     = if (i == ringCount) 1.2f else 0.6f
            drawCircle(
                color  = color.copy(alpha = alpha),
                radius = ringR,
                center = Offset(cx, cy),
                style  = Stroke(
                    width      = sw,
                    pathEffect = if (i % 2 == 0)
                        PathEffect.dashPathEffect(floatArrayOf(6f, 4f), dashOffset)
                    else null
                )
            )
        }

        val crossDash = PathEffect.dashPathEffect(floatArrayOf(8f, 5f), dashOffset * 0.7f)
        drawLine(
            color       = color.copy(alpha = 0.22f),
            start       = Offset(cx - r, cy),
            end         = Offset(cx + r, cy),
            strokeWidth = 0.7f,
            pathEffect  = crossDash
        )
        drawLine(
            color       = color.copy(alpha = 0.22f),
            start       = Offset(cx, cy - r),
            end         = Offset(cx, cy + r),
            strokeWidth = 0.7f,
            pathEffect  = crossDash
        )
        val diagEnd = r * 0.65f
        val diagDash = PathEffect.dashPathEffect(floatArrayOf(4f, 6f), dashOffset * 0.5f)
        listOf(
            Offset(-diagEnd, -diagEnd) to Offset(diagEnd, diagEnd),
            Offset(diagEnd, -diagEnd)  to Offset(-diagEnd, diagEnd)
        ).forEach { (s, e) ->
            drawLine(
                color       = color.copy(alpha = 0.10f),
                start       = Offset(cx + s.x, cy + s.y),
                end         = Offset(cx + e.x, cy + e.y),
                strokeWidth = 0.6f,
                pathEffect  = diagDash
            )
        }

        for (deg in 0 until 360 step 30) {
            val rad    = Math.toRadians(deg.toDouble())
            val sin    = Math.sin(rad).toFloat()
            val cos    = Math.cos(rad).toFloat()
            val tickLen = if (deg % 90 == 0) r * 0.12f else r * 0.07f
            drawLine(
                color       = color.copy(alpha = if (deg % 90 == 0) 0.55f else 0.25f),
                start       = Offset(cx + cos * (r - tickLen), cy + sin * (r - tickLen)),
                end         = Offset(cx + cos * r,             cy + sin * r),
                strokeWidth = if (deg % 90 == 0) 1.2f else 0.6f
            )
        }

        val scanSpacing = h / 18f
        var sy = 0f
        while (sy < h) {
            drawLine(
                color       = BatCyan.copy(alpha = 0.045f),
                start       = Offset(0f, sy),
                end         = Offset(w, sy),
                strokeWidth = 0.6f
            )
            sy += scanSpacing
        }

        val sweepY = (scanPhase * (h + scanSpacing * 2f)) - scanSpacing
        drawLine(
            color       = BatCyan.copy(alpha = 0.35f),
            start       = Offset(0f, sweepY),
            end         = Offset(w, sweepY),
            strokeWidth = 1.2f
        )
        drawLine(
            color       = BatCyan.copy(alpha = 0.10f),
            start       = Offset(0f, sweepY - 3f),
            end         = Offset(w, sweepY - 3f),
            strokeWidth = 2.4f
        )

        val batPath = Path().apply {
            moveTo(cx, cy - h * 0.25f)
            lineTo(cx - w * 0.05f, cy - h * 0.45f)
            lineTo(cx - w * 0.10f, cy - h * 0.28f)
            cubicTo(cx - w * 0.25f, cy - h * 0.45f, cx - w * 0.45f, cy - h * 0.30f, cx - w * 0.50f, cy - h * 0.05f)
            cubicTo(cx - w * 0.40f, cy + h * 0.05f, cx - w * 0.25f, cy + h * 0.15f, cx - w * 0.18f, cy + h * 0.35f)
            cubicTo(cx - w * 0.12f, cy + h * 0.22f, cx - w * 0.05f, cy + h * 0.25f, cx, cy + h * 0.45f)
            cubicTo(cx + w * 0.05f, cy + h * 0.25f, cx + w * 0.12f, cy + h * 0.22f, cx + w * 0.18f, cy + h * 0.35f)
            cubicTo(cx + w * 0.25f, cy + h * 0.15f, cx + w * 0.40f, cy + h * 0.05f, cx + w * 0.50f, cy - h * 0.05f)
            cubicTo(cx + w * 0.45f, cy - h * 0.30f, cx + w * 0.25f, cy - h * 0.45f, cx + w * 0.10f, cy - h * 0.28f)
            lineTo(cx + w * 0.05f, cy - h * 0.45f)
            close()
        }

        drawPath(batPath, color.copy(alpha = 0.07f), style = Stroke(width = 18f))
        drawPath(batPath, color.copy(alpha = 0.18f), style = Stroke(width = 9f))
        drawPath(batPath, color.copy(alpha = 0.45f), style = Stroke(width = 3.5f))
        drawPath(
            path  = batPath,
            color = color.copy(alpha = 0.92f),
            style = Stroke(
                width      = 1.6f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 3f), dashOffset * 1.2f)
            )
        )
        drawPath(
            path  = batPath,
            color = Color.White.copy(alpha = 0.18f),
            style = Stroke(width = 0.8f)
        )

        val bLen = r * 0.28f
        val bGap = r * 0.10f
        val bSW  = 1.5f
        val bAlpha = 0.60f
        val corners = listOf(
            Offset(cx - r + bGap, cy - r + bGap) to Pair(Offset(bLen, 0f), Offset(0f, bLen)),
            Offset(cx + r - bGap, cy - r + bGap) to Pair(Offset(-bLen, 0f), Offset(0f, bLen)),
            Offset(cx - r + bGap, cy + r - bGap) to Pair(Offset(bLen, 0f), Offset(0f, -bLen)),
            Offset(cx + r - bGap, cy + r - bGap) to Pair(Offset(-bLen, 0f), Offset(0f, -bLen)),
        )
        corners.forEach { (origin, arms) ->
            drawLine(color.copy(alpha = bAlpha), origin, Offset(origin.x + arms.first.x, origin.y), bSW)
            drawLine(color.copy(alpha = bAlpha), origin, Offset(origin.x, origin.y + arms.second.y), bSW)
        }

        drawCircle(color.copy(alpha = 0.85f), radius = 2.5f, center = Offset(cx, cy))
        drawCircle(color.copy(alpha = 0.30f), radius = 6f,   center = Offset(cx, cy), style = Stroke(1f))
    }
}