package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ModelDownloadState
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun DependencyDownloadBanner(
    modelState: ModelDownloadState,
    speechStatusNotice: String?,
    onRetryDownload: () -> Unit,
    onDismissNotice: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showReadyConfirmation by remember { mutableStateOf(false) }
    var wasNotReady by remember { mutableStateOf(false) }

    LaunchedEffect(modelState) {
        if (modelState !is ModelDownloadState.Ready) {
            wasNotReady = true
            showReadyConfirmation = false
        } else if (wasNotReady) {
            showReadyConfirmation = true
            delay(2500)
            showReadyConfirmation = false
            wasNotReady = false
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AnimatedVisibility(
            visible = modelState !is ModelDownloadState.Ready || showReadyConfirmation,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            when (modelState) {
                is ModelDownloadState.Checking -> {
                    VoiceSetupStatusCard(
                        badge = "VOICE STUDIO SETUP",
                        title = "Preparing Voice Studio...",
                        subtitle = "Checking your English voice coach",
                        progress = null,
                        isError = false,
                        isSuccess = false,
                        onRetry = null
                    )
                }
                is ModelDownloadState.Downloading -> {
                    val percent = (modelState.progress * 100).toInt().coerceIn(0, 100)
                    VoiceSetupStatusCard(
                        badge = "SETTING UP VOICE COACH • $percent%",
                        title = "Preparing English voice coach",
                        subtitle = "${formatMegabytes(modelState.downloadedBytes)} / ${formatMegabytes(modelState.totalBytes)} • One-time setup for instant speaking feedback",
                        progress = modelState.progress,
                        isError = false,
                        isSuccess = false,
                        onRetry = null
                    )
                }
                is ModelDownloadState.Extracting -> {
                    val percent = (modelState.progress * 100).toInt().coerceIn(0, 100)
                    VoiceSetupStatusCard(
                        badge = "OPTIMIZING • $percent%",
                        title = "Configuring English speech clarity...",
                        subtitle = "Preparing speech analysis and pronunciation scoring",
                        progress = modelState.progress,
                        isError = false,
                        isSuccess = false,
                        isConfiguring = true,
                        onRetry = null
                    )
                }
                is ModelDownloadState.Loading -> {
                    VoiceSetupStatusCard(
                        badge = "INITIALIZING COACH",
                        title = "Warming up English speech recognition...",
                        subtitle = "Almost ready for your speaking workout",
                        progress = null,
                        isError = false,
                        isSuccess = false,
                        onRetry = null
                    )
                }
                is ModelDownloadState.Error -> {
                    VoiceSetupStatusCard(
                        badge = "SETUP PAUSED",
                        title = "Voice setup needs attention",
                        subtitle = modelState.message,
                        progress = null,
                        isError = true,
                        isSuccess = false,
                        onRetry = onRetryDownload
                    )
                }
                is ModelDownloadState.Ready -> {
                    VoiceSetupStatusCard(
                        badge = "VOICE STUDIO READY",
                        title = "English voice coach is ready",
                        subtitle = "Real-time pronunciation & clarity scoring active",
                        progress = 1f,
                        isError = false,
                        isSuccess = true,
                        onRetry = null
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = !speechStatusNotice.isNullOrBlank(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            speechStatusNotice?.let { notice ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Orange50),
                    border = BorderStroke(1.dp, Orange100),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("speech_status_notice")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Notice",
                            tint = Orange600,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = notice,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = PolishNavy,
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = onDismissNotice,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss notice",
                                tint = Slate500,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VoiceSetupStatusCard(
    badge: String,
    title: String,
    subtitle: String,
    progress: Float?,
    isError: Boolean,
    isSuccess: Boolean,
    isConfiguring: Boolean = false,
    onRetry: (() -> Unit)?
) {
    val containerColor = when {
        isError -> Red50
        isSuccess -> PolishTealBg
        else -> PolishTealLight
    }
    val borderColor = when {
        isError -> Red100
        else -> PolishTealLightBorder
    }
    val accentColor = when {
        isError -> Red700
        isSuccess -> PolishTealText
        else -> PolishTeal
    }
    val icon = when {
        isError -> Icons.Default.ErrorOutline
        isSuccess -> Icons.Default.CheckCircle
        isConfiguring -> Icons.Default.Tune
        else -> Icons.Default.GraphicEq
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dependency_download_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = accentColor,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isError) Red900 else PolishNavy
                        )
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (isError) Red700 else Slate500
                        )
                    )
                }
            }

            if (!isError && !isSuccess) {
                if (progress != null) {
                    LinearProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = accentColor,
                        trackColor = Color.White.copy(alpha = 0.7f)
                    )
                } else {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = accentColor,
                        trackColor = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            if (isError && onRetry != null) {
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(containerColor = Red700),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("retry_model_download_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Retry setup",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Retry Setup",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun formatMegabytes(bytes: Long): String {
    val mb = bytes.toDouble() / (1024.0 * 1024.0)
    return String.format(Locale.US, "%.1f MB", mb)
}
