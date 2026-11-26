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
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.EnglishViewModel
import com.example.ui.SpokenTextNormalizer
import com.example.ui.components.MascotCoach
import com.example.ui.components.MascotState
import com.example.ui.components.PronunciationFeedbackChips
import com.example.ui.components.SonicVoiceOrb
import com.example.ui.components.SpokenVoiceToTextCard
import com.example.ui.theme.*

private fun cleanParagraphTitle(title: String): String {
    return title
        .replace(Regex("(?i)\\bTED\\b|\\bTED Talk\\b|\\bTEDTalks?\\b"), "")
        .replace(Regex("(?i)\\bby\\s+[A-Za-z\\s.]+"), "")
        .replace(Regex("\\s+-\\s+.*$"), "")
        .replace(Regex("\\(.*?\\)"), "")
        .trim('-', ':', ' ', '|', '_', ',')
        .ifEmpty { "Paragraph Practice" }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParagraphScreen(
    viewModel: EnglishViewModel,
    modifier: Modifier = Modifier
) {
    val paragraph by viewModel.activeParagraph.collectAsState()
    val paragraphsList by viewModel.paragraphs.collectAsState()
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
    val paragraphProcessingStatus by viewModel.paragraphProcessingStatus.collectAsState()

    val mapListState = rememberLazyListState()
    var scrollToTargetIndex by remember { mutableStateOf<Int?>(null) }

    val currentParaIndex = remember(paragraph, paragraphsList) {
        if (paragraph == null) {
            -1
        } else {
            paragraphsList.indexOfFirst { it.id == paragraph?.id }
        }
    }
    val hasNextPara = currentParaIndex in 0 until (paragraphsList.size - 1)
    val nextPara = if (hasNextPara) paragraphsList[currentParaIndex + 1] else null

    val parasCompletedCount = remember(paragraphsList) { paragraphsList.count { it.isCompleted } }

    // Auto-scroll map to next uncompleted paragraph or explicit target
    LaunchedEffect(paragraph == null, parasCompletedCount, scrollToTargetIndex) {
        if (paragraph == null && paragraphsList.isNotEmpty()) {
            val targetIdx = scrollToTargetIndex
                ?: paragraphsList.indexOfFirst { !it.isCompleted }.takeIf { it >= 0 }
                ?: (paragraphsList.size - 1)
            if (targetIdx in paragraphsList.indices) {
                mapListState.animateScrollToItem(targetIdx)
            }
            scrollToTargetIndex = null
        }
    }

    // Intercept back button when practicing paragraph to return to that paragraph on the map
    BackHandler(enabled = (paragraph != null)) {
        viewModel.cancelParagraphProcessing()
        if (currentParaIndex in paragraphsList.indices) {
            scrollToTargetIndex = currentParaIndex
        }
        viewModel.activeParagraph.value = null
        viewModel.lastScore.value = null
        viewModel.scoredWords.value = emptyList()
        viewModel.recognizedText.value = ""
    }

    if (paragraph == null) {
        var searchQuery by rememberSaveable { mutableStateOf("") }
        val selectedLevel by viewModel.paragraphSelectedLevel.collectAsState()

        val filteredList = remember(paragraphsList, searchQuery, selectedLevel) {
            paragraphsList.filter { item ->
                val matchesSearch = searchQuery.isBlank() ||
                    item.title.contains(searchQuery, ignoreCase = true) ||
                    item.text.contains(searchQuery, ignoreCase = true)
                val matchesLevel = selectedLevel == "All" ||
                    item.level.equals(selectedLevel, ignoreCase = true) ||
                    (selectedLevel.equals("Basic", ignoreCase = true) && item.level.equals("Beginner", ignoreCase = true))
                matchesSearch && matchesLevel
            }
        }

        // --- MAP VIEW OF PARAGRAPHS ---
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Paragraph Map (${paragraphsList.size})",
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
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(PolishTealBg)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = "Map Guidance",
                                tint = PolishTeal,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "LITERATURE VALLEY (${paragraphsList.size} Checkpoints)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PolishTeal,
                                    letterSpacing = 0.8.sp
                                )
                            )
                            Text(
                                text = "Train long-form reading fluency across speeches, literature, and essays.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Slate500
                                )
                            )
                        }
                    }
                }

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search ${paragraphsList.size} paragraphs...", fontSize = 14.sp, color = Slate400) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Slate400,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(20.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = PolishTeal,
                        unfocusedBorderColor = Slate200
                    )
                )

                // Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("All", "Basic", "Intermediate", "Advanced").forEach { lvl ->
                        val selected = selectedLevel == lvl
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.paragraphSelectedLevel.value = lvl },
                            label = {
                                Text(
                                    text = lvl,
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color.White,
                                labelColor = Slate500,
                                selectedContainerColor = PolishTeal,
                                selectedLabelColor = Color.White
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selected,
                                borderColor = Slate200
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }

                // Map Path
                LazyColumn(
                    state = mapListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 48.dp, start = 16.dp, end = 16.dp)
                ) {
                    itemsIndexed(filteredList) { index, para ->
                        val isCompleted = para.isCompleted
                        val isFirstUncompleted = filteredList.indexOfFirst { !it.isCompleted } == index
                        val isActive = isFirstUncompleted || (filteredList.all { it.isCompleted } && index == 0)
                        val isLeft = index % 2 == 0

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.activeParagraph.value = para
                                    viewModel.lastScore.value = null
                                    viewModel.scoredWords.value = emptyList()
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isLeft) {
                                Box(modifier = Modifier.weight(1f)) {
                                    MapLocationCard(
                                        title = cleanParagraphTitle(para.title),
                                        subtitle = para.text,
                                        score = if (para.maxAccuracy > 0) para.maxAccuracy else null,
                                        isCompleted = isCompleted,
                                        isActive = isActive,
                                        accentColor = PolishTeal
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                MapPinCircle(
                                    isCompleted = isCompleted,
                                    isActive = isActive,
                                    accentColor = PolishTeal,
                                    number = para.id
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Box(modifier = Modifier.weight(1f))
                            } else {
                                Box(modifier = Modifier.weight(1f))
                                Spacer(modifier = Modifier.width(12.dp))
                                MapPinCircle(
                                    isCompleted = isCompleted,
                                    isActive = isActive,
                                    accentColor = PolishTeal,
                                    number = para.id
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Box(modifier = Modifier.weight(1f)) {
                                    MapLocationCard(
                                        title = cleanParagraphTitle(para.title),
                                        subtitle = para.text,
                                        score = if (para.maxAccuracy > 0) para.maxAccuracy else null,
                                        isCompleted = isCompleted,
                                        isActive = isActive,
                                        accentColor = PolishTeal
                                    )
                                }
                            }
                        }

                        if (index < filteredList.size - 1) {
                            MapConnector()
                        }
                    }
                }
            }
        }
    } else {
        // --- PRACTICE VIEW ---
        val item = paragraph
        if (item != null) {
            val practiceScrollState = rememberScrollState()

            LaunchedEffect(lastScore) {
                if (lastScore != null) {
                    practiceScrollState.animateScrollTo(practiceScrollState.maxValue)
                }
            }

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = "Paragraph Fluency",
                                fontWeight = FontWeight.Bold,
                                color = PolishNavy
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                viewModel.cancelParagraphProcessing()
                                if (currentParaIndex in paragraphsList.indices) {
                                    scrollToTargetIndex = currentParaIndex
                                }
                                viewModel.activeParagraph.value = null
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(PolishBg)
                        .padding(innerPadding)
                ) {
                    // 1. TOPIC TITLE ROW
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = cleanParagraphTitle(item.title),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = PolishNavy
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = PolishTealBg,
                            border = BorderStroke(1.dp, PolishTealLightBorder)
                        ) {
                            Text(
                                text = "Est: ${item.estimatedReadingTime}s",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PolishTeal
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    // 2. UNIFIED SCROLLABLE PAGE CONTENT
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(practiceScrollState)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Paragraph Text Box
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Slate100),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                val displayParagraphText = remember(item.text) {
                                    SpokenTextNormalizer.normalizeToSpokenWords(item.text)
                                }
                                Text(
                                    text = displayParagraphText,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        lineHeight = 28.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = PolishNavy,
                                        letterSpacing = 0.3.sp
                                    ),
                                    modifier = Modifier.clickable { viewModel.speak(item.text) }
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                FilledTonalButton(
                                    onClick = { viewModel.speak(item.text) },
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = if (isTtsSpeaking) Red500 else PolishIndigoBg,
                                        contentColor = if (isTtsSpeaking) Color.White else PolishIndigoText
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .align(Alignment.CenterHorizontally)
                                        .height(48.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isTtsSpeaking) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = "Listen",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isTtsSpeaking) "Stop Model Guide" else "Play Paragraph Model Guide",
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        // Practice Instruction
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = PolishAmberBg),
                            border = BorderStroke(1.dp, Orange100),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                    contentDescription = "Fluency Tip",
                                    tint = Orange600
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "FLUENCY & INTONATION TIP",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Orange600
                                        )
                                    )
                                    Text(
                                        text = "Take a breath at punctuation marks. Try to glide smoothly from word to word without artificial pauses.",
                                        style = MaterialTheme.typography.bodySmall.copy(color = PolishNavy)
                                    )
                                }
                            }
                        }

                        // AUDIO PROCESSING NOTIFICATION BANNER
                        AnimatedVisibility(
                            visible = isProcessingSpeech,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = PolishTealBg
                                ),
                                border = BorderStroke(1.dp, PolishTealLightBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(28.dp),
                                        color = PolishTeal,
                                        strokeWidth = 3.dp
                                    )
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Analyzing your speech...",
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = PolishTeal
                                            )
                                        )
                                        Text(
                                            text = paragraphProcessingStatus ?: "Transcribing audio and computing your fluency score...",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Slate500
                                            )
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    FilledTonalButton(
                                        onClick = { viewModel.cancelParagraphProcessing() },
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = Color(0xFFFEE2E2),
                                            contentColor = Red500
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                        modifier = Modifier
                                            .heightIn(min = 48.dp)
                                            .testTag("paragraph_stop_analysis_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Stop,
                                            contentDescription = "Stop analyzing",
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Stop",
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // Real-time Voice to Text Display
                        if (isRecording || isProcessingSpeech || lastScore != null || recognizedText.isNotBlank()) {
                            SpokenVoiceToTextCard(
                                isRecording = isRecording,
                                recognizedText = recognizedText,
                                hasEvaluated = lastScore != null
                            )
                        }

                        // Scoring Output Card
                        lastScore?.let { score ->
                            Card(
                                shape = RoundedCornerShape(22.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Slate100),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            MascotCoach(
                                                state = if (score >= 60) MascotState.CELEBRATING else MascotState.ENCOURAGING,
                                                size = 54.dp
                                            )
                                            Column {
                                                Text(
                                                    text = "READING FLUENCY",
                                                    style = MaterialTheme.typography.labelMedium.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = Slate500
                                                    )
                                                )
                                                Text(
                                                    text = if (score >= 60) "Passed (>= 60%)" else "Keep Practicing",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = if (score >= 60) Emerald600 else Orange600,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                )
                                            }
                                        }
                                        Text(
                                            text = "$score%",
                                            style = MaterialTheme.typography.headlineMedium.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                color = when {
                                                    score >= 75 -> Emerald600
                                                    score >= 60 -> Orange600
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

                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // 3. DOCKED BOTTOM RECORDING BAR
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White,
                        shadowElevation = 10.dp,
                        border = BorderStroke(1.dp, Slate100),
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "RECORD YOUR ATTEMPT",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Slate500,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // User playback
                                IconButton(
                                    onClick = { viewModel.playRecordedVoice() },
                                    enabled = lastScore != null && !isProcessingSpeech,
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
                                        contentDescription = "Compare",
                                        tint = when {
                                            isPlayingBack -> Color.White
                                            lastScore != null -> PolishIndigoText
                                            else -> Slate400
                                        }
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                SonicVoiceOrb(
                                    isRecording = isRecording,
                                    audioLevel = liveAudioLevel,
                                    isProcessing = isProcessingSpeech,
                                    onClick = {
                                        if (isRecording) {
                                            viewModel.stopRecording(item.text, "paragraph", item.id)
                                        } else {
                                            viewModel.startRecording(item.text, "paragraph")
                                        }
                                    },
                                    size = 54.dp,
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
                                    modifier = Modifier.testTag("paragraph_record_mic")
                                )

                                Spacer(modifier = Modifier.width(14.dp))

                                // Reset (or Stop Analyzing while processing)
                                IconButton(
                                    onClick = {
                                        if (isProcessingSpeech) {
                                            viewModel.cancelParagraphProcessing()
                                        } else {
                                            viewModel.lastScore.value = null
                                            viewModel.scoredWords.value = emptyList()
                                            viewModel.recognizedText.value = ""
                                        }
                                    },
                                    enabled = isProcessingSpeech || lastScore != null,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(
                                            when {
                                                isProcessingSpeech -> Color(0xFFFEE2E2)
                                                lastScore != null -> Slate100
                                                else -> Slate50
                                            },
                                            CircleShape
                                        )
                                        .testTag(if (isProcessingSpeech) "paragraph_dock_stop_analysis" else "paragraph_reset_button")
                                ) {
                                    Icon(
                                        imageVector = if (isProcessingSpeech) Icons.Default.Stop else Icons.Default.Refresh,
                                        contentDescription = if (isProcessingSpeech) "Stop analyzing" else "Reset",
                                        tint = when {
                                            isProcessingSpeech -> Red500
                                            lastScore != null -> PolishNavy
                                            else -> Slate400
                                        }
                                    )
                                }
                            }

                            Text(
                                text = when {
                                    isProcessingSpeech -> paragraphProcessingStatus?.let { "$it Tap Stop to cancel" }
                                        ?: "Analyzing your audio... Tap Stop to cancel"
                                    isRecording -> "Recording... Tap the orb to evaluate"
                                    lastScore != null -> "Tap the orb to practice again"
                                    else -> "Tap the orb and read the paragraph aloud"
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
                                    if (hasNextPara && nextPara != null) {
                                        Button(
                                            onClick = {
                                                viewModel.activeParagraph.value = nextPara
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
                                            if (currentParaIndex in paragraphsList.indices) {
                                                scrollToTargetIndex = currentParaIndex
                                            }
                                            viewModel.activeParagraph.value = null
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
                                            text = if (hasNextPara) "Finish (Map)" else "Complete Trail",
                                            color = PolishNavy,
                                            fontWeight = FontWeight.SemiBold
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
                Text("No active paragraph practice selected.", color = Slate500)
            }
        }
    }
}