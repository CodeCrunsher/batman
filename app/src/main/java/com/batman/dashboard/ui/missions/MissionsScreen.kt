package com.batman.dashboard.ui.missions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import com.batman.dashboard.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissionsScreen(
    viewModel: MissionsViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = BatBlack,
        topBar = {
            TopAppBar(
                title = { Text("Missions", style = MaterialTheme.typography.headlineLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor        = BatBlack,
                    titleContentColor     = TextPrimary,
                    navigationIconContentColor = TextSecondary,
                ),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = viewModel::openAddDialog,
                containerColor = BatGold,
                contentColor   = BatBlack,
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add mission")
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            state.networkError?.let { error ->
                Surface(color = BatRedDark, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(error, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                        IconButton(
                            onClick = viewModel::clearNetworkError,
                            modifier = Modifier.size(20.dp),
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = TextSecondary)
                        }
                    }
                }
            }

            StatusFilterRow(
                selected  = state.filterStatus,
                onChange  = viewModel::setStatusFilter,
                modifier  = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )

            PriorityFilterRow(
                selected = state.filterPriority,
                onChange = viewModel::setPriorityFilter,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 0.dp),
            )

            HorizontalDivider(
                modifier  = Modifier.padding(top = 12.dp),
                thickness = 0.5.dp,
                color     = BatBorder,
            )

            if (state.missions.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint     = TextDisabled,
                            modifier = Modifier.size(40.dp),
                        )
                        Spacer(Modifier.height(12.dp))
                        Text("No missions", style = MaterialTheme.typography.titleLarge, color = TextSecondary)
                        Text("No items match the current filters.", style = MaterialTheme.typography.bodySmall, color = TextDisabled)
                    }
                }
            } else {
                LazyColumn {
                    items(state.missions, key = { it.id }) { mission ->
                        MissionRow(
                            mission      = mission,
                            onEdit       = { viewModel.openEditDialog(mission) },
                            onDelete     = { viewModel.deleteMission(mission) },
                            onStatusChange = { viewModel.updateMissionStatus(mission, it) },
                        )
                        HorizontalDivider(thickness = 0.5.dp, color = BatBorder)
                    }
                    item { Spacer(Modifier.height(88.dp)) }
                }
            }
        }
    }

    if (state.isAddDialogOpen) {
        MissionDialog(
            existing  = state.editingMission,
            onDismiss = viewModel::closeDialog,
            onSave    = viewModel::saveMission,
        )
    }
}

@Composable
private fun StatusFilterRow(
    selected: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier              = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        listOf("ALL", "PENDING", "IN_PROGRESS", "COMPLETED").forEach { status ->
            FilterChip(
                selected = selected == status,
                onClick  = { onChange(status) },
                label    = { Text(status.replace("_", " "), style = MaterialTheme.typography.labelMedium) },
                colors   = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = BatSurfaceVar,
                    selectedLabelColor     = TextPrimary,
                ),
            )
        }
    }
}

@Composable
private fun PriorityFilterRow(
    selected: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier              = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        listOf("ALL", "CRITICAL", "HIGH", "MEDIUM", "LOW").forEach { p ->
            FilterChip(
                selected = selected == p,
                onClick  = { onChange(p) },
                label    = { Text(p, style = MaterialTheme.typography.labelMedium) },
                colors   = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = BatSurfaceVar,
                    selectedLabelColor     = priorityColor(p),
                ),
            )
        }
    }
}

@Composable
private fun MissionRow(
    mission: MissionEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onStatusChange: (String) -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }
    val isCompleted = mission.status == "COMPLETED"
    val priorityAccent = priorityColor(mission.priority)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        // Priority dot
        Surface(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(8.dp),
            shape = androidx.compose.foundation.shape.CircleShape,
            color = if (isCompleted) TextDisabled else priorityAccent,
        ) {}

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text  = mission.title,
                style = MaterialTheme.typography.titleLarge.copy(
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else null,
                ),
                color    = if (isCompleted) TextDisabled else TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (mission.description.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text     = mission.description,
                    style    = MaterialTheme.typography.bodySmall,
                    color    = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text  = mission.priority,
                    style = MaterialTheme.typography.labelSmall,
                    color = priorityAccent,
                )
                Text("·", style = MaterialTheme.typography.labelSmall, color = TextDisabled)
                Text(
                    text  = mission.status.replace("_", " "),
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor(mission.status),
                )
                mission.dueDate?.let { due ->
                    val fmt = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(due))
                    val overdue = due < System.currentTimeMillis() && !isCompleted
                    Text("·", style = MaterialTheme.typography.labelSmall, color = TextDisabled)
                    Text(
                        text  = fmt,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (overdue) BatRed else TextDisabled,
                    )
                }
            }
        }

        Box {
            IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "Options",
                    tint     = TextDisabled,
                    modifier = Modifier.size(18.dp),
                )
            }
            DropdownMenu(
                expanded          = showMenu,
                onDismissRequest  = { showMenu = false },
                containerColor    = BatSurfaceVar,
            ) {
                DropdownMenuItem(
                    text    = { Text("Edit", style = MaterialTheme.typography.bodyMedium) },
                    onClick = { showMenu = false; onEdit() },
                )
                DropdownMenuItem(
                    text    = { Text("Mark complete", style = MaterialTheme.typography.bodyMedium) },
                    onClick = { showMenu = false; onStatusChange("COMPLETED") },
                )
                DropdownMenuItem(
                    text    = { Text("Mark in progress", style = MaterialTheme.typography.bodyMedium) },
                    onClick = { showMenu = false; onStatusChange("IN_PROGRESS") },
                )
                HorizontalDivider(color = BatBorder)
                DropdownMenuItem(
                    text    = { Text("Delete", style = MaterialTheme.typography.bodyMedium, color = BatRed) },
                    onClick = { showMenu = false; onDelete() },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MissionDialog(
    existing: MissionEntity?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, Long?) -> Unit,
) {
    var title       by remember { mutableStateOf(existing?.title       ?: "") }
    var description by remember { mutableStateOf(existing?.description ?: "") }
    var priority    by remember { mutableStateOf(existing?.priority    ?: "MEDIUM") }
    var status      by remember { mutableStateOf(existing?.status      ?: "PENDING") }
    var category    by remember { mutableStateOf(existing?.category    ?: "COMBAT") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = BatSurfaceVar,
        title = {
            Text(
                if (existing != null) "Edit mission" else "New mission",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value         = title,
                    onValueChange = { title = it },
                    label         = { Text("Title") },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true,
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = BatGold,
                        unfocusedBorderColor = BatBorder,
                        focusedTextColor     = TextPrimary,
                        unfocusedTextColor   = TextPrimary,
                        focusedLabelColor    = BatGold,
                        unfocusedLabelColor  = TextSecondary,
                    ),
                )
                OutlinedTextField(
                    value         = description,
                    onValueChange = { description = it },
                    label         = { Text("Description") },
                    modifier      = Modifier.fillMaxWidth(),
                    minLines      = 2,
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = BatGold,
                        unfocusedBorderColor = BatBorder,
                        focusedTextColor     = TextPrimary,
                        unfocusedTextColor   = TextPrimary,
                        focusedLabelColor    = BatGold,
                        unfocusedLabelColor  = TextSecondary,
                    ),
                )
                Text("Priority", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("CRITICAL", "HIGH", "MEDIUM", "LOW").forEach { p ->
                        FilterChip(
                            selected = priority == p,
                            onClick  = { priority = p },
                            label    = { Text(p, style = MaterialTheme.typography.labelMedium) },
                            colors   = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BatSurfaceVar,
                                selectedLabelColor     = priorityColor(p),
                            ),
                        )
                    }
                }
                Text("Category", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
                Row(
                    modifier              = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    listOf("RECON", "COMBAT", "INFILTRATION", "INVESTIGATION").forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick  = { category = cat },
                            label    = { Text(cat, style = MaterialTheme.typography.labelMedium) },
                            colors   = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BatSurfaceVar,
                                selectedLabelColor     = TextPrimary,
                            ),
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick  = { if (title.isNotBlank()) onSave(title, description, priority, status, category, null) },
                enabled  = title.isNotBlank(),
            ) {
                Text(
                    if (existing != null) "Save" else "Add",
                    style = MaterialTheme.typography.labelLarge,
                    color = BatGold,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
            }
        },
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
