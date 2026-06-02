package com.batman.dashboard.ui.music

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
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import com.batman.dashboard.ui.components.*
import com.batman.dashboard.ui.theme.*
import kotlin.math.*

data class Track(
    val title: String,
    val artist: String,
    val duration: String,
    val genre: String
)

val GOTHAM_PLAYLIST = listOf(
    Track("Shadows Over Gotham", "The Dark Orchestra", "4:23", "Cinematic"),
    Track("Night Patrol", "Bat Signal", "3:47", "Electronic"),
    Track("Vengeance Rising", "The Caped Crusaders", "5:12", "Orchestral"),
    Track("Crime Alley Blues", "Alfred & The Butlers", "3:58", "Jazz"),
    Track("Wayne Manor", "The Gotham Strings", "6:01", "Classical"),
    Track("Arkham Asylum", "Scarecrow's Fear", "4:44", "Industrial"),
    Track("The Dark Knight Returns", "DC Symphony", "7:22", "Orchestral"),
    Track("Batarang", "Robin Hood", "3:15", "Hip-Hop"),
    Track("The Joker's Waltz", "The Purple Gang", "4:08", "Dark Jazz"),
    Track("Flight of the Batwing", "Night Wing", "5:30", "Electronic"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicPlayerScreen(onBack: () -> Unit) {
    var currentTrackIndex by remember { mutableStateOf(0) }
    var isPlaying by remember { mutableStateOf(false) }
    var isShuffle by remember { mutableStateOf(false) }
    var isRepeat by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0.35f) }

    val currentTrack = GOTHAM_PLAYLIST[currentTrackIndex]

    val infiniteTransition = rememberInfiniteTransition(label = "music")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing)),
        label = "wave"
    )
    val albumPulse by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = if (isPlaying) 1.04f else 1f,
        animationSpec = infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "album"
    )

    fun nextTrack() { currentTrackIndex = (currentTrackIndex + 1) % GOTHAM_PLAYLIST.size }
    fun prevTrack() { currentTrackIndex = (currentTrackIndex - 1 + GOTHAM_PLAYLIST.size) % GOTHAM_PLAYLIST.size }

    Scaffold(
        containerColor = BatBlack,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("MUSIC PLAYER", style = MaterialTheme.typography.headlineLarge)
                        Text("GOTHAM NIGHTWATCH", style = MaterialTheme.typography.headlineSmall)
                    }
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = BatGold) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BatBlack)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))

            // Album art
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .graphicsLayer { scaleX = albumPulse; scaleY = albumPulse }
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.radialGradient(colors = listOf(BatPurple.copy(0.8f), BatBlack)))
                    .border(2.dp, BatGold.copy(0.5f), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    val cx = size.width / 2; val cy = size.height / 2
                    drawCircle(BatGold.copy(0.1f), size.minDimension * 0.4f)
                    drawCircle(BatGold.copy(0.06f), size.minDimension * 0.48f, style = Stroke(2f))
                    val path = Path().apply {
                        moveTo(cx, cy)
                        cubicTo(cx - 60f, cy - 40f, cx - 40f, cy - 65f, cx - 18f, cy - 22f)
                        cubicTo(cx - 12f, cy - 38f, cx, cy - 28f, cx, cy - 22f)
                        cubicTo(cx, cy - 28f, cx + 12f, cy - 38f, cx + 18f, cy - 22f)
                        cubicTo(cx + 40f, cy - 65f, cx + 60f, cy - 40f, cx, cy)
                        close()
                        moveTo(cx - 18f, cy - 22f); lineTo(cx - 22f, cy - 55f); lineTo(cx - 9f, cy - 32f); close()
                        moveTo(cx + 18f, cy - 22f); lineTo(cx + 22f, cy - 55f); lineTo(cx + 9f, cy - 32f); close()
                    }
                    drawPath(path, BatGold.copy(0.9f))
                }
                if (isPlaying) {
                    Canvas(Modifier.fillMaxWidth().height(40.dp).align(Alignment.BottomCenter)) {
                        val bars = 32
                        val barW = size.width / bars
                        for (i in 0 until bars) {
                            val amplitude = (sin(wavePhase.toDouble() + i * 0.4) * 0.5 + 0.5).toFloat()
                            val barH = amplitude * size.height
                            drawRect(
                                color = BatGold.copy(alpha = amplitude),
                                topLeft = Offset(i * barW + 1f, size.height - barH),
                                size = Size(barW - 2f, barH)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(currentTrack.title, style = MaterialTheme.typography.displaySmall, color = TextPrimary, textAlign = TextAlign.Center)
            Text(currentTrack.artist, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            Spacer(Modifier.height(4.dp))
            StatusChip(currentTrack.genre, BatPurple)
            Spacer(Modifier.height(16.dp))

            Slider(
                value = progress,
                onValueChange = { progress = it },
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(thumbColor = BatGold, activeTrackColor = BatGold, inactiveTrackColor = BatBorder)
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("1:28", style = MaterialTheme.typography.labelSmall, color = TextDisabled)
                Text(currentTrack.duration, style = MaterialTheme.typography.labelSmall, color = TextDisabled)
            }

            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { isShuffle = !isShuffle }) {
                    Icon(Icons.Default.Shuffle, null, tint = if (isShuffle) BatGold else TextDisabled, modifier = Modifier.size(24.dp))
                }
                IconButton(onClick = ::prevTrack, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Default.SkipPrevious, null, tint = TextPrimary, modifier = Modifier.size(32.dp))
                }
                Box(
                    modifier = Modifier.size(64.dp).background(BatGold, CircleShape).clickable { isPlaying = !isPlaying },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, null, tint = BatBlack, modifier = Modifier.size(36.dp))
                }
                IconButton(onClick = ::nextTrack, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Default.SkipNext, null, tint = TextPrimary, modifier = Modifier.size(32.dp))
                }
                IconButton(onClick = { isRepeat = !isRepeat }) {
                    Icon(Icons.Default.Repeat, null, tint = if (isRepeat) BatGold else TextDisabled, modifier = Modifier.size(24.dp))
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = BatBorder)
            Spacer(Modifier.height(8.dp))
            Text("PLAYLIST", style = MaterialTheme.typography.labelLarge, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(6.dp))

            GOTHAM_PLAYLIST.forEachIndexed { idx, track ->
                val isCurrentTrack = idx == currentTrackIndex
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isCurrentTrack) BatGold.copy(0.1f) else Color.Transparent)
                        .clickable { currentTrackIndex = idx; isPlaying = true }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${idx + 1}", style = MaterialTheme.typography.labelMedium, color = if (isCurrentTrack) BatGold else TextDisabled, modifier = Modifier.width(24.dp))
                    Column(Modifier.weight(1f)) {
                        Text(track.title, style = MaterialTheme.typography.bodyMedium, color = if (isCurrentTrack) TextPrimary else TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(track.artist, style = MaterialTheme.typography.bodySmall, color = TextDisabled)
                    }
                    Text(track.duration, style = MaterialTheme.typography.labelSmall, color = TextDisabled)
                    if (isCurrentTrack && isPlaying) {
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Default.Equalizer, null, tint = BatGold, modifier = Modifier.size(16.dp))
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
