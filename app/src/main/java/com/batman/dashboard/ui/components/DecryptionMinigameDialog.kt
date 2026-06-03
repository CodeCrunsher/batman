package com.batman.dashboard.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import com.batman.dashboard.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DecryptionMinigameDialog(
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val hexPool = remember {
        listOf("A4","FF","3C","7E","B1","0D","9F","22","C6","5A","E8","12","D3","6B","4F","88")
    }
    val solution = remember { hexPool.shuffled().take(4) }
    val options  = remember { (solution + hexPool.filterNot { it in solution }.shuffled().take(4)).shuffled() }

    var selected      by remember { mutableStateOf<List<String>>(emptyList()) }
    var shakeState    by remember { mutableStateOf(false) }
    var errorFlash    by remember { mutableStateOf(false) }
    var success       by remember { mutableStateOf(false) }
    var attempts      by remember { mutableStateOf(0) }
    val scope         = rememberCoroutineScope()

    val shakeOffset by animateFloatAsState(
        targetValue = if (shakeState) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy),
        label = "shake",
        finishedListener = { shakeState = false }
    )

    val infiniteAnim = rememberInfiniteTransition(label = "lock")
    val lockAlpha by infiniteAnim.animateFloat(
        initialValue = 0.6f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "lockAlpha"
    )

    Dialog(onDismissRequest = { if (!success) onDismiss() }) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .offset(x = if (shakeState) (shakeOffset * 12f - 6f).dp else 0.dp),
            borderColor = when {
                success    -> BatGreen.copy(alpha = 0.8f)
                errorFlash -> BatRed.copy(alpha = 0.9f)
                else       -> BatGold.copy(alpha = 0.5f)
            }
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = BatGold.copy(alpha = lockAlpha),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    "Cipher Bypass",
                    style = MaterialTheme.typography.titleLarge,
                    color = BatGold,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                "Select the 4 correct hex sequences in order",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Spacer(Modifier.height(16.dp))

            Text("Target sequence:", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                solution.forEachIndexed { idx, code ->
                    val isMatched = selected.getOrNull(idx) == code
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(
                                if (isMatched) BatGreen.copy(alpha = 0.2f) else GlassWhite,
                                RoundedCornerShape(8.dp)
                            )
                            .border(
                                1.dp,
                                if (isMatched) BatGreen else BatBorder,
                                RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text  = if (isMatched) code else "?",
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = FontFamily.Monospace,
                            color = if (isMatched) BatGreen else TextDisabled,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Text("Your input:", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(4) { idx ->
                    val chip = selected.getOrNull(idx)
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(
                                if (chip != null) BatGold.copy(alpha = 0.1f) else GlassWhite,
                                RoundedCornerShape(8.dp)
                            )
                            .border(
                                1.dp,
                                if (chip != null) BatGold.copy(alpha = 0.6f) else BatBorder,
                                RoundedCornerShape(8.dp)
                            )
                            .then(if (chip != null) Modifier.clickable {
                                selected = selected.toMutableList().also { it.removeAt(idx) }
                            } else Modifier),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text  = chip ?: "—",
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = FontFamily.Monospace,
                            color = if (chip != null) BatGold else TextDisabled,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Text("Options:", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Spacer(Modifier.height(8.dp))

            val chunked = options.chunked(4)
            chunked.forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    row.forEach { chip ->
                        val isUsed = chip in selected
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .background(
                                    if (isUsed) BatSurfaceVar else GlassWhite,
                                    RoundedCornerShape(8.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isUsed) BatBorder else BatCyan.copy(alpha = 0.4f),
                                    RoundedCornerShape(8.dp)
                                )
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(enabled = !isUsed && !success && selected.size < 4) {
                                    selected = selected + chip
                                    if (selected.size == 4) {
                                        scope.launch {
                                            if (selected == solution) {
                                                success = true
                                                delay(1200)
                                                onSuccess()
                                            } else {
                                                errorFlash = true
                                                shakeState = true
                                                attempts++
                                                delay(600)
                                                selected = emptyList()
                                                errorFlash = false
                                            }
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text  = chip,
                                style = MaterialTheme.typography.labelLarge,
                                fontFamily = FontFamily.Monospace,
                                color = if (isUsed) TextDisabled else BatCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            if (attempts > 0 && !success) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "✕  Incorrect sequence — $attempts attempt${if (attempts > 1) "s" else ""} made",
                    style = MaterialTheme.typography.bodySmall,
                    color = BatRed,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            AnimatedVisibility(visible = success, enter = fadeIn() + scaleIn()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = BatGreen,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("Access granted", style = MaterialTheme.typography.titleLarge, color = BatGreen, fontWeight = FontWeight.Bold)
                    Text("Cipher bypassed successfully", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }

            Spacer(Modifier.height(12.dp))

            if (!success) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End),
                    border = BorderStroke(1.dp, BatBorder)
                ) {
                    Text("Cancel", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                }
            }
        }
    }
}
