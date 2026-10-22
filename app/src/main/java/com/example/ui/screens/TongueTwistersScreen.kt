package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TongueTwister
import com.example.ui.EnglishViewModel
import com.example.ui.Screen
import com.example.ui.WordScoreType
import com.example.ui.components.SpokenVoiceToTextCard
import com.example.ui.theme.StreakGold
import com.example.ui.theme.TealPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TongueTwistersScreen(
    viewModel: EnglishViewModel,
    modifier: Modifier = Modifier
) {
    val twisters by viewModel.tongueTwisters.collectAsState()
    val activeTwister by viewModel.activeTongueTwister.collectAsState()

    val isRecording by viewModel.isRecording.collectAsState()
    val isPlayingBack by viewModel.isPlayingBack.collectAsState()
    val isTtsSpeaking by viewModel.isTtsSpeaking.collectAsState()
    val lastScore by viewModel.lastScore.collectAsState()
    val scoredWords by viewModel.scoredWords.collectAsState()
    val recognizedText by viewModel.recognizedText.collectAsState()
    val isProcessingSpeech by viewModel.isProcessingSpeech.collectAsState()

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
            val targetIdx = scrollToTargetIndex ?: twisters.indexOfFirst { !it.isCompleted && it.maxAccuracy < 75 }.takeIf { it >= 0 } ?: (twisters.size - 1)
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

    // Clear score when twister changes
    LaunchedEffect(activeTwister) {
        viewModel.lastScore.value = null
        viewModel.scoredWords.value = emptyList()
    }

    if (activeTwister == null) {
        // --- MAP VIEW OF TONGUE TWISTERS ---
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Tongue Twister Map") },
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
                                text = "DENTAL CHALLENGE PEAK",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TealPrimary
                                )
                            )
                            Text(
                                text = "Test your speed and speech articulation with our local tongue twisters. Complete checkpoints on the peak trail below!",
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
                                Box(modifier = Modifier.weight(1f))
                            } else {
                                Box(modifier = Modifier.weight(1f))
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
                                        title = "Focus: ${twister.soundFocus.uppercase()}",
                                        subtitle = twister.text,
                                        score = if (twister.maxAccuracy > 0) twister.maxAccuracy else null,
                                        isCompleted = isCompleted,
                                        isActive = isActive,
                                        accentColor = TealPrimary
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
                    title = { Text("Tongue Twister Soundroom") },
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
            if (twisters.isNotEmpty()) {
                val twister = twisters.getOrNull(currentIndex) ?: twisters.first()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
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
                            color = TealPrimary,
                            letterSpacing = 1.2.sp
                        )
                    )
                    SuggestionChip(
                        onClick = {},
                        label = { Text("Sound Focus: ${twister.soundFocus.uppercase()}") },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = StreakGold.copy(alpha = 0.15f),
                            labelColor = StreakGold
                        )
                    )
                }

                // Tongue Twister core text display Card
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "SPEED DRILL",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 1.2.sp
                            )
                        )
                        Text(
                            text = twister.text,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                lineHeight = 32.sp
                            ),
                            modifier = Modifier.clickable { viewModel.speak(twister.text) }
                        )
                        if (twister.description.isNotEmpty()) {
                            Text(
                                text = twister.description,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Playback Guide Speed Toggles
                        Text(
                            text = "SELECT SPEECH RATE:",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 1.sp
                            )
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val speeds = listOf(
                                Triple("Slow", 0.6f, Icons.Default.DirectionsWalk),
                                Triple("Normal", 1.0f, Icons.Default.DirectionsRun),
                                Triple("Fast", 1.4f, Icons.Default.FlashOn)
                            )
                            speeds.forEach { (label, rate, icon) ->
                                val active = viewModel.selectedSpeed.value == rate
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
                                    label = { Text(label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = TealPrimary,
                                        selectedLabelColor = Color.Black,
                                        selectedLeadingIconColor = Color.Black
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // --- MIC CONTROLS & DRILL ATTEMPT TRACKER ---
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "RECORD AND TRACK ATTEMPTS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
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
                                    .size(44.dp)
                                    .background(
                                        if (isPlayingBack) Color(0xFFFF5252) else MaterialTheme.colorScheme.secondary.copy(alpha = if (lastScore != null) 0.5f else 0.15f),
                                        CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = if (isPlayingBack) Icons.Default.Stop else Icons.Default.PlayArrow,
                                    contentDescription = "UserVoice",
                                    tint = if (lastScore != null) Color.White else Color.Gray
                                )
                            }

                            Spacer(modifier = Modifier.width(20.dp))

                            // Large Mic
                            Box(contentAlignment = Alignment.Center) {
                                Box(
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isProcessingSpeech -> StreakGold
                                                isRecording -> Color(0xFFFF5252)
                                                else -> TealPrimary
                                            }
                                        )
                                        .clickable(enabled = !isProcessingSpeech) {
                                            if (isRecording) {
                                                viewModel.stopRecording(twister.text, "tongue_twister", twister.id)
                                            } else {
                                                viewModel.startRecording(twister.text, "tongue_twister")
                                            }
                                        }
                                        .testTag("twister_record_mic"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isProcessingSpeech) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(28.dp),
                                            color = Color.Black,
                                            strokeWidth = 3.dp
                                        )
                                    } else {
                                        Icon(
                                            imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                                            contentDescription = "Mic",
                                            tint = Color.Black,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(20.dp))

                            // Retry
                            IconButton(
                                onClick = {
                                    viewModel.lastScore.value = null
                                    viewModel.scoredWords.value = emptyList()
                                    viewModel.recognizedText.value = ""
                                },
                                enabled = lastScore != null && !isProcessingSpeech,
                                modifier = Modifier
                                    .size(44.dp)
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
                                isRecording -> "Recording... Speed read now!"
                                lastScore != null -> "Tap Mic to try again"
                                else -> "Press Mic and read the tongue twister fast"
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
                                if (currentIndex < twisters.size - 1) {
                                    Button(
                                        onClick = {
                                            viewModel.activeTongueTwister.value = twisters[currentIndex + 1]
                                            viewModel.lastScore.value = null
                                            viewModel.scoredWords.value = emptyList()
                                            viewModel.recognizedText.value = ""
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Next Lesson", color = Color.Black, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Next Lesson", tint = Color.Black)
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
                                            text = "Analyzing speed drill...",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = TealPrimary
                                            )
                                        )
                                        Text(
                                            text = "Evaluating pronunciation and phonetics...",
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
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                    Text(
                                        text = if (isPassed) "Drill Passed (>= 75%)" else "Need 75% to Complete",
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

                            // Interactive words
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
                                            .padding(3.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(color.copy(alpha = 0.12f))
                                            .clickable { viewModel.speak(word) }
                                            .padding(horizontal = 6.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = word,
                                            color = color,
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
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
                Text("No tongue twisters loaded.")
            }
        }
    }
}
}
