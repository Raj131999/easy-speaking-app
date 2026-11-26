package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.EnglishViewModel
import com.example.ui.SpokenTextNormalizer
import com.example.ui.Screen
import com.example.ui.components.MascotCoach
import com.example.ui.components.MascotState
import com.example.ui.components.PhoneticMouthGuideDialog
import com.example.ui.components.PronunciationFeedbackChips
import com.example.ui.components.SonicVoiceOrb
import com.example.ui.components.SpokenVoiceToTextCard
import com.example.ui.components.resolvePhoneticGuideSound
import com.example.ui.theme.*

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
    val liveAudioLevel by viewModel.liveAudioLevel.collectAsState()
    val isPlayingBack by viewModel.isPlayingBack.collectAsState()
    val isTtsSpeaking by viewModel.isTtsSpeaking.collectAsState()
    val lastScore by viewModel.lastScore.collectAsState()
    val scoredWords by viewModel.scoredWords.collectAsState()
    val alignedWordTimings by viewModel.alignedWordTimings.collectAsState()
    val playbackPositionSec by viewModel.playbackPositionSec.collectAsState()
    val missedWordsSummary by viewModel.missedWordsSummary.collectAsState()
    val unclearWordsSummary by viewModel.unclearWordsSummary.collectAsState()
    val extraWordsSummary by viewModel.extraWordsSummary.collectAsState()
    val recognizedText by viewModel.recognizedText.collectAsState()
    val isProcessingSpeech by viewModel.isProcessingSpeech.collectAsState()

    var showMouthGuide by remember { mutableStateOf(false) }

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
            val targetIdx = scrollToTargetIndex
                ?: sentences.indexOfFirst { !it.isCompleted && it.lastAccuracy < 75 }.takeIf { it >= 0 }
                ?: (sentences.size - 1)
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
                    title = {
                        Text(
                            text = "Daily Sentences Map",
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
            ) {
                // Header guidance card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Slate100),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(PolishTealBg)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = "Map Guidance",
                                tint = PolishTeal,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "DAILY WORKOUT TRAIL",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PolishTeal,
                                    letterSpacing = 1.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Tap any station on the trail below. Score 75%+ to complete a location!",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Slate500
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
                    contentPadding = PaddingValues(top = 12.dp, bottom = 48.dp, start = 16.dp, end = 16.dp)
                ) {
                    itemsIndexed(sentences) { index, sentence ->
                        val isCompleted = sentence.isCompleted || sentence.lastAccuracy >= 75
                        val isFirstUncompleted = sentences.indexOfFirst { !it.isCompleted && it.lastAccuracy < 75 } == index
                        val isActive = isFirstUncompleted || (sentences.all { it.isCompleted || it.lastAccuracy >= 75 } && index == 0)
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
                                        accentColor = PolishTeal
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                MapPinCircle(
                                    isCompleted = isCompleted,
                                    isActive = isActive,
                                    accentColor = PolishTeal,
                                    number = index + 1
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Box(modifier = Modifier.weight(1f))
                            } else {
                                Box(modifier = Modifier.weight(1f))
                                Spacer(modifier = Modifier.width(14.dp))
                                MapPinCircle(
                                    isCompleted = isCompleted,
                                    isActive = isActive,
                                    accentColor = PolishTeal,
                                    number = index + 1
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Box(modifier = Modifier.weight(1f)) {
                                    MapLocationCard(
                                        title = sentence.category,
                                        subtitle = sentence.text,
                                        score = if (sentence.lastAccuracy > 0) sentence.lastAccuracy else null,
                                        isCompleted = isCompleted,
                                        isActive = isActive,
                                        accentColor = PolishTeal
                                    )
                                }
                            }
                        }

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
            sentences.find { it.id == activeSentence?.id }
                ?: sentences.getOrNull(effectiveIndex)
                ?: sentences.firstOrNull()
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isReviewSession) "Spaced Review Gym" else "Daily Speaking Gym",
                                fontWeight = FontWeight.Bold,
                                color = PolishNavy
                            )
                            if (isReviewSession) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = PolishTealLight,
                                    border = BorderStroke(1.dp, PolishTealLightBorder)
                                ) {
                                    Text(
                                        text = "REVIEW",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = PolishTealDark
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
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
            if (sentence != null) {
                val scrollState = rememberScrollState()
                val mascotState = when {
                    isRecording -> MascotState.LISTENING
                    lastScore != null && lastScore!! >= 75 -> MascotState.CELEBRATING
                    lastScore != null -> MascotState.ENCOURAGING
                    else -> MascotState.IDLE
                }
                val coachMessage = when {
                    isProcessingSpeech -> "Scoring your clarity..."
                    isRecording -> "I'm listening — speak naturally!"
                    lastScore != null && lastScore!! >= 75 -> "Great job! Scored ${lastScore}%"
                    lastScore != null -> "Almost there! Tap amber words to practice"
                    else -> "Listen first, then tap the orb to speak"
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(PolishBg)
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
                                color = PolishTeal,
                                letterSpacing = 1.2.sp
                            )
                        )
                        Surface(
                            onClick = { showMouthGuide = true },
                            shape = RoundedCornerShape(12.dp),
                            color = PolishAmberBg,
                            border = BorderStroke(1.dp, Orange100)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RecordVoiceOver,
                                    contentDescription = "Open Mouth Guide",
                                    tint = Orange600,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Sound: ${sentence.soundFocus.uppercase()}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Orange600
                                    )
                                )
                            }
                        }
                    }

                    LinearProgressIndicator(
                        progress = { if (totalCount > 0) (effectiveIndex + 1).toFloat() / totalCount else 1f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = PolishTeal,
                        trackColor = Slate200
                    )

                    // Primary Sentence Card
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Slate100),
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
                                text = if (isReviewSession) "REVIEW AND REPEAT" else "REPEAT AFTER ME",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Slate500,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            val displaySentenceText = remember(sentence.text) {
                                SpokenTextNormalizer.normalizeToSpokenWords(sentence.text)
                            }
                            Text(
                                text = displaySentenceText,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = PolishNavy,
                                    lineHeight = 34.sp
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FilledTonalButton(
                                    onClick = { viewModel.speak(sentence.text) },
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = if (isTtsSpeaking) Red500 else PolishTealBg,
                                        contentColor = if (isTtsSpeaking) Color.White else PolishTeal
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.height(48.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isTtsSpeaking) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = if (isTtsSpeaking) "Stop" else "Listen",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isTtsSpeaking) "Stop Audio" else "Hear Native",
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                FilledTonalButton(
                                    onClick = { showMouthGuide = true },
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = PolishIndigoBg,
                                        contentColor = PolishIndigoText
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.height(48.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Face,
                                        contentDescription = "Mouth Guide",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Mouth Guide",
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    // --- RECORDING CONTROLS & MASCOT COACH CARD ---
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Slate100),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            MascotCoach(
                                state = mascotState,
                                audioLevel = liveAudioLevel,
                                coachMessage = coachMessage,
                                size = 72.dp
                            )

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
                                            when {
                                                isPlayingBack -> Red500
                                                lastScore != null -> PolishIndigoBg
                                                else -> Slate100
                                            },
                                            CircleShape
                                        )
                                ) {
                                    Icon(
                                        imageVector = if (isPlayingBack) Icons.Default.Stop else Icons.Default.PlayArrow,
                                        contentDescription = "Play your recording",
                                        tint = when {
                                            isPlayingBack -> Color.White
                                            lastScore != null -> PolishIndigoText
                                            else -> Slate400
                                        }
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                // Sonic Pulse Audio-Reactive Voice Orb
                                SonicVoiceOrb(
                                    isRecording = isRecording,
                                    audioLevel = liveAudioLevel,
                                    isProcessing = isProcessingSpeech,
                                    onClick = {
                                        if (isRecording) {
                                            viewModel.stopRecording(sentence.text, "daily_sentence", sentence.id)
                                        } else {
                                            viewModel.startRecording(sentence.text, "daily_sentence")
                                        }
                                    },
                                    size = 64.dp,
                                    coreColor = when {
                                        isProcessingSpeech -> Orange500
                                        isRecording -> Red500
                                        else -> PolishTeal
                                    },
                                    glowColor = when {
                                        isProcessingSpeech -> Orange100
                                        isRecording -> Red400
                                        else -> Color(0xFF2DD4BF)
                                    },
                                    modifier = Modifier.testTag("sentence_record_mic")
                                )

                                Spacer(modifier = Modifier.width(12.dp))

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
                                        .background(
                                            if (lastScore != null) Slate100 else Slate50,
                                            CircleShape
                                        )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Retry",
                                        tint = if (lastScore != null) PolishNavy else Slate400
                                    )
                                }
                            }

                            Text(
                                text = when {
                                    isProcessingSpeech -> "Analyzing your audio... Please wait"
                                    isRecording -> "Recording... Tap the orb to finish"
                                    lastScore != null -> "Tap the orb to try again"
                                    else -> "Tap the orb to record yourself"
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = when {
                                        isProcessingSpeech -> Orange600
                                        isRecording -> Red500
                                        else -> Slate500
                                    },
                                    fontWeight = if (isProcessingSpeech || isRecording) FontWeight.Bold else FontWeight.Medium
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
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = PolishTeal,
                                                contentColor = Color.White
                                            ),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(48.dp),
                                            shape = RoundedCornerShape(14.dp)
                                        ) {
                                            Text(
                                                text = if (isReviewSession) "Next Review" else "Next Lesson",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                contentDescription = "Next Lesson",
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
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
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Slate100,
                                            contentColor = PolishNavy
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Text(
                                            text = "Finish (Map)",
                                            color = PolishNavy,
                                            fontWeight = FontWeight.SemiBold
                                        )
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
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = PolishTealBg
                                    ),
                                    border = BorderStroke(1.dp, PolishTealLightBorder),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = PolishTeal,
                                            strokeWidth = 2.5.dp
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "Analyzing your speech...",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = PolishTeal
                                                )
                                            )
                                            Text(
                                                text = "Evaluating pronunciation and fluency...",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = Slate500
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
                                HorizontalDivider(color = Slate100)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "PRONUNCIATION SCORE",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Slate500
                                            )
                                        )
                                        Text(
                                            text = if (isPassed) "Lesson Passed (>= 75%)" else "Need 75% to Complete",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (isPassed) Emerald600 else Orange600
                                            )
                                        )
                                    }
                                    Text(
                                        text = "$score%",
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = when {
                                                score >= 75 -> Emerald600
                                                score >= 50 -> Orange600
                                                else -> Red500
                                            }
                                        )
                                    )
                                }

                                // Word-level alignment & playback-synced timestamp highlighting
                                PronunciationFeedbackChips(
                                    alignedTimings = alignedWordTimings,
                                    fallbackScoredWords = scoredWords,
                                    playbackPositionSec = playbackPositionSec,
                                    isPlayingBack = isPlayingBack,
                                    missedWords = missedWordsSummary,
                                    unclearWords = unclearWordsSummary,
                                    extraWords = extraWordsSummary,
                                    onWordClick = { word -> viewModel.speak(word) }
                                )
                            }
                        }
                    }
                }

                if (showMouthGuide) {
                    PhoneticMouthGuideDialog(
                        initialSound = resolvePhoneticGuideSound(sentence.soundFocus),
                        onDismiss = { showMouthGuide = false },
                        onSpeakWord = { word -> viewModel.speak(word) }
                    )
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No review sentences available.", color = Slate500)
                }
            }
        }
    }
}

// --- REUSABLE MODERN TRAIL MAP COMPONENTS ---

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
            .then(
                when {
                    isCompleted -> Modifier.background(Emerald500)
                    isActive -> Modifier.background(
                        Brush.linearGradient(listOf(accentColor, PolishTealDark))
                    )
                    else -> Modifier.background(Color.White)
                }
            )
            .border(
                width = if (isActive) 3.dp else 1.5.dp,
                color = when {
                    isCompleted -> Emerald100
                    isActive -> PolishTealLight
                    else -> Slate200
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
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        } else {
            Text(
                text = number.toString(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Slate500
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
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isCompleted -> Emerald50.copy(alpha = 0.65f)
                isActive -> Color.White
                else -> Color.White
            }
        ),
        border = BorderStroke(
            width = if (isCompleted || isActive) 1.5.dp else 1.dp,
            color = when {
                isCompleted -> Emerald500.copy(alpha = 0.55f)
                isActive -> accentColor
                else -> Slate200
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isActive) 3.dp else 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val badgeBg = when {
                isCompleted -> Emerald100
                isActive -> PolishTealBg
                else -> Slate100
            }
            val badgeTextColor = when {
                isCompleted -> Emerald600
                isActive -> accentColor
                else -> Slate500
            }
            val badgeText = customStatusText ?: when {
                isCompleted -> "Completed"
                isActive -> "Current Topic"
                else -> "Ready"
            }
            val badgeIcon = when {
                isCompleted -> Icons.Default.CheckCircle
                isActive -> Icons.Default.PlayCircle
                else -> Icons.Default.RadioButtonUnchecked
            }

            Surface(
                color = badgeBg,
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = badgeIcon,
                        contentDescription = null,
                        tint = badgeTextColor,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = badgeTextColor,
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
                    color = PolishNavy
                ),
                maxLines = 1
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Slate500,
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
                        tint = Orange600,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "Score: $score%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Orange600,
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
                .width(3.dp)
                .fillMaxHeight()
                .background(
                    color = Slate200,
                    shape = RoundedCornerShape(2.dp)
                )
        )
    }
}
