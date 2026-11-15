package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import com.example.ui.EnglishViewModel
import com.example.ui.ModelDownloadState
import com.example.ui.SpeechModelOption
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: EnglishViewModel,
    modifier: Modifier = Modifier
) {
    val selectedAccent by viewModel.selectedAccent.collectAsState()
    val selectedSpeed by viewModel.selectedSpeed.collectAsState()
    val userProgress by viewModel.userProgress.collectAsState()
    val modelDownloadState by viewModel.modelDownloadState.collectAsState()
    val selectedSpeechModel by viewModel.selectedSpeechModel.collectAsState()
    val installedSpeechModels by viewModel.installedSpeechModels.collectAsState()
    val useGrammarMode by viewModel.useGrammarMode.collectAsState()

    val scrollState = rememberScrollState()
    var showResetDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        fontWeight = FontWeight.Bold,
                        color = PolishNavy
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.goBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PolishNavy
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PolishBg
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(PolishBg)
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // --- SECTION 1: SPEECH RECOGNITION ACCENT & CLARITY ---
            Text(
                text = "Voice Recognition & Accent",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = PolishTeal
                )
            )

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Slate100),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "SPEECH RECOGNITION VOICE PACK",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate500,
                            letterSpacing = 0.8.sp
                        )
                    )

                    SpeechModelOption.entries.forEach { option ->
                        val isSelected = selectedSpeechModel == option
                        val isInstalled = installedSpeechModels.contains(option.id)
                        val stateForOption = modelDownloadState

                        val badgeText = when {
                            isSelected && stateForOption is ModelDownloadState.Ready && stateForOption.option == option -> "Active"
                            isSelected && stateForOption is ModelDownloadState.Downloading && stateForOption.option == option ->
                                "Setting up ${(stateForOption.progress * 100).toInt()}%"
                            isSelected && stateForOption is ModelDownloadState.Extracting && stateForOption.option == option ->
                                "Optimizing ${(stateForOption.progress * 100).toInt()}%"
                            isSelected && stateForOption is ModelDownloadState.Loading && stateForOption.option == option ->
                                "Loading..."
                            isInstalled -> "Ready"
                            else -> "Tap to set up"
                        }

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) PolishTealLight else Slate50,
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) PolishTeal else Slate200
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.selectSpeechModel(option) }
                                .testTag("speech_model_option_${option.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { viewModel.selectSpeechModel(option) },
                                    colors = RadioButtonDefaults.colors(selectedColor = PolishTeal)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = option.displayName,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = PolishNavy
                                        )
                                    )
                                    Text(
                                        text = option.subtitle,
                                        style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected || isInstalled) PolishTealBg else Color.White,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected || isInstalled) PolishTealLightBorder else Slate200
                                    )
                                ) {
                                    Text(
                                        text = badgeText,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected || isInstalled) PolishTealText else Slate500
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = Slate100)

                    // Grammar-Guided Accuracy Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Grammar-Guided Accuracy",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = PolishNavy
                                )
                            )
                            Text(
                                text = "Focuses recognition on the current lesson vocabulary for tighter word alignment and timestamps",
                                style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(
                            checked = useGrammarMode,
                            onCheckedChange = { viewModel.setGrammarModeEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PolishTeal
                            ),
                            modifier = Modifier.testTag("grammar_mode_switch")
                        )
                    }

                    HorizontalDivider(color = Slate100)

                    val statusText = when (val state = modelDownloadState) {
                        is ModelDownloadState.Checking -> "Checking Voice Studio status..."
                        is ModelDownloadState.Downloading ->
                            "Setting up ${state.option.displayName} (${(state.progress * 100).toInt()}%)"
                        is ModelDownloadState.Extracting ->
                            "Optimizing ${state.option.displayName} (${(state.progress * 100).toInt()}%)"
                        is ModelDownloadState.Loading -> "Warming up ${state.option.displayName} recognition..."
                        is ModelDownloadState.Ready -> "${state.option.displayName} active & ready for practice"
                        is ModelDownloadState.Error -> "Setup paused: ${state.message}"
                    }

                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = PolishNavy,
                            fontWeight = FontWeight.Medium
                        )
                    )

                    if (modelDownloadState is ModelDownloadState.Error) {
                        Button(
                            onClick = { viewModel.retryModelDownload() },
                            colors = ButtonDefaults.buttonColors(containerColor = PolishTeal),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("settings_retry_vosk_button")
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Retry")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Retry Voice Setup", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    } else if (modelDownloadState is ModelDownloadState.Ready) {
                        OutlinedButton(
                            onClick = { viewModel.redownloadSpeechModel() },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, PolishTealLightBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("redownload_vosk_model_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh voice pack",
                                tint = PolishTeal
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Refresh ${selectedSpeechModel.displayName} Pack",
                                color = PolishTeal,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // --- SECTION 2: VOICE SPEECH SYNTHESIS ENGINE ---
            Text(
                text = "Audio Guide Accent & Speed",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = PolishTeal
                )
            )

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Slate100)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "SPEAKER ACCENT FOCUS",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate500
                        )
                    )

                    val accents = listOf(
                        Pair("IN", "India (Standard Indian)"),
                        Pair("US", "United States (General)"),
                        Pair("UK", "United Kingdom (Received)"),
                        Pair("AU", "Australia (General Australian)")
                    )

                    accents.forEach { (code, name) ->
                        val active = selectedAccent == code
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .clickable { viewModel.setAccent(code) }
                        ) {
                            RadioButton(
                                selected = active,
                                onClick = { viewModel.setAccent(code) },
                                colors = RadioButtonDefaults.colors(selectedColor = PolishTeal)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                                    color = PolishNavy
                                )
                            )
                        }
                    }

                    HorizontalDivider(color = Slate100)

                    Text(
                        text = "DEFAULT AUDIO SPEECH RATE",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate500
                        )
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val speeds = listOf(
                            Pair("0.75x", 0.75f),
                            Pair("1.0x", 1.0f),
                            Pair("1.25x", 1.25f),
                            Pair("1.5x", 1.5f)
                        )
                        speeds.forEach { (label, rate) ->
                            val active = selectedSpeed == rate
                            FilterChip(
                                selected = active,
                                onClick = { viewModel.setPlaybackSpeed(rate) },
                                label = { Text(label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PolishTeal,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // --- SECTION 3: GAMIFICATION GOALS ---
            Text(
                text = "Gamified Targets",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = PolishTeal
                )
            )

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Slate100)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "DAILY STUDY INTENSITY",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate500
                        )
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val intensityTargets = listOf(30, 50, 100)
                        intensityTargets.forEach { target ->
                            val active = userProgress?.dailyGoalXP == target
                            FilterChip(
                                selected = active,
                                onClick = { viewModel.updateDailyGoalXP(target) },
                                label = { Text("$target XP") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PolishTeal,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Text(
                        text = "30 XP = 2 short speaking exercises a day\n50 XP = Regular active workout\n100 XP = Fluent speaker drill mastery",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Slate500,
                            lineHeight = 16.sp
                        )
                    )
                }
            }

            // --- SECTION 4: ABOUT APP ---
            Text(
                text = "About App",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = PolishTeal
                )
            )

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Slate100),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(PolishTealLight)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "About App",
                                tint = PolishTeal,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Easy Speaking",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PolishNavy
                                )
                            )
                            Text(
                                text = "Version 1.0.0 • Spoken English Coach",
                                style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                            )
                        }
                    }

                    HorizontalDivider(color = Slate100)

                    Text(
                        text = "This app helps you improve pronunciation skill, fluency, and word recall while speaking.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = PolishNavy,
                            lineHeight = 22.sp
                        )
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = null,
                                tint = PolishTeal,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Word-level pronunciation & clarity feedback synced with audio playback",
                                style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = PolishTeal,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Natural fluency drills and connected speech practice",
                                style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = PolishTeal,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Smart spaced-repetition review for missed & tricky words",
                                style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                            )
                        }
                    }
                }
            }

            // --- SECTION 5: SYSTEM MAINTENANCE ---
            Text(
                text = "Course Maintenance",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = PolishTeal
                )
            )

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Slate100),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "RESET PROGRESS HISTORY",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate500
                        )
                    )

                    Text(
                        text = "This will erase your daily streaks, total XP points, completed lesson statuses, and spaced-repetition schedules.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                    )

                    Button(
                        onClick = { showResetDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Red500),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("reset_progress_button")
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Reset", tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reset All Study Data", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- RESET CONFIRMATION DIALOG ---
        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = { Text("Erase Progress History?") },
                text = { Text("Are you absolutely sure you want to delete all streaks, lesson completions, and spaced-repetition schedules? This cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.resetProgress()
                            showResetDialog = false
                        }
                    ) {
                        Text("Reset Everything", color = Red500, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) {
                        Text("Cancel", color = Slate500)
                    }
                }
            )
        }
    }
}
