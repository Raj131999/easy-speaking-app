package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.VoiceOverOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    isProcessing: Boolean,
    recognizedText: String,
    hasEvaluated: Boolean,
    modifier: Modifier = Modifier
) {
    // Show during active recording, processing, or after evaluation has completed
    val isSpokenEmpty = recognizedText.trim().isEmpty()

    if (isRecording || isProcessing || hasEvaluated) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (isRecording || isProcessing) {
                        TealPrimary.copy(alpha = 0.08f)
                    } else if (isSpokenEmpty) {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    } else {
                        PolishIndigoBg.copy(alpha = 0.7f)
                    }
                )
                .border(
                    width = 1.dp,
                    color = if (isRecording || isProcessing) {
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
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (isSpokenEmpty && !isRecording && !isProcessing) {
                                Icons.Default.VoiceOverOff
                            } else {
                                Icons.Default.RecordVoiceOver
                            },
                            contentDescription = "Spoken voice transcript",
                            tint = if (isRecording || isProcessing) {
                                Color(0xFFFF5252)
                            } else if (isSpokenEmpty) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                PolishIndigoText
                            },
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (isRecording || isProcessing) "LIVE SPEECH CONVERSION:" else "YOU SPOKE:",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = if (isRecording || isProcessing) {
                                    Color(0xFFFF5252)
                                } else if (isSpokenEmpty) {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                } else {
                                    PolishIndigoText
                                }
                            )
                        )
                    }

                    if (isRecording || isProcessing) {
                        Text(
                            text = if (isProcessing) "Finalizing..." else "Listening...",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFFFF5252),
                                fontStyle = FontStyle.Italic
                            )
                        )
                    }
                }

                if (isSpokenEmpty) {
                    Text(
                        text = if (isRecording) {
                            "Listening to your voice..."
                        } else if (isProcessing) {
                            "Processing your speech..."
                        } else {
                            "Nothing is spoken"
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
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
