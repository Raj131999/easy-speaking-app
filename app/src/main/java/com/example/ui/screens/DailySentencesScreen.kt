package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DailySentence
import com.example.ui.EnglishViewModel
import com.example.ui.Screen
import com.example.ui.WordScoreType
import com.example.ui.theme.StreakGold
import com.example.ui.theme.TealPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailySentencesScreen(
    viewModel: EnglishViewModel,
    modifier: Modifier = Modifier
) {
    val sentences by viewModel.dailySentences.collectAsState()
    val activeSentence by viewModel.activeDailySentence.collectAsState()

    val isRecording by viewModel.isRecording.collectAsState()
    val isPlayingBack by viewModel.isPlayingBack.collectAsState()
    val lastScore by viewModel.lastScore.collectAsState()
    val scoredWords by viewModel.scoredWords.collectAsState()

    val currentIndex = remember(activeSentence, sentences) {
        val index = sentences.indexOfFirst { it.id == activeSentence?.id }
        if (index == -1) 0 else index
    }

    if (activeSentence == null) {
        // --- MAP VIEW OF LOCATIONS ---
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Daily Sentences Map") },
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
            ) {
                // Header guidance
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = TealPrimary.copy(alpha = 0.08f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = "Map Guidance",
                            tint = TealPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "DAILY WORKOUT TRAIL",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TealPrimary
                                )
                            )
                            Text(
                                text = "Practice any daily sentence location on the trail below. Perfect your score to complete a location!",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }

                // Map Path
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp, start = 16.dp, end = 16.dp)
                ) {
                    itemsIndexed(sentences) { index, sentence ->
                        val isCompleted = sentence.timesPracticed > 0
                        // The first uncompleted item or the current selected one is active
                        val isFirstUncompleted = sentences.indexOfFirst { it.timesPracticed == 0 } == index
                        val isActive = isFirstUncompleted || (sentences.all { it.timesPracticed > 0 } && index == 0)

                        // Alternate left & right detail cards around the center line
                        val isLeft = index % 2 == 0

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.activeDailySentence.value = sentence
                                    viewModel.lastScore.value = null
                                    viewModel.scoredWords.value = emptyList()
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isLeft) {
                                Box(modifier = Modifier.weight(1f)) {
                                    MapLocationCard(
                                        title = sentence.category,
                                        subtitle = sentence.text,
                                        score = if (sentence.lastAccuracy > 0) sentence.lastAccuracy else null,
                                        isCompleted = isCompleted,
                                        isActive = isActive,
                                        accentColor = TealPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                MapPinCircle(
                                    isCompleted = isCompleted,
                                    isActive = isActive,
                                    accentColor = TealPrimary,
                                    number = index + 1
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Box(modifier = Modifier.weight(1f)) // Empty spacer same weight
                            } else {
                                Box(modifier = Modifier.weight(1f)) // Empty spacer same weight
                                Spacer(modifier = Modifier.width(16.dp))
                                MapPinCircle(
                                    isCompleted = isCompleted,
                                    isActive = isActive,
                                    accentColor = TealPrimary,
                                    number = index + 1
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Box(modifier = Modifier.weight(1f)) {
                                    MapLocationCard(
                                        title = sentence.category,
                                        subtitle = sentence.text,
                                        score = if (sentence.lastAccuracy > 0) sentence.lastAccuracy else null,
                                        isCompleted = isCompleted,
                                        isActive = isActive,
                                        accentColor = TealPrimary
                                    )
                                }
                            }
                        }

                        // Connect to next item
                        if (index < sentences.size - 1) {
                            MapConnector()
                        }
                    }
                }
            }
        }
    } else {
        // --- PRACTICE VIEW ---
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Daily Speaking Gym") },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.activeDailySentence.value = null }) {
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
            if (sentences.isNotEmpty()) {
                val sentence = sentences.getOrNull(currentIndex) ?: sentences.first()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(innerPadding)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Workout Progression indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SENTENCE ${currentIndex + 1} OF ${sentences.size}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TealPrimary,
                                letterSpacing = 1.2.sp
                            )
                        )
                        SuggestionChip(
                            onClick = {},
                            label = { Text("Sound: ${sentence.soundFocus.uppercase()}") },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = StreakGold.copy(alpha = 0.15f),
                                labelColor = StreakGold
                            )
                        )
                    }

                    LinearProgressIndicator(
                        progress = { (currentIndex + 1).toFloat() / sentences.size },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = TealPrimary,
                        trackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Primary Sentence Card
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "REPEAT AFTER ME:",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    letterSpacing = 1.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = sentence.text,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 36.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            // Play Speaker button
                            IconButton(
                                onClick = { viewModel.speak(sentence.text) },
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(TealPrimary.copy(alpha = 0.15f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Listen",
                                    tint = TealPrimary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "HEAR NATIVE SPEAKER",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = TealPrimary
                                )
                            )
                        }
                    }

                    // --- RECORDING CONTROLS CARD ---
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // User speech recorded voice playback
                                IconButton(
                                    onClick = {
                                        if (isPlayingBack) {
                                            viewModel.stopRecordedVoicePlayback()
                                        } else {
                                            viewModel.playRecordedVoice()
                                        }
                                    },
                                    enabled = lastScore != null,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(
                                            if (isPlayingBack) Color(0xFFFF5252) else MaterialTheme.colorScheme.secondary.copy(alpha = if (lastScore != null) 0.5f else 0.15f),
                                            CircleShape
                                        )
                                ) {
                                    Icon(
                                        imageVector = if (isPlayingBack) Icons.Default.VolumeMute else Icons.Default.PlayArrow,
                                        contentDescription = "UserPlayback",
                                        tint = if (lastScore != null) Color.White else Color.Gray
                                    )
                                }

                                Spacer(modifier = Modifier.width(24.dp))

                                // Large central Mic button
                                Box(contentAlignment = Alignment.Center) {
                                    Box(
                                        modifier = Modifier
                                            .size(76.dp)
                                            .clip(CircleShape)
                                            .background(if (isRecording) Color(0xFFFF5252) else TealPrimary)
                                            .clickable {
                                                if (isRecording) {
                                                    viewModel.stopRecording(sentence.text, "daily_sentence", sentence.id)
                                                } else {
                                                    viewModel.startRecording(sentence.text)
                                                }
                                            }
                                            .testTag("sentence_record_mic"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                                            contentDescription = "Record",
                                            tint = Color.Black,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(24.dp))

                                // Retry / Clear attempt button
                                IconButton(
                                    onClick = {
                                        viewModel.lastScore.value = null
                                        viewModel.scoredWords.value = emptyList()
                                    },
                                    enabled = lastScore != null,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = if (lastScore != null) 0.5f else 0.15f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Retry",
                                        tint = if (lastScore != null) Color.White else Color.Gray
                                    )
                                }
                            }

                            Text(
                                text = if (isRecording) "Recording... Speak clearly now" else "Tap microphone to record yourself",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (isRecording) Color(0xFFFF5252) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )

                            // Accuracy Score and Feedback
                            lastScore?.let { score ->
                                HorizontalDivider()

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "PRONUNCIATION SCORE:",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                    Text(
                                        text = "$score%",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (score >= 90) Color(0xFF2ECC71) else StreakGold
                                        )
                                    )
                                }

                                // Dynamic Phonetics Highlight
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    scoredWords.forEach { (word, scoreType) ->
                                        val color = when (scoreType) {
                                            WordScoreType.Correct -> Color(0xFF2ECC71)
                                            WordScoreType.Hesitant -> StreakGold
                                            WordScoreType.Incorrect -> Color(0xFFFF5252)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .padding(4.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(color.copy(alpha = 0.15f))
                                                .clickable { viewModel.speak(word) }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = word,
                                                color = color,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Next navigation button
                                Button(
                                    onClick = {
                                        if (currentIndex < sentences.size - 1) {
                                            // Advance to next card
                                            viewModel.activeDailySentence.value = sentences[currentIndex + 1]
                                            viewModel.lastScore.value = null
                                            viewModel.scoredWords.value = emptyList()
                                        } else {
                                            // Completed workout! Go back to Map
                                            viewModel.activeDailySentence.value = null
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = if (currentIndex < sentences.size - 1) "Next Sentence" else "Back to Map",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Next", tint = Color.Black)
                                }
                            }
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No workout sentences available.")
                }
            }
        }
    }
}

// --- REUSABLE MAP COMPONENTS ---

@Composable
fun MapPinCircle(
    isCompleted: Boolean,
    isActive: Boolean,
    accentColor: Color,
    number: Int
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(54.dp)
            .clip(CircleShape)
            .background(
                when {
                    isCompleted -> Color(0xFF2ECC71)
                    isActive -> accentColor
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                }
            )
            .border(
                width = if (isActive) 3.dp else 1.5.dp,
                color = when {
                    isCompleted -> Color(0xFF2ECC71)
                    isActive -> Color.White
                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                },
                shape = CircleShape
            )
    ) {
        if (isCompleted) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Completed",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        } else if (isActive) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Active",
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Text(
                text = number.toString(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            )
        }
    }
}

@Composable
fun MapLocationCard(
    title: String,
    subtitle: String,
    score: Int?,
    isCompleted: Boolean,
    isActive: Boolean,
    accentColor: Color
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
        ),
        border = BorderStroke(
            width = if (isActive) 1.5.dp else 1.dp,
            color = if (isActive) accentColor else MaterialTheme.colorScheme.outlineVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isActive) 2.dp else 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                ),
                maxLines = 1
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                ),
                maxLines = 2
            )
            if (score != null && score > 0) {
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Score",
                        tint = StreakGold,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "Score: $score%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = StreakGold,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun MapConnector() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(2.dp)
                )
        )
    }
}
