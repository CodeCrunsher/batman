package com.batman.dashboard.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.batman.dashboard.ui.theme.*

/**
 * Glassmorphism card with optional gold border glow
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    borderColor: Color = GlassBorder,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.verticalGradient(
                    colors = listOf(BatSurfaceVar.copy(alpha = 0.9f), BatSurface.copy(alpha = 0.95f))
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(borderColor, borderColor.copy(alpha = 0.2f), borderColor)
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
            .padding(16.dp),
        content = content
    )
}

/**
 * Animated pulsing dot — used for status indicators and crime pins
 */
@Composable
fun PulseIndicator(
    color: Color,
    size: Dp = 12.dp,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "alpha"
    )
    Box(
        modifier = modifier
            .size(size)
            .drawBehind {
                // Outer glow ring
                drawCircle(color = color.copy(alpha = alpha * 0.4f), radius = size.toPx() * 0.9f)
                // Inner solid dot
                drawCircle(color = color, radius = size.toPx() * 0.4f)
            }
    )
}

/**
 * Threat level bar — horizontal fill bar with color gradient based on level
 */
@Composable
fun ThreatLevelBar(
    level: Float, // 0f-1f
    modifier: Modifier = Modifier,
    label: String = "THREAT LEVEL"
) {
    val color = when {
        level > 0.8f -> BatRed
        level > 0.6f -> BatOrange
        level > 0.4f -> BatGold
        else -> BatGreen
    }
    val animatedLevel by animateFloatAsState(targetValue = level, animationSpec = tween(800), label = "threat")

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text("${(level * 100).toInt()}%", style = MaterialTheme.typography.labelLarge, color = color)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(BatBorder)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedLevel)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(color.copy(alpha = 0.6f), color)
                        )
                    )
            )
        }
    }
}

/**
 * Battery/fuel level indicator
 */
@Composable
fun BatteryBar(
    level: Int,  // 0-100
    modifier: Modifier = Modifier,
    label: String = ""
) {
    val fraction = level / 100f
    val color = when {
        fraction < 0.2f -> BatRed
        fraction < 0.4f -> BatOrange
        else -> BatGreen
    }
    val animated by animateFloatAsState(targetValue = fraction, animationSpec = tween(600), label = "battery")

    Column(modifier = modifier) {
        if (label.isNotEmpty()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(label, style = MaterialTheme.typography.bodySmall)
                Text("$level%", style = MaterialTheme.typography.bodySmall, color = color)
            }
            Spacer(Modifier.height(3.dp))
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(BatBorder)
        ) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animated)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
        }
    }
}

/**
 * Section header with gold accent line
 */
@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(title, style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(4.dp))
        Box(
            Modifier
                .width(40.dp)
                .height(2.dp)
                .background(
                    Brush.horizontalGradient(colors = listOf(BatGold, BatGold.copy(alpha = 0f)))
                )
        )
    }
}

/**
 * Status chip badge
 */
@Composable
fun StatusChip(
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = color)
    }
}
