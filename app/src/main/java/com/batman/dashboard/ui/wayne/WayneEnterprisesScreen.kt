package com.batman.dashboard.ui.wayne

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
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.batman.dashboard.ui.components.*
import com.batman.dashboard.ui.theme.*

data class Department(
    val name: String,
    val head: String,
    val projects: Int,
    val status: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

data class GadgetPipeline(val name: String, val progress: Float, val stage: String)

val DEPARTMENTS = listOf(
    Department("R&D", "Lucius Fox", 7, "ACTIVE", Icons.Default.Science),
    Department("Security", "Marcus Kane", 3, "ACTIVE", Icons.Default.Security),
    Department("Legal", "Jennifer Cole", 12, "ACTIVE", Icons.Default.Gavel),
    Department("Finance", "Harold Allnut", 5, "ACTIVE", Icons.Default.AccountBalance),
    Department("PR", "Vicki Vale", 4, "ACTIVE", Icons.Default.Campaign),
    Department("Aerospace", "Samuel Wu", 2, "REVIEW", Icons.Default.RocketLaunch),
)

val GADGET_PIPELINE = listOf(
    GadgetPipeline("Nano-Fiber Batsuit Mk VIII", 0.78f, "FIELD TESTING"),
    GadgetPipeline("Quantum EMP Disruptor", 0.45f, "PROTOTYPING"),
    GadgetPipeline("AI Tactical Assistant", 0.92f, "CALIBRATION"),
    GadgetPipeline("Batwing 3.0", 0.31f, "DESIGN"),
    GadgetPipeline("Grapple Cannon MkII", 0.60f, "MANUFACTURING"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WayneEnterprisesScreen(onBack: () -> Unit) {
    val wayneViewModel: WayneViewModel = viewModel()
    val state by wayneViewModel.uiState.collectAsStateWithLifecycle()

    val infiniteTransition = rememberInfiniteTransition(label = "wayne")
    val stockValue by infiniteTransition.animateFloat(
        initialValue = 248.50f, targetValue = 249.80f,
        animationSpec = infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "stock"
    )

    if (state.isAddDialogOpen) {
        AddMeetingDialog(
            onDismiss = wayneViewModel::closeDialog,
            onSave = { title, location, time -> wayneViewModel.addMeeting(title, location, time) }
        )
    }

    Scaffold(
        containerColor = BatBlack,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("WAYNE ENTERPRISES", style = MaterialTheme.typography.headlineLarge)
                        Text("CORPORATE OPERATIONS", style = MaterialTheme.typography.headlineSmall)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = BatGold) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BatBlack)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = wayneViewModel::openAddDialog,
                containerColor = BatGold,
                contentColor = BatBlack
            ) {
                Icon(Icons.Default.Add, contentDescription = "Schedule Meeting")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Stock ticker
            item {
                GlassCard(Modifier.fillMaxWidth(), borderColor = BatGreen.copy(0.4f)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("WNE", style = MaterialTheme.typography.labelLarge)
                            Text("Wayne Enterprises Inc.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("\$%.2f".format(stockValue), style = MaterialTheme.typography.displaySmall, color = BatGreen)
                            Text("\u25b2 +1.24 (+0.50%)", style = MaterialTheme.typography.bodySmall, color = BatGreen)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Canvas(Modifier.fillMaxWidth().height(40.dp)) {
                        val pts = listOf(0.4f, 0.5f, 0.38f, 0.6f, 0.55f, 0.7f, 0.65f, 0.8f, 0.75f, 0.9f)
                        val path = Path()
                        pts.forEachIndexed { i, v ->
                            val x = i / (pts.size - 1f) * size.width
                            val y = (1f - v) * size.height
                            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }
                        drawPath(path, BatGreen, style = Stroke(2f))
                        val fillPath = Path()
                        fillPath.addPath(path)
                        fillPath.lineTo(size.width, size.height)
                        fillPath.lineTo(0f, size.height)
                        fillPath.close()
                        drawPath(fillPath, Brush.verticalGradient(listOf(BatGreen.copy(0.3f), BatGreen.copy(0f))))
                    }
                }
            }

            // Analytics bar charts
            item {
                SectionHeader("QUARTERLY ANALYTICS", Modifier.fillMaxWidth())
            }
            item {
                GlassCard(Modifier.fillMaxWidth(), borderColor = BatGold.copy(0.3f)) {
                    Text("REVENUE BY DIVISION (Billions)", style = MaterialTheme.typography.labelLarge, color = BatGold)
                    Spacer(Modifier.height(8.dp))
                    val revenueData = listOf(
                        "R&D" to 1.2f, "Defense" to 0.9f, "AeroSpace" to 0.6f,
                        "Energy" to 0.8f, "Medical" to 0.4f, "Comms" to 0.3f
                    )
                    WayneBarChart(
                        data     = revenueData,
                        barColor = BatGold,
                        modifier = Modifier.fillMaxWidth().height(130.dp)
                    )
                }
            }
            item {
                GlassCard(Modifier.fillMaxWidth(), borderColor = BatCyan.copy(0.3f)) {
                    Text("HEADCOUNT BY DIVISION (Thousands)", style = MaterialTheme.typography.labelLarge, color = BatCyan)
                    Spacer(Modifier.height(8.dp))
                    val headcountData = listOf(
                        "R&D" to 3.2f, "Defense" to 2.1f, "AeroSpace" to 1.8f,
                        "Energy" to 2.5f, "Medical" to 1.4f, "Comms" to 1.4f
                    )
                    WayneBarChart(
                        data     = headcountData,
                        barColor = BatCyan,
                        modifier = Modifier.fillMaxWidth().height(130.dp)
                    )
                }
            }
            item {
                GlassCard(Modifier.fillMaxWidth(), borderColor = BatGreen.copy(0.3f)) {
                    Text("PROJECT COMPLETION RATE (%)", style = MaterialTheme.typography.labelLarge, color = BatGreen)
                    Spacer(Modifier.height(8.dp))
                    val completionData = DEPARTMENTS.map { it.name to it.projects.toFloat() / 15f * 100f }
                    WayneBarChart(
                        data     = completionData,
                        barColor = BatGreen,
                        modifier = Modifier.fillMaxWidth().height(130.dp)
                    )
                }
            }

            // Departments
            item { SectionHeader("DEPARTMENTS", Modifier.fillMaxWidth()) }
            items(DEPARTMENTS) { dept ->
                GlassCard(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(44.dp)
                                .background(BatGold.copy(0.1f), RoundedCornerShape(10.dp))
                                .border(1.dp, BatGold.copy(0.3f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(dept.icon, null, tint = BatGold, modifier = Modifier.size(24.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(dept.name, style = MaterialTheme.typography.titleLarge)
                            Text("Head: ${dept.head}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Text("${dept.projects} active projects", style = MaterialTheme.typography.bodySmall, color = TextDisabled)
                        }
                        StatusChip(dept.status, if (dept.status == "ACTIVE") BatGreen else BatOrange)
                    }
                }
            }

            // Tech pipeline
            item { SectionHeader("LUCIUS FOX \u2014 TECH PIPELINE", Modifier.fillMaxWidth()) }
            items(GADGET_PIPELINE) { gadget ->
                GlassCard(Modifier.fillMaxWidth(), borderColor = BatCyan.copy(0.2f)) {
                    Text(gadget.name, style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(4.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(gadget.stage, style = MaterialTheme.typography.labelMedium, color = BatCyan)
                        Text("${(gadget.progress * 100).toInt()}%", style = MaterialTheme.typography.labelMedium, color = BatGold)
                    }
                    Spacer(Modifier.height(6.dp))
                    BatteryBar(level = (gadget.progress * 100).toInt(), modifier = Modifier.fillMaxWidth())
                }
            }

            // Dynamic meetings list
            item { SectionHeader("BOARD MEETINGS", Modifier.fillMaxWidth()) }
            items(state.meetings, key = { it.id }) { meeting ->
                GlassCard(Modifier.fillMaxWidth(), borderColor = BatOrange.copy(0.3f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.Event, null, tint = BatOrange, modifier = Modifier.size(22.dp))
                        Column(Modifier.weight(1f)) {
                            Text(meeting.title, style = MaterialTheme.typography.titleLarge, color = BatOrange)
                            Text(meeting.location, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Text(meeting.time, style = MaterialTheme.typography.bodySmall, color = TextDisabled)
                        }
                        IconButton(
                            onClick = { wayneViewModel.deleteMeeting(meeting.id) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Close, null, tint = TextDisabled, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
            if (state.meetings.isEmpty()) {
                item {
                    Text("No meetings scheduled", style = MaterialTheme.typography.bodyMedium, color = TextDisabled)
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMeetingDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BatSurfaceVar,
        title = { Text("SCHEDULE MEETING", style = MaterialTheme.typography.headlineLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Meeting Title", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BatGold, unfocusedBorderColor = BatBorder,
                        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary
                    )
                )
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BatGold, unfocusedBorderColor = BatBorder,
                        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary
                    )
                )
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Time (e.g. Tomorrow, 09:00 AM)", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BatGold, unfocusedBorderColor = BatBorder,
                        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(title, location, time) },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = BatGold, contentColor = BatBlack)
            ) {
                Text("SCHEDULE", style = MaterialTheme.typography.labelLarge)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCEL", color = TextSecondary) }
        }
    )
}

@Composable
fun WayneBarChart(
    data:     List<Pair<String, Float>>,
    barColor: Color,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    // Animate progress from 0→1 on first composition
    var started by remember { mutableStateOf(false) }
    val animProgress by animateFloatAsState(
        targetValue   = if (started) 1f else 0f,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label         = "barGrow"
    )
    LaunchedEffect(Unit) { started = true }

    val maxVal = data.maxOfOrNull { it.second } ?: 1f

    Canvas(modifier = modifier) {
        val barCount = data.size
        val totalGap = size.width * 0.10f
        val barW     = (size.width - totalGap) / barCount - (totalGap / barCount)
        val gap      = totalGap / (barCount + 1)
        val labelH   = 22f
        val chartH   = size.height - labelH

        data.forEachIndexed { idx, (label, value) ->
            val x       = gap + idx * (barW + gap)
            val barH    = (value / maxVal) * chartH * animProgress
            val top     = chartH - barH

            // Bar shadow
            drawRect(
                color   = barColor.copy(alpha = 0.12f),
                topLeft = Offset(x + 3f, top + 3f),
                size    = Size(barW, barH)
            )
            // Bar fill (gradient)
            drawRect(
                brush   = Brush.verticalGradient(
                    colors = listOf(barColor, barColor.copy(alpha = 0.5f)),
                    startY = top,
                    endY   = chartH
                ),
                topLeft = Offset(x, top),
                size    = Size(barW, barH)
            )
            // Bar top highlight
            drawRect(
                color   = Color.White.copy(alpha = 0.12f),
                topLeft = Offset(x, top),
                size    = Size(barW, 3f)
            )
            // Value label above bar
            if (animProgress > 0.7f) {
                val valueStr = "%.1f".format(value)
                val measured = textMeasurer.measure(valueStr, TextStyle(color = barColor, fontSize = 8.sp, fontWeight = FontWeight.Bold))
                drawText(
                    textMeasurer = textMeasurer,
                    text         = valueStr,
                    topLeft      = Offset(x + barW / 2f - measured.size.width / 2f, top - measured.size.height - 2f),
                    style        = TextStyle(color = barColor, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                )
            }
            // Label below
            val labelMeasured = textMeasurer.measure(label, TextStyle(color = TextSecondary, fontSize = 7.sp))
            drawText(
                textMeasurer = textMeasurer,
                text         = label,
                topLeft      = Offset(x + barW / 2f - labelMeasured.size.width / 2f, chartH + 4f),
                style        = TextStyle(color = TextSecondary, fontSize = 7.sp)
            )
        }

        // Baseline
        drawLine(
            color       = TextDisabled.copy(alpha = 0.5f),
            start       = Offset(0f, chartH),
            end         = Offset(size.width, chartH),
            strokeWidth = 1f
        )
    }
}
