package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.VoiceOverOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PolishIndigoBg
import com.example.ui.theme.PolishIndigoText
import com.example.ui.theme.TealPrimary

@Composable
fun SpokenVoiceToTextCard(
    isRecording: Boolean,
    recognizedText: String,
    hasEvaluated: Boolean,
    modifier: Modifier = Modifier
) {
    val isSpokenEmpty = recognizedText.trim().isEmpty()

    if (isRecording || hasEvaluated) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (isRecording) {
                        TealPrimary.copy(alpha = 0.08f)
                    } else if (isSpokenEmpty) {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    } else {
                        PolishIndigoBg.copy(alpha = 0.7f)
                    }
                )
                .border(
                    width = 1.dp,
                    color = if (isRecording) {
                        TealPrimary.copy(alpha = 0.35f)
                    } else if (isSpokenEmpty) {
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    } else {
                        TealPrimary.copy(alpha = 0.4f)
                    },
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isSpokenEmpty && !isRecording) {
                                Icons.Default.VoiceOverOff
                            } else {
                                Icons.Default.RecordVoiceOver
                            },
                            contentDescription = "Spoken voice transcript",
                            tint = if (isRecording) {
                                Color(0xFFFF5252)
                            } else if (isSpokenEmpty) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                PolishIndigoText
                            },
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (isRecording) "LIVE SPEECH CAPTURE:" else "YOU SPOKE:",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = if (isRecording) {
                                    Color(0xFFFF5252)
                                } else if (isSpokenEmpty) {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                } else {
                                    PolishIndigoText
                                }
                            )
                        )
                    }

                    if (isRecording) {
                        // Live Audio Wave Bars indicator
                        AnimatedWaveformBars()
                    }
                }

                if (isSpokenEmpty) {
                    Text(
                        text = if (isRecording) {
                            "Listening to your voice... Speak clearly now."
                        } else {
                            "No speech detected. Hold device closer to mic and speak clearly."
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                            fontStyle = FontStyle.Italic
                        )
                    )
                } else {
                    Text(
                        text = "“$recognizedText”",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 22.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun AnimatedWaveformBars() {
    val transition = rememberInfiniteTransition(label = "waveform_anim")
    
    val height1 by transition.animateFloat(
        initialValue = 6f,
        targetValue = 18f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar1"
    )
    val height2 by transition.animateFloat(
        initialValue = 16f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar2"
    )
    val height3 by transition.animateFloat(
        initialValue = 8f,
        targetValue = 22f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar3"
    )
    val height4 by transition.animateFloat(
        initialValue = 18f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(380, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar4"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        modifier = Modifier.height(24.dp)
    ) {
        listOf(height1, height2, height3, height4).forEach { heightVal ->
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(heightVal.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF5252))
            )
        }
    }
}
