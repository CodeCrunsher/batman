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
import androidx.compose.ui.unit.*
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
    val infiniteTransition = rememberInfiniteTransition(label = "wayne")
    val stockValue by infiniteTransition.animateFloat(
        initialValue = 248.50f, targetValue = 249.80f,
        animationSpec = infiniteRepeatable(tween(3000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "stock"
    )

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
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Stock ticker card
            item {
                GlassCard(Modifier.fillMaxWidth(), borderColor = BatGreen.copy(0.4f)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("WNE", style = MaterialTheme.typography.labelLarge)
                            Text("Wayne Enterprises Inc.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("\$%.2f".format(stockValue), style = MaterialTheme.typography.displaySmall, color = BatGreen)
                            Text("▲ +1.24 (+0.50%)", style = MaterialTheme.typography.bodySmall, color = BatGreen)
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
            // Stats row
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    GlassCard(Modifier.weight(1f)) {
                        Text("\$4.2B", style = MaterialTheme.typography.displaySmall, color = BatGold)
                        Text("REVENUE", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    GlassCard(Modifier.weight(1f)) {
                        Text("12,400", style = MaterialTheme.typography.displaySmall, color = BatCyan)
                        Text("EMPLOYEES", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    GlassCard(Modifier.weight(1f)) {
                        Text("6", style = MaterialTheme.typography.displaySmall, color = BatGreen)
                        Text("DIVISIONS", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            }
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
            item { SectionHeader("LUCIUS FOX — TECH PIPELINE", Modifier.fillMaxWidth()) }
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
            item {
                GlassCard(Modifier.fillMaxWidth(), borderColor = BatOrange.copy(0.3f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Event, null, tint = BatOrange, modifier = Modifier.size(20.dp))
                        Column {
                            Text("BOARD MEETING", style = MaterialTheme.typography.titleLarge, color = BatOrange)
                            Text("Quarterly review — Wayne Tower Boardroom", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Text("Tomorrow, 09:00 AM", style = MaterialTheme.typography.bodySmall, color = TextDisabled)
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}
