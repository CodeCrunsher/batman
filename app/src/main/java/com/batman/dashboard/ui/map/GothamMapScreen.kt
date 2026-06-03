package com.batman.dashboard.ui.map

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.batman.dashboard.data.db.CrimePinEntity
import com.batman.dashboard.ui.components.*
import com.batman.dashboard.ui.theme.*
import com.batman.dashboard.ui.components.DecryptionMinigameDialog

// ─────────────────────────────────────────────────────────────────────────────
// Gotham City District Data
// Based on the fictional DC Comics Gotham — an island/peninsula city with
// Gothic architecture, divided into distinct boroughs.
// ─────────────────────────────────────────────────────────────────────────────

data class GothamDistrict(
    val name: String,
    val polygon: List<Offset>,   // normalized 0-1 coordinates
    val color: Color,
    val labelPos: Offset
)

// Gotham is modeled as a peninsula — elongated north-south with Gotham River
// to the west, upper bay to the south, connecting to the mainland to the north.
// Key districts based on canonical DC Comics Gotham geography.
val GOTHAM_DISTRICTS = listOf(
    GothamDistrict(
        name = "Old Gotham",
        polygon = listOf(
            Offset(0.25f, 0.10f), Offset(0.55f, 0.10f),
            Offset(0.58f, 0.30f), Offset(0.22f, 0.32f)
        ),
        color = Color(0xFF1A1A2E),
        labelPos = Offset(0.38f, 0.20f)
    ),
    GothamDistrict(
        name = "Diamond\nDistrict",
        polygon = listOf(
            Offset(0.55f, 0.10f), Offset(0.80f, 0.12f),
            Offset(0.82f, 0.32f), Offset(0.58f, 0.30f)
        ),
        color = Color(0xFF16213E),
        labelPos = Offset(0.68f, 0.20f)
    ),
    GothamDistrict(
        name = "The Narrows",
        polygon = listOf(
            Offset(0.22f, 0.32f), Offset(0.40f, 0.30f),
            Offset(0.40f, 0.52f), Offset(0.18f, 0.54f)
        ),
        color = Color(0xFF0F3460),
        labelPos = Offset(0.28f, 0.42f)
    ),
    GothamDistrict(
        name = "Crime Alley\n& Bowery",
        polygon = listOf(
            Offset(0.40f, 0.30f), Offset(0.60f, 0.30f),
            Offset(0.62f, 0.55f), Offset(0.38f, 0.55f)
        ),
        color = Color(0xFF1A0A2E),
        labelPos = Offset(0.50f, 0.42f)
    ),
    GothamDistrict(
        name = "Park Row",
        polygon = listOf(
            Offset(0.60f, 0.30f), Offset(0.82f, 0.32f),
            Offset(0.84f, 0.55f), Offset(0.62f, 0.55f)
        ),
        color = Color(0xFF16213E),
        labelPos = Offset(0.72f, 0.42f)
    ),
    GothamDistrict(
        name = "Amusement\nMile",
        polygon = listOf(
            Offset(0.62f, 0.55f), Offset(0.84f, 0.55f),
            Offset(0.82f, 0.72f), Offset(0.60f, 0.70f)
        ),
        color = Color(0xFF1E0A3C),
        labelPos = Offset(0.72f, 0.62f)
    ),
    GothamDistrict(
        name = "The Bowery\n(South)",
        polygon = listOf(
            Offset(0.38f, 0.55f), Offset(0.60f, 0.55f),
            Offset(0.60f, 0.72f), Offset(0.36f, 0.73f)
        ),
        color = Color(0xFF120A20),
        labelPos = Offset(0.48f, 0.63f)
    ),
    GothamDistrict(
        name = "The Narrows\nSouth",
        polygon = listOf(
            Offset(0.18f, 0.54f), Offset(0.38f, 0.55f),
            Offset(0.36f, 0.73f), Offset(0.15f, 0.72f)
        ),
        color = Color(0xFF0A1628),
        labelPos = Offset(0.26f, 0.63f)
    ),
    GothamDistrict(
        name = "Arkham\nIsland",
        polygon = listOf(
            Offset(0.05f, 0.38f), Offset(0.18f, 0.36f),
            Offset(0.18f, 0.54f), Offset(0.05f, 0.52f)
        ),
        color = Color(0xFF1C0A0A),
        labelPos = Offset(0.10f, 0.45f)
    ),
    GothamDistrict(
        name = "Uptown\nGotham",
        polygon = listOf(
            Offset(0.25f, 0.73f), Offset(0.60f, 0.72f),
            Offset(0.60f, 0.88f), Offset(0.22f, 0.88f)
        ),
        color = Color(0xFF0E1A26),
        labelPos = Offset(0.40f, 0.80f)
    ),
    GothamDistrict(
        name = "Financial\nDistrict",
        polygon = listOf(
            Offset(0.60f, 0.72f), Offset(0.82f, 0.72f),
            Offset(0.80f, 0.88f), Offset(0.60f, 0.88f)
        ),
        color = Color(0xFF101E2A),
        labelPos = Offset(0.70f, 0.80f)
    ),
)

// Rivers and water bodies
data class WaterBody(val points: List<Offset>, val label: String, val labelPos: Offset)

val GOTHAM_WATER = listOf(
    // Gotham River (west side)
    WaterBody(
        points = listOf(Offset(0f, 0.3f), Offset(0.05f, 0.32f), Offset(0.05f, 0.52f), Offset(0f, 0.55f)),
        label = "Gotham\nRiver", labelPos = Offset(0.02f, 0.43f)
    ),
    // Upper bay
    WaterBody(
        points = listOf(Offset(0f, 0.88f), Offset(0.22f, 0.88f), Offset(0.80f, 0.88f), Offset(1f, 0.88f), Offset(1f, 1f), Offset(0f, 1f)),
        label = "Gotham Bay", labelPos = Offset(0.5f, 0.94f)
    ),
    // Eastern channel
    WaterBody(
        points = listOf(Offset(0.82f, 0.10f), Offset(1f, 0.10f), Offset(1f, 0.90f), Offset(0.82f, 0.90f)),
        label = "East\nChannel", labelPos = Offset(0.91f, 0.5f)
    ),
    // North inlet
    WaterBody(
        points = listOf(Offset(0f, 0f), Offset(1f, 0f), Offset(1f, 0.10f), Offset(0.80f, 0.10f),
            Offset(0.55f, 0.10f), Offset(0.25f, 0.10f), Offset(0f, 0.10f)),
        label = "North\nInlet", labelPos = Offset(0.12f, 0.05f)
    ),
    // North-west water (left of Arkham)
    WaterBody(
        points = listOf(Offset(0f, 0.10f), Offset(0.25f, 0.10f), Offset(0.22f, 0.32f), Offset(0.18f, 0.36f), Offset(0.05f, 0.38f), Offset(0f, 0.36f)),
        label = "", labelPos = Offset(0f, 0f)
    ),
    // Channel between Arkham and mainland
    WaterBody(
        points = listOf(Offset(0f, 0.55f), Offset(0.05f, 0.52f), Offset(0.15f, 0.72f), Offset(0f, 0.75f)),
        label = "", labelPos = Offset(0f, 0f)
    ),
    // South-west channel
    WaterBody(
        points = listOf(Offset(0f, 0.75f), Offset(0.15f, 0.72f), Offset(0.22f, 0.88f), Offset(0f, 0.88f)),
        label = "", labelPos = Offset(0f, 0f)
    ),
)

// Bridges
data class Bridge(val start: Offset, val end: Offset, val name: String)

val GOTHAM_BRIDGES = listOf(
    Bridge(Offset(0.24f, 0.32f), Offset(0.18f, 0.36f), "Arkham Bridge"),
    Bridge(Offset(0.38f, 0.55f), Offset(0.36f, 0.73f), "Lower Bridge"),
    Bridge(Offset(0.60f, 0.55f), Offset(0.60f, 0.72f), "Mid Bridge"),
)

fun crimeTypeColor(type: String): Color = when (type) {
    "ROBBERY"          -> CrimeRed
    "ASSAULT"          -> CrimeOrange
    "TERRORISM"        -> CrimeCyan
    "DRUG_TRAFFICKING" -> CrimePurple
    "KIDNAPPING"       -> CrimeYellow
    else               -> BatGold
}

fun crimeTypeEmoji(type: String): String = when (type) {
    "ROBBERY"          -> "💰"
    "ASSAULT"          -> "⚡"
    "TERRORISM"        -> "☢"
    "DRUG_TRAFFICKING" -> "💊"
    "KIDNAPPING"       -> "🔒"
    else               -> "⚠"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GothamMapScreen(
    viewModel: MapViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog      by remember { mutableStateOf(false) }
    var pendingTapPosition by remember { mutableStateOf<Offset?>(null) }
    var detectiveMode      by remember { mutableStateOf(false) }
    var showMinigame       by remember { mutableStateOf(false) }
    var strikeTarget       by remember { mutableStateOf<Offset?>(null) }

    // Detective mode pulsing anim
    val infiniteAnim = rememberInfiniteTransition(label = "detectivePulse")
    val strikeRadius by infiniteAnim.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing)),
        label = "strikeRadius"
    )

    Scaffold(
        containerColor = BatBlack,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("GOTHAM CITY", style = MaterialTheme.typography.headlineLarge)
                        Text(
                            if (detectiveMode) "DETECTIVE MODE — ACTIVE" else "CRIME INTELLIGENCE MAP",
                            style = MaterialTheme.typography.headlineSmall,
                            color = if (detectiveMode) BatCyan else TextSecondary
                        )
                    }
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = BatGold) } },
                actions = {
                    // Detective Mode toggle
                    IconButton(onClick = { detectiveMode = !detectiveMode }) {
                        Icon(
                            imageVector = if (detectiveMode) Icons.Default.Visibility else Icons.Default.RemoveRedEye,
                            contentDescription = "Detective Mode",
                            tint = if (detectiveMode) BatCyan else TextSecondary
                        )
                    }
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.AddLocation, contentDescription = "Add Crime", tint = BatRed)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BatBlack)
            )
        },
        floatingActionButton = {
            if (detectiveMode) {
                ExtendedFloatingActionButton(
                    onClick = { showMinigame = true },
                    containerColor = BatCyan,
                    contentColor = BatBlack,
                    icon = { Icon(Icons.Default.GpsFixed, null) },
                    text = { Text("PREDICT STRIKE", style = MaterialTheme.typography.labelLarge) }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Map Canvas ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(8.dp)
            ) {
                GothamMapCanvas(
                    pins          = state.activePins,
                    selectedPin   = state.selectedPin,
                    detectiveMode = detectiveMode,
                    strikeTarget  = strikeTarget,
                    strikeRadius  = strikeRadius,
                    onPinTap = { viewModel.selectPin(it) },
                    onMapTap = { x, y ->
                        pendingTapPosition = Offset(x, y)
                        showAddDialog = true
                        viewModel.selectPin(null)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // ── Legend ──
            CrimeLegend()

            // ── Detective Mode info bar ──
            if (detectiveMode) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                    borderColor = BatCyan.copy(alpha = 0.5f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.RemoveRedEye, null, tint = BatCyan, modifier = Modifier.size(14.dp))
                        Text(
                            "DETECTIVE MODE: Neural pattern analysis enabled — ${state.activePins.size} anomalies tracked",
                            style = MaterialTheme.typography.labelSmall,
                            color = BatCyan
                        )
                    }
                }
            }

            // ── Crime list ──
            if (state.activePins.isNotEmpty()) {
                Text(
                    "ACTIVE INCIDENTS (${state.activePins.size})",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(state.activePins, key = { it.id }) { pin ->
                        CrimePinListItem(
                            pin = pin,
                            isSelected = state.selectedPin?.id == pin.id,
                            onClick = { viewModel.selectPin(pin) },
                            onResolve = { viewModel.resolvePin(pin) }
                        )
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
        }
    }

    // Selected pin detail
    state.selectedPin?.let { pin ->
        CrimePinDetailSheet(
            pin = pin,
            onDismiss = { viewModel.selectPin(null) },
            onResolve = { viewModel.resolvePin(pin) }
        )
    }

    // Add crime dialog
    if (showAddDialog) {
        AddCrimePinDialog(
            initialX = pendingTapPosition?.x ?: 0.5f,
            initialY = pendingTapPosition?.y ?: 0.5f,
            onDismiss = { showAddDialog = false; pendingTapPosition = null },
            onAdd = { type, district, desc, threat, x, y ->
                viewModel.addCrimePin(type, district, desc, threat, x, y)
                showAddDialog = false
                pendingTapPosition = null
            }
        )
    }

    // Predictive Strike Minigame
    if (showMinigame) {
        DecryptionMinigameDialog(
            onDismiss = { showMinigame = false },
            onSuccess = {
                // Place a random predicted strike vector on the map
                strikeTarget = Offset((0.3f..0.7f).random(), (0.3f..0.7f).random())
                showMinigame = false
            }
        )
    }
}

private fun ClosedFloatingPointRange<Float>.random(): Float =
    start + (endInclusive - start) * kotlin.random.Random.nextFloat()

@Composable
fun GothamMapCanvas(
    pins: List<CrimePinEntity>,
    selectedPin: CrimePinEntity?,
    detectiveMode: Boolean = false,
    strikeTarget: Offset? = null,
    strikeRadius: Float = 0f,
    onPinTap: (CrimePinEntity) -> Unit,
    onMapTap: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val infiniteTransition = rememberInfiniteTransition(label = "mapPulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1600, easing = LinearEasing)),
        label = "pulse"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f, targetValue = 0.2f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glow"
    )

    Canvas(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(
                1.dp,
                if (detectiveMode) BatCyan.copy(alpha = 0.7f) else BatGold.copy(alpha = 0.3f),
                RoundedCornerShape(12.dp)
            )
            .background(if (detectiveMode) Color(0xFF00080F) else Color(0xFF060612))
            .pointerInput(pins) {
                detectTapGestures { tapOffset ->
                    val normX = tapOffset.x / size.width
                    val normY = tapOffset.y / size.height
                    // Check if tapped near a pin
                    val hitPin = pins.firstOrNull { pin ->
                        val pinPx = Offset(pin.mapX * size.width, pin.mapY * size.height)
                        val dist = (tapOffset - pinPx).getDistance()
                        dist < 30f
                    }
                    if (hitPin != null) {
                        onPinTap(hitPin)
                    } else {
                        onMapTap(normX, normY)
                    }
                }
            }
    ) {
        val w = size.width
        val h = size.height

        // ── Water bodies ──
        for (water in GOTHAM_WATER) {
            val path = Path().apply {
                val pts = water.points
                if (pts.isEmpty()) return@apply
                moveTo(pts[0].x * w, pts[0].y * h)
                for (i in 1 until pts.size) {
                    lineTo(pts[i].x * w, pts[i].y * h)
                }
                close()
            }
            drawPath(path, color = Color(0xFF0A1628))
        }

        // ── Districts ──
        for (district in GOTHAM_DISTRICTS) {
            val path = Path().apply {
                val pts = district.polygon
                moveTo(pts[0].x * w, pts[0].y * h)
                for (i in 1 until pts.size) lineTo(pts[i].x * w, pts[i].y * h)
                close()
            }
            // Fill
            drawPath(path, color = district.color)
            // Border
            drawPath(path, color = Color(0xFF2A3A55), style = Stroke(width = 1.5f))
        }

        // ── Major roads (grid-ish) ──
        val roadColor = Color(0xFF1E2A3A)
        // Horizontal roads
        for (y in listOf(0.20f, 0.32f, 0.45f, 0.57f, 0.70f, 0.80f)) {
            drawLine(roadColor, Offset(0.15f * w, y * h), Offset(0.85f * w, y * h), strokeWidth = 2f)
        }
        // Vertical roads
        for (x in listOf(0.25f, 0.38f, 0.50f, 0.62f, 0.75f)) {
            drawLine(roadColor, Offset(x * w, 0.10f * h), Offset(x * w, 0.90f * h), strokeWidth = 2f)
        }
        // Wayne Tower boulevard (diagonal)
        drawLine(Color(0xFF253545), Offset(0.35f * w, 0.12f * h), Offset(0.55f * w, 0.32f * h), strokeWidth = 3f)

        // ── Bridges ──
        for (bridge in GOTHAM_BRIDGES) {
            drawLine(
                color = BatGold.copy(alpha = 0.6f),
                start = Offset(bridge.start.x * w, bridge.start.y * h),
                end = Offset(bridge.end.x * w, bridge.end.y * h),
                strokeWidth = 4f
            )
        }

        // ── Wayne Tower landmark ──
        val wt = Offset(0.47f * w, 0.22f * h)
        drawRect(
            color = BatGold.copy(alpha = 0.8f),
            topLeft = Offset(wt.x - 5f, wt.y - 10f),
            size = Size(10f, 10f)
        )
        drawRect(
            color = BatGold.copy(alpha = 0.5f),
            topLeft = Offset(wt.x - 3f, wt.y - 14f),
            size = Size(6f, 4f)
        )

        // ── District labels ──
        val labelStyle = TextStyle(
            color = Color(0xFF7A9BB5),
            fontSize = 8.sp,
            fontWeight = FontWeight.Normal
        )
        for (district in GOTHAM_DISTRICTS) {
            val measured = textMeasurer.measure(district.name, labelStyle)
            drawText(
                textMeasurer = textMeasurer,
                text = district.name,
                topLeft = Offset(
                    district.labelPos.x * w - measured.size.width / 2f,
                    district.labelPos.y * h - measured.size.height / 2f
                ),
                style = labelStyle
            )
        }

        // ── Water labels ──
        val waterLabelStyle = TextStyle(color = Color(0xFF1E4A6A), fontSize = 7.sp)
        for (water in GOTHAM_WATER) {
            if (water.label.isNotEmpty()) {
                val measured = textMeasurer.measure(water.label, waterLabelStyle)
                drawText(
                    textMeasurer = textMeasurer,
                    text = water.label,
                    topLeft = Offset(
                        water.labelPos.x * w - measured.size.width / 2f,
                        water.labelPos.y * h - measured.size.height / 2f
                    ),
                    style = waterLabelStyle
                )
            }
        }

        // ── Wayne Tower label ──
        val wtLabel = textMeasurer.measure("Wayne Tower", TextStyle(color = BatGold.copy(0.8f), fontSize = 7.sp, fontWeight = FontWeight.Bold))
        drawText(textMeasurer, "Wayne Tower", Offset(wt.x - wtLabel.size.width / 2f, wt.y + 4f), TextStyle(color = BatGold.copy(0.8f), fontSize = 7.sp, fontWeight = FontWeight.Bold))

        // ── Crime Pins ──
        for (pin in pins) {
            val pinCenter = Offset(pin.mapX * w, pin.mapY * h)
            val pinColor = crimeTypeColor(pin.type)
            val isSelected = selectedPin?.id == pin.id
            val pinRadius = if (isSelected) 14f else 10f

            // Animated pulse ring
            val ringRadius = pinRadius + pulseRadius * 24f
            drawCircle(
                color = pinColor.copy(alpha = (1f - pulseRadius) * 0.5f),
                radius = ringRadius,
                center = pinCenter,
                style = Stroke(2f)
            )
            // Second ring
            val ring2 = pinRadius + pulseRadius * 16f
            drawCircle(
                color = pinColor.copy(alpha = (1f - pulseRadius) * 0.3f),
                radius = ring2,
                center = pinCenter
            )
            // Pin body
            drawCircle(
                color = pinColor.copy(alpha = if (isSelected) glowAlpha else 0.85f),
                radius = pinRadius,
                center = pinCenter
            )
            // Pin border
            drawCircle(
                color = pinColor,
                radius = pinRadius,
                center = pinCenter,
                style = Stroke(1.5f)
            )
            // Threat dots
            for (i in 0 until pin.threatLevel.coerceAtMost(5)) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.9f),
                    radius = 1.5f,
                    center = Offset(pinCenter.x - 4f + i * 2f, pinCenter.y)
                )
            }
        }

        // ── Detective Mode overlay ──
        if (detectiveMode) {
            // Pixel grid
            val gridStep = w / 20f
            var gx = 0f
            while (gx <= w) {
                drawLine(BatCyan.copy(alpha = 0.04f), Offset(gx, 0f), Offset(gx, h), 0.5f)
                gx += gridStep
            }
            var gy = 0f
            while (gy <= h) {
                drawLine(BatCyan.copy(alpha = 0.04f), Offset(0f, gy), Offset(w, gy), 0.5f)
                gy += gridStep
            }
            // Neon-cyan highlight on district borders
            for (district in GOTHAM_DISTRICTS) {
                val path = Path().apply {
                    val pts = district.polygon
                    moveTo(pts[0].x * w, pts[0].y * h)
                    for (i in 1 until pts.size) lineTo(pts[i].x * w, pts[i].y * h)
                    close()
                }
                drawPath(path, color = BatCyan.copy(alpha = 0.18f), style = Stroke(width = 1f))
            }
            // Gold tunnel paths from Wayne Tower to each crime pin
            val wayneTower = Offset(0.47f * w, 0.22f * h)
            for (pin in pins) {
                val pinPx = Offset(pin.mapX * w, pin.mapY * h)
                drawLine(
                    color = BatGold.copy(alpha = 0.25f),
                    start = wayneTower,
                    end   = pinPx,
                    strokeWidth = 1.5f,
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
                )
            }
            // Strike vector: pulsing dashed circle
            strikeTarget?.let { st ->
                val stPx = Offset(st.x * w, st.y * h)
                val baseR = 35f
                drawCircle(
                    color  = BatGold.copy(alpha = (1f - strikeRadius) * 0.7f),
                    radius = baseR + strikeRadius * 40f,
                    center = stPx,
                    style  = Stroke(
                        width      = 2f,
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 5f))
                    )
                )
                drawCircle(BatGold.copy(alpha = 0.9f), 6f, stPx)
                drawCircle(BatGold.copy(alpha = 0.4f), 14f, stPx, style = Stroke(1f))
            }
        }

        // ── Compass Rose ──
        val cx = w - 28f
        val cy = h - 28f
        val compassColor = if (detectiveMode) BatCyan else BatGold
        drawLine(compassColor.copy(0.6f), Offset(cx, cy - 16f), Offset(cx, cy + 16f), 1.5f)
        drawLine(compassColor.copy(0.6f), Offset(cx - 16f, cy), Offset(cx + 16f, cy), 1.5f)
        drawCircle(compassColor.copy(0.3f), 18f, Offset(cx, cy), style = Stroke(1f))
    }
}

@Composable
fun CrimeLegend() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val items = listOf(
            "ROBBERY" to CrimeRed,
            "ASSAULT" to CrimeOrange,
            "TERRORISM" to CrimeCyan,
            "DRUGS" to CrimePurple,
            "KIDNAPPING" to CrimeYellow
        )
        items.forEach { (label, color) ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(Modifier.size(8.dp).background(color, CircleShape))
                Text(label, style = MaterialTheme.typography.labelSmall, color = color)
            }
        }
    }
}

@Composable
fun CrimePinListItem(
    pin: CrimePinEntity,
    isSelected: Boolean,
    onClick: () -> Unit,
    onResolve: () -> Unit
) {
    val color = crimeTypeColor(pin.type)
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        borderColor = if (isSelected) color else color.copy(alpha = 0.2f)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).background(color, CircleShape))
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text("${crimeTypeEmoji(pin.type)} ${pin.type.replace("_", " ")}", style = MaterialTheme.typography.labelMedium, color = color)
                Text(pin.district, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            if (isSelected) {
                TextButton(onClick = onResolve, contentPadding = PaddingValues(4.dp)) {
                    Text("RESOLVE", style = MaterialTheme.typography.labelSmall, color = BatGreen)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrimePinDetailSheet(
    pin: CrimePinEntity,
    onDismiss: () -> Unit,
    onResolve: () -> Unit
) {
    val color = crimeTypeColor(pin.type)
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BatSurfaceVar,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.size(12.dp).background(color, CircleShape))
                Text("${pin.type.replace("_", " ")}", style = MaterialTheme.typography.headlineLarge, color = color)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoRow("DISTRICT", pin.district)
                InfoRow("THREAT LEVEL", "⬛".repeat(pin.threatLevel) + "□".repeat(5 - pin.threatLevel))
                InfoRow("DESCRIPTION", pin.description)
                InfoRow("COORDINATES", "%.2f, %.2f".format(pin.mapX, pin.mapY))
            }
        },
        confirmButton = {
            Button(
                onClick = { onResolve(); onDismiss() },
                colors = ButtonDefaults.buttonColors(containerColor = BatGreen, contentColor = BatBlack)
            ) { Text("RESOLVE INCIDENT", style = MaterialTheme.typography.labelLarge) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CLOSE", color = TextSecondary) }
        }
    )
}

@Composable
fun InfoRow(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCrimePinDialog(
    initialX: Float,
    initialY: Float,
    onDismiss: () -> Unit,
    onAdd: (String, String, String, Int, Float, Float) -> Unit
) {
    var selectedType by remember { mutableStateOf("ROBBERY") }
    var selectedDistrict by remember { mutableStateOf("Old Gotham") }
    var description by remember { mutableStateOf("") }
    var threatLevel by remember { mutableStateOf(3) }

    val districts = GOTHAM_DISTRICTS.map { it.name.replace("\n", " ") }
    val types = listOf("ROBBERY", "ASSAULT", "TERRORISM", "DRUG_TRAFFICKING", "KIDNAPPING")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BatSurfaceVar,
        title = { Text("REPORT CRIME", style = MaterialTheme.typography.headlineLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text("CRIME TYPE", style = MaterialTheme.typography.labelLarge)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    types.forEach { type ->
                        val color = crimeTypeColor(type)
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type.replace("_", " "), style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = color.copy(0.2f), selectedLabelColor = color)
                        )
                    }
                }
                Text("DISTRICT", style = MaterialTheme.typography.labelLarge)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    districts.forEach { d ->
                        FilterChip(
                            selected = selectedDistrict == d,
                            onClick = { selectedDistrict = d },
                            label = { Text(d, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = BatCyan.copy(0.2f), selectedLabelColor = BatCyan)
                        )
                    }
                }
                Text("THREAT LEVEL: $threatLevel / 5", style = MaterialTheme.typography.labelLarge)
                Slider(
                    value = threatLevel.toFloat(),
                    onValueChange = { threatLevel = it.toInt() },
                    valueRange = 1f..5f,
                    steps = 3,
                    colors = SliderDefaults.colors(thumbColor = BatGold, activeTrackColor = BatGold)
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BatGold, unfocusedBorderColor = BatBorder, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(selectedType, selectedDistrict, description, threatLevel, initialX, initialY) },
                colors = ButtonDefaults.buttonColors(containerColor = BatRed, contentColor = Color.White)
            ) { Text("REPORT", style = MaterialTheme.typography.labelLarge) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCEL", color = TextSecondary) }
        }
    )
}
