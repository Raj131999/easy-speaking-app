package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.example.ui.components.SpokenVoiceToTextCard
import com.example.ui.theme.PolishTeal
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
    val isReviewSession by viewModel.isReviewSession.collectAsState()
    val reviewList by viewModel.reviewSentencesList.collectAsState()
    val reviewIndex by viewModel.currentReviewIndex.collectAsState()

    val isRecording by viewModel.isRecording.collectAsState()
    val isPlayingBack by viewModel.isPlayingBack.collectAsState()
    val isTtsSpeaking by viewModel.isTtsSpeaking.collectAsState()
    val lastScore by viewModel.lastScore.collectAsState()
    val scoredWords by viewModel.scoredWords.collectAsState()
    val recognizedText by viewModel.recognizedText.collectAsState()
    val isProcessingSpeech by viewModel.isProcessingSpeech.collectAsState()

    val currentList = if (isReviewSession) reviewList else sentences
    val effectiveIndex = if (isReviewSession) {
        reviewIndex.coerceIn(0, (reviewList.size - 1).coerceAtLeast(0))
    } else {
        val index = sentences.indexOfFirst { it.id == activeSentence?.id }
        if (index == -1) 0 else index
    }
    val totalCount = currentList.size

    val mapListState = rememberLazyListState()
    var scrollToTargetIndex by remember { mutableStateOf<Int?>(null) }

    val completedCount = remember(sentences) { sentences.count { it.isCompleted || it.lastAccuracy >= 75 } }

    LaunchedEffect(activeSentence == null, isReviewSession, completedCount, scrollToTargetIndex) {
        if (!isReviewSession && activeSentence == null && sentences.isNotEmpty()) {
            val targetIdx = scrollToTargetIndex ?: sentences.indexOfFirst { !it.isCompleted && it.lastAccuracy < 75 }.takeIf { it >= 0 } ?: (sentences.size - 1)
            if (targetIdx in sentences.indices) {
                mapListState.animateScrollToItem(targetIdx)
            }
            scrollToTargetIndex = null
        }
    }

    // Intercept back button when in workout view or review session
    BackHandler(enabled = (activeSentence != null || isReviewSession)) {
        if (isReviewSession) {
            viewModel.endReviewSession()
            viewModel.goBack()
        } else {
            if (effectiveIndex in sentences.indices) {
                scrollToTargetIndex = effectiveIndex
            }
            viewModel.activeDailySentence.value = null
            viewModel.lastScore.value = null
            viewModel.scoredWords.value = emptyList()
            viewModel.recognizedText.value = ""
        }
    }

    if (!isReviewSession && activeSentence == null) {
        // --- MAP VIEW OF LOCATIONS ---
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Daily Sentences Map") },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.goBack() }) {
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
                    state = mapListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp, start = 16.dp, end = 16.dp)
                ) {
                    itemsIndexed(sentences) { index, sentence ->
                        val isCompleted = sentence.isCompleted || sentence.lastAccuracy >= 75
                        // The first uncompleted item or the current selected one is active
                        val isFirstUncompleted = sentences.indexOfFirst { !it.isCompleted && it.lastAccuracy < 75 } == index
                        val isActive = isFirstUncompleted || (sentences.all { it.isCompleted || it.lastAccuracy >= 75 } && index == 0)

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
        // --- PRACTICE / REVIEW VIEW ---
        val sentence = if (isReviewSession) {
            reviewList.getOrNull(effectiveIndex) ?: reviewList.firstOrNull()
        } else {
            sentences.find { it.id == activeSentence?.id } ?: sentences.getOrNull(effectiveIndex) ?: sentences.firstOrNull()
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(if (isReviewSession) "Spaced Review Gym" else "Daily Speaking Gym")
                            if (isReviewSession) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = PolishTeal.copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, PolishTeal.copy(alpha = 0.3f))
                                ) {
                                    Text(
                                        text = "REVIEW",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = PolishTeal
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (isReviewSession) {
                                viewModel.endReviewSession()
                                viewModel.goBack()
                            } else {
                                if (effectiveIndex in sentences.indices) {
                                  scrollToTargetIndex = effectiveIndex
                                }
                                viewModel.activeDailySentence.value = null
                                viewModel.lastScore.value = null
                                viewModel.scoredWords.value = emptyList()
                                viewModel.recognizedText.value = ""
                            }
                        }) {
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
            if (sentence != null) {
                val scrollState = rememberScrollState()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(innerPadding)
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Workout Progression indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isReviewSession) {
                                "REVIEW ITEM ${effectiveIndex + 1} OF $totalCount"
                            } else {
                                "SENTENCE ${effectiveIndex + 1} OF $totalCount"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isReviewSession) PolishTeal else TealPrimary,
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
                        progress = { if (totalCount > 0) (effectiveIndex + 1).toFloat() / totalCount else 1f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (isReviewSession) PolishTeal else TealPrimary,
                        trackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                    )

                    // Primary Sentence Card
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (isReviewSession) "REVIEW AND REPEAT:" else "REPEAT AFTER ME:",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    letterSpacing = 1.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = sentence.text,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 34.sp
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Play Speaker button
                            FilledTonalButton(
                                onClick = { viewModel.speak(sentence.text) },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = if (isTtsSpeaking) Color(0xFFFF5252) else if (isReviewSession) PolishTeal.copy(alpha = 0.15f) else TealPrimary.copy(alpha = 0.15f),
                                    contentColor = if (isTtsSpeaking) Color.White else if (isReviewSession) PolishTeal else TealPrimary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = if (isTtsSpeaking) Icons.Default.Stop else Icons.Default.VolumeUp,
                                    contentDescription = if (isTtsSpeaking) "Stop" else "Listen",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isTtsSpeaking) "Stop Audio" else "Hear Native Speaker",
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
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
                                    onClick = { viewModel.playRecordedVoice() },
                                    enabled = lastScore != null,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(
                                            if (isPlayingBack) Color(0xFFFF5252) else MaterialTheme.colorScheme.secondary.copy(alpha = if (lastScore != null) 0.5f else 0.15f),
                                            CircleShape
                                        )
                                ) {
                                    Icon(
                                        imageVector = if (isPlayingBack) Icons.Default.Stop else Icons.Default.PlayArrow,
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
                                            .background(
                                                when {
                                                    isProcessingSpeech -> StreakGold
                                                    isRecording -> Color(0xFFFF5252)
                                                    isReviewSession -> PolishTeal
                                                    else -> TealPrimary
                                                }
                                            )
                                            .clickable(enabled = !isProcessingSpeech) {
                                                if (isRecording) {
                                                    viewModel.stopRecording(sentence.text, "daily_sentence", sentence.id)
                                                } else {
                                                    viewModel.startRecording(sentence.text)
                                                }
                                            }
                                            .testTag("sentence_record_mic"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isProcessingSpeech) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(32.dp),
                                                color = Color.Black,
                                                strokeWidth = 3.dp
                                            )
                                        } else {
                                            Icon(
                                                imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                                                contentDescription = "Record",
                                                tint = Color.Black,
                                                modifier = Modifier.size(32.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(24.dp))

                                // Retry / Clear attempt button
                                IconButton(
                                    onClick = {
                                        viewModel.lastScore.value = null
                                        viewModel.scoredWords.value = emptyList()
                                        viewModel.recognizedText.value = ""
                                    },
                                    enabled = lastScore != null && !isProcessingSpeech,
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
                                text = when {
                                    isProcessingSpeech -> "Analyzing your audio... Please wait"
                                    isRecording -> "Recording... Speak clearly now"
                                    lastScore != null -> "Tap microphone to try again"
                                    else -> "Tap microphone to record yourself"
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = when {
                                        isProcessingSpeech -> StreakGold
                                        isRecording -> Color(0xFFFF5252)
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    fontWeight = if (isProcessingSpeech || isRecording) FontWeight.Bold else FontWeight.Normal
                                )
                            )

                            // Action buttons right under mic after speaking
                            if (lastScore != null) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp)
                                ) {
                                    val hasNext = if (isReviewSession) reviewIndex < reviewList.size - 1 else effectiveIndex < sentences.size - 1
                                    if (hasNext) {
                                        Button(
                                            onClick = {
                                                if (isReviewSession) {
                                                    val nextIdx = reviewIndex + 1
                                                    viewModel.currentReviewIndex.value = nextIdx
                                                    viewModel.activeDailySentence.value = reviewList[nextIdx]
                                                } else {
                                                    viewModel.activeDailySentence.value = sentences[effectiveIndex + 1]
                                                }
                                                viewModel.lastScore.value = null
                                                viewModel.scoredWords.value = emptyList()
                                                viewModel.recognizedText.value = ""
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = if (isReviewSession) PolishTeal else TealPrimary),
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text(if (isReviewSession) "Next Review" else "Next Lesson", color = Color.Black, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Next Lesson", tint = Color.Black)
                                        }
                                    }
                                    Button(
                                        onClick = {
                                            if (isReviewSession) {
                                                viewModel.endReviewSession()
                                                viewModel.navigateTo(Screen.Home)
                                            } else {
                                                if (effectiveIndex in sentences.indices) {
                                                    scrollToTargetIndex = effectiveIndex
                                                }
                                                viewModel.activeDailySentence.value = null
                                            }
                                            viewModel.lastScore.value = null
                                            viewModel.scoredWords.value = emptyList()
                                            viewModel.recognizedText.value = ""
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Finish (Map)", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }

                            // Audio processing notification banner
                            AnimatedVisibility(
                                visible = isProcessingSpeech,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = TealPrimary.copy(alpha = 0.12f)
                                    ),
                                    border = BorderStroke(1.dp, TealPrimary.copy(alpha = 0.7f)),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = TealPrimary,
                                            strokeWidth = 2.5.dp
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "Analyzing your speech...",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = TealPrimary
                                                )
                                            )
                                            Text(
                                                text = "Evaluating pronunciation and fluency...",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            )
                                        }
                                    }
                                }
                            }

                            // Real-time Voice to Text Display
                            SpokenVoiceToTextCard(
                                isRecording = isRecording,
                                recognizedText = recognizedText,
                                hasEvaluated = lastScore != null
                            )

                            // Accuracy Score and Feedback
                            lastScore?.let { score ->
                                val isPassed = score >= 75
                                HorizontalDivider()

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "PRONUNCIATION SCORE:",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                        Text(
                                            text = if (isPassed) "Lesson Passed (>= 75%)" else "Need 75% to Complete",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (isPassed) Color(0xFF2ECC71) else StreakGold
                                            )
                                        )
                                    }
                                    Text(
                                        text = "$score%",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = when {
                                                score >= 90 -> Color(0xFF2ECC71)
                                                score >= 75 -> Color(0xFF2ECC71)
                                                score >= 50 -> StreakGold
                                                else -> Color(0xFFFF5252)
                                            }
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
                            }
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No review sentences available.")
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
                width = if (isActive || isCompleted) 3.dp else 1.5.dp,
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
                modifier = Modifier.size(26.dp)
            )
        } else if (isActive) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Active",
                tint = Color.Black,
                modifier = Modifier.size(26.dp)
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
    score: Int? = null,
    isCompleted: Boolean,
    isActive: Boolean,
    accentColor: Color,
    customStatusText: String? = null
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isCompleted -> Color(0xFF2ECC71).copy(alpha = 0.08f)
                isActive -> accentColor.copy(alpha = 0.06f)
                else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
            }
        ),
        border = BorderStroke(
            width = if (isCompleted || isActive) 1.5.dp else 1.dp,
            color = when {
                isCompleted -> Color(0xFF2ECC71)
                isActive -> accentColor
                else -> MaterialTheme.colorScheme.outlineVariant
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isActive || isCompleted) 2.dp else 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Status Badge
            val badgeColor = when {
                isCompleted -> Color(0xFF2ECC71)
                isActive -> accentColor
                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            }
            val badgeText = customStatusText ?: when {
                isCompleted -> "Completed"
                isActive -> "Current Topic"
                else -> "Not Completed"
            }
            val badgeIcon = when {
                isCompleted -> Icons.Default.CheckCircle
                isActive -> Icons.Default.PlayCircle
                else -> Icons.Default.RadioButtonUnchecked
            }

            Surface(
                color = badgeColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = badgeIcon,
                        contentDescription = null,
                        tint = badgeColor,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = badgeColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isCompleted || isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
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
