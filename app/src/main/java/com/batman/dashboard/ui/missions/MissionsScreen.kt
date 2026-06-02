package com.batman.dashboard.ui.missions

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.batman.dashboard.data.db.MissionEntity
import com.batman.dashboard.ui.components.*
import com.batman.dashboard.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissionsScreen(
    viewModel: MissionsViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = BatBlack,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("MISSIONS", style = MaterialTheme.typography.headlineLarge)
                        Text("& ACTIVE QUESTS", style = MaterialTheme.typography.headlineSmall)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = BatGold)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.openAddDialog() }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Mission", tint = BatGold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BatBlack)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddDialog() },
                containerColor = BatGold,
                contentColor = BatBlack
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // ── Filters ──
            FilterRow(
                selectedStatus = state.filterStatus,
                onStatusChange = viewModel::setStatusFilter
            )
            Spacer(Modifier.height(8.dp))
            PriorityFilterRow(
                selectedPriority = state.filterPriority,
                onPriorityChange = viewModel::setPriorityFilter
            )
            Spacer(Modifier.height(12.dp))

            if (state.missions.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BatGreen, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("ALL CLEAR", style = MaterialTheme.typography.headlineMedium, color = BatGreen)
                        Text("No missions match filters", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(state.missions, key = { it.id }) { mission ->
                        MissionCard(
                            mission = mission,
                            onEdit = { viewModel.openEditDialog(mission) },
                            onDelete = { viewModel.deleteMission(mission) },
                            onStatusChange = { newStatus -> viewModel.updateMissionStatus(mission, newStatus) }
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }

    if (state.isAddDialogOpen) {
        AddEditMissionDialog(
            existingMission = state.editingMission,
            onDismiss = viewModel::closeDialog,
            onSave = viewModel::saveMission
        )
    }
}

@Composable
fun FilterRow(selectedStatus: String, onStatusChange: (String) -> Unit) {
    val statuses = listOf("ALL", "PENDING", "IN_PROGRESS", "COMPLETED")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
        statuses.forEach { status ->
            val selected = selectedStatus == status
            FilterChip(
                selected = selected,
                onClick = { onStatusChange(status) },
                label = { Text(status.replace("_", " "), style = MaterialTheme.typography.labelMedium) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = BatGold.copy(alpha = 0.2f),
                    selectedLabelColor = BatGold
                )
            )
        }
    }
}

@Composable
fun PriorityFilterRow(selectedPriority: String, onPriorityChange: (String) -> Unit) {
    val priorities = listOf("ALL", "CRITICAL", "HIGH", "MEDIUM", "LOW")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
        priorities.forEach { p ->
            val selected = selectedPriority == p
            val color = priorityColor(p)
            FilterChip(
                selected = selected,
                onClick = { onPriorityChange(p) },
                label = { Text(p, style = MaterialTheme.typography.labelMedium) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = color.copy(alpha = 0.2f),
                    selectedLabelColor = color
                )
            )
        }
    }
}

@Composable
fun MissionCard(
    mission: MissionEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onStatusChange: (String) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val pColor = priorityColor(mission.priority)
    val isCompleted = mission.status == "COMPLETED"

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = pColor.copy(alpha = 0.3f)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            // Priority strip
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(60.dp)
                    .background(pColor, RoundedCornerShape(2.dp))
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        mission.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            textDecoration = if (isCompleted) TextDecoration.LineThrough else null
                        ),
                        color = if (isCompleted) TextDisabled else TextPrimary,
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Box {
                        IconButton(onClick = { showMenu = true }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More", tint = TextSecondary, modifier = Modifier.size(16.dp))
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            containerColor = BatSurfaceVar
                        ) {
                            DropdownMenuItem(text = { Text("Edit", color = BatGold) }, onClick = { showMenu = false; onEdit() })
                            DropdownMenuItem(text = { Text("Mark Complete", color = BatGreen) }, onClick = { showMenu = false; onStatusChange("COMPLETED") })
                            DropdownMenuItem(text = { Text("Mark In Progress", color = BatCyan) }, onClick = { showMenu = false; onStatusChange("IN_PROGRESS") })
                            DropdownMenuItem(text = { Text("Delete", color = BatRed) }, onClick = { showMenu = false; onDelete() })
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    mission.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    StatusChip(mission.priority, pColor)
                    StatusChip(mission.status.replace("_", " "), statusColor(mission.status))
                    StatusChip(mission.category, BatCyan.copy(alpha = 0.7f))
                }
                mission.dueDate?.let { due ->
                    Spacer(Modifier.height(4.dp))
                    val dueFmt = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(Date(due))
                    val isOverdue = due < System.currentTimeMillis() && !isCompleted
                    Text(
                        "⏰ $dueFmt",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isOverdue) BatRed else TextDisabled
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMissionDialog(
    existingMission: MissionEntity?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, Long?) -> Unit
) {
    var title by remember { mutableStateOf(existingMission?.title ?: "") }
    var description by remember { mutableStateOf(existingMission?.description ?: "") }
    var priority by remember { mutableStateOf(existingMission?.priority ?: "MEDIUM") }
    var status by remember { mutableStateOf(existingMission?.status ?: "PENDING") }
    var category by remember { mutableStateOf(existingMission?.category ?: "COMBAT") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BatSurfaceVar,
        title = {
            Text(
                if (existingMission != null) "EDIT MISSION" else "NEW MISSION",
                style = MaterialTheme.typography.headlineLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Mission Title", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BatGold, unfocusedBorderColor = BatBorder, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BatGold, unfocusedBorderColor = BatBorder, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                )
                Text("PRIORITY", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("CRITICAL", "HIGH", "MEDIUM", "LOW").forEach { p ->
                        val sel = priority == p
                        val c = priorityColor(p)
                        FilterChip(
                            selected = sel,
                            onClick = { priority = p },
                            label = { Text(p, style = MaterialTheme.typography.labelMedium) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = c.copy(alpha = 0.25f),
                                selectedLabelColor = c
                            )
                        )
                    }
                }
                Text("CATEGORY", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    listOf("RECON", "COMBAT", "INFILTRATION", "INVESTIGATION").forEach { cat ->
                        val sel = category == cat
                        FilterChip(
                            selected = sel,
                            onClick = { category = cat },
                            label = { Text(cat, style = MaterialTheme.typography.labelMedium) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BatCyan.copy(alpha = 0.2f),
                                selectedLabelColor = BatCyan
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotBlank()) onSave(title, description, priority, status, category, null) },
                colors = ButtonDefaults.buttonColors(containerColor = BatGold, contentColor = BatBlack)
            ) {
                Text(if (existingMission != null) "UPDATE" else "DEPLOY", style = MaterialTheme.typography.labelLarge)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = TextSecondary)
            }
        }
    )
}

fun priorityColor(priority: String): Color = when (priority) {
    "CRITICAL" -> PriorityCritical
    "HIGH"     -> PriorityHigh
    "MEDIUM"   -> PriorityMedium
    "LOW"      -> PriorityLow
    else       -> TextSecondary
}

fun statusColor(status: String): Color = when (status) {
    "PENDING"     -> BatOrange
    "IN_PROGRESS" -> BatCyan
    "COMPLETED"   -> BatGreen
    else          -> TextSecondary
}
