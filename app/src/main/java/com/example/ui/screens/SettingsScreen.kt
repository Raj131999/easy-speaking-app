package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.EnglishViewModel
import com.example.ui.Screen
import com.example.ui.theme.TealPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: EnglishViewModel,
    modifier: Modifier = Modifier
) {
    val selectedAccent by viewModel.selectedAccent.collectAsState()
    val selectedSpeed by viewModel.selectedSpeed.collectAsState()
    val userProgress by viewModel.userProgress.collectAsState()

    val scrollState = rememberScrollState()
    var showResetDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(Screen.Home) }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // --- SECTION 1: VOICE SPEECH SYNTHESIS ENGINE ---
            Text(
                text = "Voice Synthesis Settings",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TealPrimary)
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "VOICE ACCENT FOCUS",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    val accents = listOf(
                        Pair("US", "United States (General)"),
                        Pair("UK", "United Kingdom (Received)"),
                        Pair("IN", "India (Standard Indian)"),
                        Pair("AU", "Australia (General Australian)")
                    )

                    accents.forEach { (code, name) ->
                        val active = selectedAccent == code
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            RadioButton(
                                selected = active,
                                onClick = { viewModel.setAccent(code) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }

                    HorizontalDivider()

                    Text(
                        text = "DEFAULT AUDIO SPEECH RATE",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val speeds = listOf(
                            Pair("0.75x", 0.75f),
                            Pair("1.0x (Normal)", 1.0f),
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
                                    selectedContainerColor = TealPrimary,
                                    selectedLabelColor = Color.Black
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // --- SECTION 2: GAMIFICATION GOALS ---
            Text(
                text = "Gamified Targets",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TealPrimary)
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "DAILY STUDY INTENSITY",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                onClick = {
                                    viewModel.updateDailyGoalXP(target)
                                },
                                label = { Text("$target XP") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TealPrimary,
                                    selectedLabelColor = Color.Black
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Text(
                        text = "30 XP = 2 short speaking exercises a day\n50 XP = Regular active workout\n100 XP = Fluent speaker drill mastery",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    )
                }
            }

            // --- SECTION 2.5: MICROPHONE & FEEDBACK ---
            Text(
                text = "Microphone & Feedback",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TealPrimary)
            )

            val useSimulatedMic by viewModel.useSimulatedMic.collectAsState()

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "RECORDING SOURCE MODE",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "High-Fidelity AI Simulation",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = "Models native speaker pronunciation for perfect audio feedback when physical microphones are unavailable.",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                        Switch(
                            checked = useSimulatedMic,
                            onCheckedChange = { viewModel.useSimulatedMic.value = it }
                        )
                    }

                    HorizontalDivider()

                    Text(
                        text = if (useSimulatedMic) {
                            "✨ Active: Recording evaluates speaking pace, and playback simulates target sentence pronunciation dynamically."
                        } else {
                            "🎤 Active: Captures physical device audio inputs directly. Note: browser emulators may record silence/static."
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (useSimulatedMic) TealPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }

            // --- SECTION 3: SYSTEM MAINTENANCE ---
            Text(
                text = "Course Maintenance",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TealPrimary)
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    Text(
                        text = "This will erase your daily streaks, total XP points, completed lesson statuses, and spaced-repetition schedules.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )

                    Button(
                        onClick = { showResetDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reset_progress_button")
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Reset")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reset All Study Data", color = Color.White)
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
                        Text("Reset Everything", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            )
        }
    }
}
