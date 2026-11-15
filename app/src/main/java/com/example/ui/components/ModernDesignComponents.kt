package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.PolishAmberBg
import com.example.ui.theme.PolishAmberText
import com.example.ui.theme.PolishIndigoBg
import com.example.ui.theme.PolishIndigoText
import com.example.ui.theme.PolishNavy
import com.example.ui.theme.PolishTeal
import com.example.ui.theme.PolishTealBg
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate800

enum class MascotState {
    IDLE,
    LISTENING,
    ENCOURAGING,
    CELEBRATING
}

@Composable
fun MascotCoach(
    state: MascotState,
    audioLevel: Float = 0f,
    coachMessage: String? = null,
    size: Dp = 72.dp,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mascotFloat")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -2.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mascotBob"
    )
    val levelScale by animateFloatAsState(
        targetValue = if (state == MascotState.LISTENING) 1f + (audioLevel.coerceIn(0f, 1f) * 0.14f) else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "mascotScale"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (!coachMessage.isNullOrBlank()) {
            val pillBg = when (state) {
                MascotState.LISTENING -> PolishIndigoBg
                MascotState.CELEBRATING -> Color(0xFFDCFCE7)
                MascotState.ENCOURAGING -> PolishTealBg
                MascotState.IDLE -> PolishTealBg
            }
            val pillFg = when (state) {
                MascotState.LISTENING -> PolishIndigoText
                MascotState.CELEBRATING -> Color(0xFF047857)
                MascotState.ENCOURAGING -> PolishTeal
                MascotState.IDLE -> PolishTeal
            }
            Surface(
                color = pillBg,
                shape = RoundedCornerShape(999.dp)
            ) {
                Text(
                    text = coachMessage,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = pillFg
                    ),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        Canvas(
            modifier = Modifier
                .size(size)
                .graphicsLayer {
                    translationY = floatOffset
                    scaleX = levelScale
                    scaleY = levelScale
                }
                .semantics {
                    contentDescription = coachMessage ?: "Speaking coach mascot"
                }
        ) {
            val w = this.size.width
            val h = this.size.height
            val cx = w / 2f
            val cy = h / 2f
            val headR = w * 0.34f

            // Headphone band arc
            drawArc(
                color = PolishNavy,
                startAngle = 190f,
                sweepAngle = 160f,
                useCenter = false,
                topLeft = Offset(cx - headR * 1.16f, cy - headR * 1.18f),
                size = Size(headR * 2.32f, headR * 2.2f),
                style = Stroke(width = w * 0.08f, cap = StrokeCap.Round)
            )

            // Ear cups
            val cupW = w * 0.14f
            val cupH = h * 0.30f
            drawRoundRect(
                color = Color(0xFF6366F1),
                topLeft = Offset(cx - headR * 1.24f, cy - cupH * 0.45f),
                size = Size(cupW, cupH),
                cornerRadius = CornerRadius(cupW / 2f, cupW / 2f)
            )
            drawRoundRect(
                color = Color(0xFF6366F1),
                topLeft = Offset(cx + headR * 1.24f - cupW, cy - cupH * 0.45f),
                size = Size(cupW, cupH),
                cornerRadius = CornerRadius(cupW / 2f, cupW / 2f)
            )

            // Orb head
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF5EEAD4), PolishTeal, Color(0xFF0F766E)),
                    center = Offset(cx - headR * 0.3f, cy - headR * 0.35f),
                    radius = headR * 1.5f
                ),
                radius = headR,
                center = Offset(cx, cy)
            )

            // Specular highlight
            drawCircle(
                color = Color.White.copy(alpha = 0.38f),
                radius = headR * 0.2f,
                center = Offset(cx - headR * 0.35f, cy - headR * 0.4f)
            )

            // Blush cheeks
            val cheekColor = Color(0xFFFDA4AF).copy(alpha = 0.65f)
            drawCircle(
                color = cheekColor,
                radius = headR * 0.14f,
                center = Offset(cx - headR * 0.52f, cy + headR * 0.15f)
            )
            drawCircle(
                color = cheekColor,
                radius = headR * 0.14f,
                center = Offset(cx + headR * 0.52f, cy + headR * 0.15f)
            )

            // Eyes & mouth per state
            val eyeY = cy - headR * 0.12f
            val eyeDx = headR * 0.34f
            when (state) {
                MascotState.CELEBRATING -> {
                    drawArc(
                        color = PolishNavy,
                        startAngle = 200f,
                        sweepAngle = 140f,
                        useCenter = false,
                        topLeft = Offset(cx - eyeDx - headR * 0.14f, eyeY - headR * 0.08f),
                        size = Size(headR * 0.28f, headR * 0.18f),
                        style = Stroke(width = w * 0.045f, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = PolishNavy,
                        startAngle = 200f,
                        sweepAngle = 140f,
                        useCenter = false,
                        topLeft = Offset(cx + eyeDx - headR * 0.14f, eyeY - headR * 0.08f),
                        size = Size(headR * 0.28f, headR * 0.18f),
                        style = Stroke(width = w * 0.045f, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = PolishNavy,
                        startAngle = 15f,
                        sweepAngle = 150f,
                        useCenter = false,
                        topLeft = Offset(cx - headR * 0.28f, cy + headR * 0.1f),
                        size = Size(headR * 0.56f, headR * 0.32f),
                        style = Stroke(width = w * 0.05f, cap = StrokeCap.Round)
                    )
                }
                MascotState.ENCOURAGING -> {
                    drawCircle(color = PolishNavy, radius = headR * 0.1f, center = Offset(cx - eyeDx, eyeY))
                    drawArc(
                        color = PolishNavy,
                        startAngle = 200f,
                        sweepAngle = 140f,
                        useCenter = false,
                        topLeft = Offset(cx + eyeDx - headR * 0.12f, eyeY - headR * 0.06f),
                        size = Size(headR * 0.24f, headR * 0.14f),
                        style = Stroke(width = w * 0.045f, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = PolishNavy,
                        startAngle = 20f,
                        sweepAngle = 140f,
                        useCenter = false,
                        topLeft = Offset(cx - headR * 0.22f, cy + headR * 0.12f),
                        size = Size(headR * 0.44f, headR * 0.24f),
                        style = Stroke(width = w * 0.045f, cap = StrokeCap.Round)
                    )
                }
                MascotState.LISTENING -> {
                    drawCircle(color = PolishNavy, radius = headR * 0.11f, center = Offset(cx - eyeDx, eyeY))
                    drawCircle(color = PolishNavy, radius = headR * 0.11f, center = Offset(cx + eyeDx, eyeY))
                    drawCircle(
                        color = PolishNavy,
                        radius = headR * (0.12f + audioLevel.coerceIn(0f, 1f) * 0.08f),
                        center = Offset(cx, cy + headR * 0.28f)
                    )
                }
                MascotState.IDLE -> {
                    drawCircle(color = PolishNavy, radius = headR * 0.10f, center = Offset(cx - eyeDx, eyeY))
                    drawCircle(color = PolishNavy, radius = headR * 0.10f, center = Offset(cx + eyeDx, eyeY))
                    drawArc(
                        color = PolishNavy,
                        startAngle = 20f,
                        sweepAngle = 140f,
                        useCenter = false,
                        topLeft = Offset(cx - headR * 0.20f, cy + headR * 0.10f),
                        size = Size(headR * 0.40f, headR * 0.22f),
                        style = Stroke(width = w * 0.042f, cap = StrokeCap.Round)
                    )
                }
            }
        }
    }
}

@Composable
fun SonicVoiceOrb(
    isRecording: Boolean,
    audioLevel: Float,
    isProcessing: Boolean,
    onClick: () -> Unit,
    size: Dp = 64.dp,
    coreColor: Color = PolishTeal,
    glowColor: Color = Color(0xFF2DD4BF),
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orbPulse")
    val idlePulse by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orbIdlePulse"
    )

    val reactiveScale by animateFloatAsState(
        targetValue = if (isRecording) {
            1.10f + (audioLevel.coerceIn(0f, 1f) * 0.35f)
        } else {
            idlePulse
        },
        animationSpec = tween(durationMillis = 90),
        label = "orbReactiveScale"
    )

    val orbDiameter = if (size < 48.dp) 48.dp else size

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(orbDiameter + 20.dp)
    ) {
        Box(
            modifier = Modifier
                .size(orbDiameter + 12.dp)
                .scale(reactiveScale)
                .clip(CircleShape)
                .background(
                    glowColor.copy(
                        alpha = if (isRecording) (0.28f + audioLevel.coerceIn(0f, 1f) * 0.25f) else 0.22f
                    )
                )
        )

        Surface(
            onClick = onClick,
            enabled = !isProcessing,
            shape = CircleShape,
            color = coreColor,
            shadowElevation = 6.dp,
            modifier = Modifier
                .size(orbDiameter)
                .semantics {
                    contentDescription = when {
                        isProcessing -> "Processing speech"
                        isRecording -> "Stop recording"
                        else -> "Start recording"
                    }
                }
        ) {
            Box(contentAlignment = Alignment.Center) {
                when {
                    isProcessing -> {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(orbDiameter * 0.42f)
                        )
                    }
                    isRecording -> {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop recording",
                            tint = Color.White,
                            modifier = Modifier.size(orbDiameter * 0.44f)
                        )
                    }
                    else -> {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Start recording",
                            tint = Color.White,
                            modifier = Modifier.size(orbDiameter * 0.44f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Trophy3DBadge(
    title: String,
    xpReward: Int,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFFFFBEB),
        border = BorderStroke(1.dp, Color(0xFFFDE68A)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(50.dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFFBBF24), Color(0xFFD97706))
                        )
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF92400E),
                        letterSpacing = 0.6.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate800)
                )
            }

            Surface(
                color = PolishAmberBg,
                shape = RoundedCornerShape(999.dp)
            ) {
                Text(
                    text = "+$xpReward XP",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = PolishAmberText
                    ),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun HolographicCard(
    maxTiltAngle: Float = 8f,
    cornerRadius: Dp = 24.dp,
    containerColor: Color = Color.White,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "holoTilt")
    val gentleFloat by infiniteTransition.animateFloat(
        initialValue = -maxTiltAngle * 0.15f,
        targetValue = maxTiltAngle * 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "holoTiltAngle"
    )

    Card(
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = modifier.graphicsLayer {
            rotationY = gentleFloat
            cameraDistance = 12f * density
        }
    ) {
        content()
    }
}

private data class WarmupBubbleItem(
    val soundLabel: String,
    val exampleWord: String,
    val tip: String
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SpeechBubblePopDialog(
    onSpeakWord: (String) -> Unit,
    onClose: () -> Unit
) {
    val bubbles = remember {
        listOf(
            WarmupBubbleItem("TH", "Think", "Tip of tongue lightly between front teeth"),
            WarmupBubbleItem("SH", "Shine", "Round lips gently forward with soft airflow"),
            WarmupBubbleItem("V", "Voice", "Upper teeth gently touch lower lip with vibration"),
            WarmupBubbleItem("R", "Really", "Curl tongue back slightly without touching roof"),
            WarmupBubbleItem("W", "Water", "Tighten lips into a small circle then open"),
            WarmupBubbleItem("Z", "Zebra", "Teeth close together with voiced buzz")
        )
    }

    var poppedIndices by remember { mutableStateOf(emptySet<Int>()) }
    var lastItem by remember { mutableStateOf<WarmupBubbleItem?>(null) }

    Dialog(onDismissRequest = onClose) {
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("speech_bubble_warmup_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = PolishTeal
                        )
                        Text(
                            text = "Sound Warmup",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = PolishNavy
                            )
                        )
                    }
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close sound warmup",
                            tint = Slate500
                        )
                    }
                }

                Text(
                    text = "Tap any sound bubble to hear a crisp model word and warm up your articulation.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    bubbles.forEachIndexed { index, item ->
                        val popped = poppedIndices.contains(index)
                        Surface(
                            onClick = {
                                poppedIndices = poppedIndices + index
                                lastItem = item
                                onSpeakWord(item.exampleWord)
                            },
                            shape = RoundedCornerShape(18.dp),
                            color = if (popped) PolishTealBg else Slate100,
                            border = BorderStroke(
                                1.5.dp,
                                if (popped) PolishTeal else Color.Transparent
                            ),
                            modifier = Modifier.testTag("warmup_bubble_${item.soundLabel.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "/${item.soundLabel}/",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (popped) PolishTeal else PolishNavy
                                    )
                                )
                                Text(
                                    text = item.exampleWord,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Slate800,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Hear ${item.exampleWord}",
                                    tint = if (popped) PolishTeal else Slate500,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                lastItem?.let { current ->
                    Surface(
                        color = PolishIndigoBg,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "${current.soundLabel} — \"${current.exampleWord}\"",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PolishIndigoText
                                )
                            )
                            Text(
                                text = current.tip,
                                style = MaterialTheme.typography.bodySmall.copy(color = Slate800)
                            )
                        }
                    }
                }

                Button(
                    onClick = onClose,
                    colors = ButtonDefaults.buttonColors(containerColor = PolishTeal),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (poppedIndices.size == bubbles.size) "All Warmed Up!" else "Done (${poppedIndices.size}/${bubbles.size})",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
