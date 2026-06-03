package com.batman.dashboard.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.batman.dashboard.ui.theme.*

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    borderColor: Color = GlassBorder,
    content: @Composable ColumnScope.() -> Unit,
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
                shape = RoundedCornerShape(cornerRadius),
            )
            .padding(16.dp),
        content = content,
    )
}

@Composable
fun PulseIndicator(
    color: Color,
    size: Dp = 12.dp,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue  = 0.2f,
        animationSpec = infiniteRepeatable(
            animation  = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "alpha",
    )
    Box(
        modifier = modifier
            .size(size)
            .drawBehind {
                drawCircle(color = color.copy(alpha = alpha * 0.4f), radius = size.toPx() * 0.9f)
                drawCircle(color = color,                            radius = size.toPx() * 0.4f)
            }
    )
}

@Composable
fun ThreatLevelBar(
    level: Float,
    modifier: Modifier = Modifier,
    label: String = "THREAT LEVEL",
) {
    val color = when {
        level > 0.8f -> BatRed
        level > 0.6f -> BatOrange
        level > 0.4f -> BatGold
        else         -> BatGreen
    }
    val animatedLevel by animateFloatAsState(
        targetValue   = level,
        animationSpec = tween(800),
        label         = "threat",
    )
    Column(modifier = modifier) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text("${(level * 100).toInt()}%", style = MaterialTheme.typography.labelLarge, color = color)
        }
        Spacer(Modifier.height(4.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(BatBorder)
        ) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedLevel)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Brush.horizontalGradient(listOf(color.copy(alpha = 0.6f), color)))
            )
        }
    }
}

@Composable
fun BatteryBar(
    level: Int,
    modifier: Modifier = Modifier,
    label: String = "",
) {
    val fraction = level / 100f
    val color = when {
        fraction < 0.2f -> BatRed
        fraction < 0.4f -> BatOrange
        else            -> BatGreen
    }
    val animated by animateFloatAsState(
        targetValue   = fraction,
        animationSpec = tween(600),
        label         = "battery",
    )
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

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(title, style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(4.dp))
        Box(
            Modifier
                .width(40.dp)
                .height(2.dp)
                .background(Brush.horizontalGradient(listOf(BatGold, BatGold.copy(alpha = 0f))))
        )
    }
}

@Composable
fun StatusChip(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = color)
    }
}
