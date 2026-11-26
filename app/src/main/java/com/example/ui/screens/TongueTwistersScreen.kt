package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.EnglishViewModel
import com.example.ui.SpokenTextNormalizer
import com.example.ui.components.MascotCoach
import com.example.ui.components.MascotState
import com.example.ui.components.PhoneticMouthGuideDialog
import com.example.ui.components.PronunciationFeedbackChips
import com.example.ui.components.SonicVoiceOrb
import com.example.ui.components.resolvePhoneticGuideSound
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TongueTwistersScreen(
    viewModel: EnglishViewModel,
    modifier: Modifier = Modifier
) {
    val twisters by viewModel.tongueTwisters.collectAsState()
    val activeTwister by viewModel.activeTongueTwister.collectAsState()

    val isRecording by viewModel.isRecording.collectAsState()
    val liveAudioLevel by viewModel.liveAudioLevel.collectAsState()
    val isPlayingBack by viewModel.isPlayingBack.collectAsState()
    val isTtsSpeaking by viewModel.isTtsSpeaking.collectAsState()
    val selectedSpeed by viewModel.selectedSpeed.collectAsState()
    val lastScore by viewModel.lastScore.collectAsState()
    val scoredWords by viewModel.scoredWords.collectAsState()
    val alignedWordTimings by viewModel.alignedWordTimings.collectAsState()
    val playbackPositionSec by viewModel.playbackPositionSec.collectAsState()
    val missedWordsSummary by viewModel.missedWordsSummary.collectAsState()
    val unclearWordsSummary by viewModel.unclearWordsSummary.collectAsState()
    val extraWordsSummary by viewModel.extraWordsSummary.collectAsState()
    val isProcessingSpeech by viewModel.isProcessingSpeech.collectAsState()

    var showMouthGuide by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val mapListState = rememberLazyListState()

    val currentIndex = remember(activeTwister, twisters) {
        val index = twisters.indexOfFirst { it.id == activeTwister?.id }
        if (index == -1) 0 else index
    }

    var scrollToTargetIndex by remember { mutableStateOf<Int?>(null) }
    val twistersCompletedCount = remember(twisters) { twisters.count { it.isCompleted || it.maxAccuracy >= 75 } }

    // Auto-scroll map to next uncompleted tongue twister or explicit target
    LaunchedEffect(activeTwister == null, twistersCompletedCount, scrollToTargetIndex) {
        if (activeTwister == null && twisters.isNotEmpty()) {
            val targetIdx = scrollToTargetIndex
                ?: twisters.indexOfFirst { !it.isCompleted && it.maxAccuracy < 75 }.takeIf { it >= 0 }
                ?: (twisters.size - 1)
            if (targetIdx in twisters.indices) {
                mapListState.animateScrollToItem(targetIdx)
            }
            scrollToTargetIndex = null
        }
    }

    // Intercept back button when in soundroom practice to return to that twister on the map
    BackHandler(enabled = (activeTwister != null)) {
        if (currentIndex in twisters.indices) {
            scrollToTargetIndex = currentIndex
        }
        viewModel.activeTongueTwister.value = null
        viewModel.lastScore.value = null
        viewModel.scoredWords.value = emptyList()
        viewModel.recognizedText.value = ""
    }

    LaunchedEffect(activeTwister) {
        viewModel.lastScore.value = null
        viewModel.scoredWords.value = emptyList()
    }

    if (activeTwister == null) {
        // --- MAP VIEW OF TONGUE TWISTERS ---
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Tongue Twister Map",
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
                // Header guidance
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
                                text = "ARTICULATION CHALLENGE PEAK",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PolishTeal,
                                    letterSpacing = 0.8.sp
                                )
                            )
                            Text(
                                text = "Sharpen speed and clarity on tricky sound combinations across the peak trail.",
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
                    itemsIndexed(twisters) { index, twister ->
                        val isCompleted = twister.isCompleted || twister.maxAccuracy >= 75
                        val isFirstUncompleted = twisters.indexOfFirst { !it.isCompleted && it.maxAccuracy < 75 } == index
                        val isActive = isFirstUncompleted || (twisters.all { it.isCompleted || it.maxAccuracy >= 75 } && index == 0)
                        val isLeft = index % 2 == 0

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.activeTongueTwister.value = twister
                                    viewModel.lastScore.value = null
                                    viewModel.scoredWords.value = emptyList()
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isLeft) {
                                Box(modifier = Modifier.weight(1f)) {
                                    MapLocationCard(
                                        title = "Focus: ${twister.soundFocus.uppercase()}",
                                        subtitle = twister.text,
                                        score = if (twister.maxAccuracy > 0) twister.maxAccuracy else null,
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
                                        title = "Focus: ${twister.soundFocus.uppercase()}",
                                        subtitle = twister.text,
                                        score = if (twister.maxAccuracy > 0) twister.maxAccuracy else null,
                                        isCompleted = isCompleted,
                                        isActive = isActive,
                                        accentColor = PolishTeal
                                    )
                                }
                            }
                        }

                        if (index < twisters.size - 1) {
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
                    title = {
                        Text(
                            text = "Tongue Twister Soundroom",
                            fontWeight = FontWeight.Bold,
                            color = PolishNavy
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (currentIndex in twisters.indices) {
                                scrollToTargetIndex = currentIndex
                            }
                            viewModel.activeTongueTwister.value = null
                            viewModel.lastScore.value = null
                            viewModel.scoredWords.value = emptyList()
                            viewModel.recognizedText.value = ""
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
            if (twisters.isNotEmpty()) {
                val twister = twisters.getOrNull(currentIndex) ?: twisters.first()
                val mascotState = when {
                    isRecording -> MascotState.LISTENING
                    lastScore != null && lastScore!! >= 75 -> MascotState.CELEBRATING
                    lastScore != null -> MascotState.ENCOURAGING
                    else -> MascotState.IDLE
                }
                val coachMessage = when {
                    isProcessingSpeech -> "Checking articulation..."
                    isRecording -> "Speak clearly & keep the rhythm!"
                    lastScore != null && lastScore!! >= 75 -> "Crisp articulation! ${lastScore}%"
                    lastScore != null -> "Try Slow pace once, then speed up!"
                    else -> "Check the Mouth Guide, then tap the orb"
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(PolishBg)
                        .padding(innerPadding)
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Headline Carousel tracker
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CHALLENGE ${currentIndex + 1} OF ${twisters.size}",
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
                                    text = "Focus: ${twister.soundFocus.uppercase()}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Orange600
                                    )
                                )
                            }
                        }
                    }

                    // Tongue Twister core text display Card
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Slate100),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "SPEED DRILL",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Slate500,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp
                                )
                            )
                            val displayTwisterText = remember(twister.text) {
                                SpokenTextNormalizer.normalizeToSpokenWords(twister.text)
                            }
                            Text(
                                text = displayTwisterText,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = PolishNavy,
                                    lineHeight = 32.sp
                                ),
                                modifier = Modifier.clickable { viewModel.speak(twister.text) }
                            )
                            if (twister.description.isNotEmpty()) {
                                Text(
                                    text = twister.description,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Slate500,
                                        textAlign = TextAlign.Center
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Playback Guide Speed Toggles
                            Text(
                                text = "HEAR AT YOUR PACE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Slate500,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val speeds = listOf(
                                    Triple("Slow", 0.6f, Icons.AutoMirrored.Filled.DirectionsWalk),
                                    Triple("Normal", 1.0f, Icons.AutoMirrored.Filled.DirectionsRun),
                                    Triple("Fast", 1.4f, Icons.Default.FlashOn)
                                )
                                speeds.forEach { (label, rate, icon) ->
                                    val active = selectedSpeed == rate
                                    FilterChip(
                                        selected = active,
                                        onClick = {
                                            viewModel.setPlaybackSpeed(rate)
                                            viewModel.speak(twister.text)
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = label,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = label,
                                                fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = Slate50,
                                            labelColor = Slate500,
                                            iconColor = Slate500,
                                            selectedContainerColor = PolishTeal,
                                            selectedLabelColor = Color.White,
                                            selectedLeadingIconColor = Color.White
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            enabled = true,
                                            selected = active,
                                            borderColor = Slate200
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                FilledTonalButton(
                                    onClick = { viewModel.speak(twister.text) },
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = if (isTtsSpeaking) Red500 else PolishTealBg,
                                        contentColor = if (isTtsSpeaking) Color.White else PolishTeal
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isTtsSpeaking) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = "Hear Drill",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isTtsSpeaking) "Stop Audio" else "Hear Model",
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
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Face,
                                        contentDescription = "Mouth Guide",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Mouth Guide", fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    // --- MIC CONTROLS & DRILL ATTEMPT TRACKER ---
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
                                size = 70.dp
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Play voice
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
                                        contentDescription = "UserVoice",
                                        tint = when {
                                            isPlayingBack -> Color.White
                                            lastScore != null -> PolishIndigoText
                                            else -> Slate400
                                        }
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                SonicVoiceOrb(
                                    isRecording = isRecording,
                                    audioLevel = liveAudioLevel,
                                    isProcessing = isProcessingSpeech,
                                    onClick = {
                                        if (isRecording) {
                                            viewModel.stopRecording(twister.text, "tongue_twister", twister.id)
                                        } else {
                                            viewModel.startRecording(twister.text, "tongue_twister")
                                        }
                                    },
                                    size = 62.dp,
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
                                    modifier = Modifier.testTag("twister_record_mic")
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                // Retry
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
                                    isRecording -> "Recording... Speed read now!"
                                    lastScore != null -> "Tap the orb to try again"
                                    else -> "Tap the orb and read the tongue twister aloud"
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

                            if (lastScore != null) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp)
                                ) {
                                    if (currentIndex < twisters.size - 1) {
                                        Button(
                                            onClick = {
                                                viewModel.activeTongueTwister.value = twisters[currentIndex + 1]
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
                                            Text("Next Lesson", color = Color.White, fontWeight = FontWeight.Bold)
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
                                            if (currentIndex in twisters.indices) {
                                                scrollToTargetIndex = currentIndex
                                            }
                                            viewModel.activeTongueTwister.value = null
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
                                                text = "Analyzing speed drill...",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = PolishTeal
                                                )
                                            )
                                            Text(
                                                text = "Evaluating pronunciation and phonetics...",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = Slate500
                                                )
                                            )
                                        }
                                    }
                                }
                            }

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
                                            text = if (isPassed) "Drill Passed (>= 75%)" else "Need 75% to Complete",
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
                        initialSound = resolvePhoneticGuideSound(twister.soundFocus),
                        onDismiss = { showMouthGuide = false },
                        onSpeakWord = { word -> viewModel.speak(word) }
                    )
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No tongue twisters loaded.", color = Slate500)
                }
            }
        }
    }
}
