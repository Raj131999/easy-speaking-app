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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ParagraphSet
import com.example.ui.EnglishViewModel
import com.example.ui.WordScoreType
import com.example.ui.components.SpokenVoiceToTextCard
import com.example.ui.theme.StreakGold
import com.example.ui.theme.TealPrimary

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
    val isPlayingBack by viewModel.isPlayingBack.collectAsState()
    val isTtsSpeaking by viewModel.isTtsSpeaking.collectAsState()
    val lastScore by viewModel.lastScore.collectAsState()
    val scoredWords by viewModel.scoredWords.collectAsState()
    val recognizedText by viewModel.recognizedText.collectAsState()
    val isProcessingSpeech by viewModel.isProcessingSpeech.collectAsState()

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
            val targetIdx = scrollToTargetIndex ?: paragraphsList.indexOfFirst { !it.isCompleted }.takeIf { it >= 0 } ?: (paragraphsList.size - 1)
            if (targetIdx in paragraphsList.indices) {
                mapListState.animateScrollToItem(targetIdx)
            }
            scrollToTargetIndex = null
        }
    }

    // Intercept back button when practicing paragraph to return to that paragraph on the map
    BackHandler(enabled = (paragraph != null)) {
        if (currentParaIndex in paragraphsList.indices) {
            scrollToTargetIndex = currentParaIndex
        }
        viewModel.activeParagraph.value = null
        viewModel.lastScore.value = null
        viewModel.scoredWords.value = emptyList()
        viewModel.recognizedText.value = ""
    }

    if (paragraph == null) {
        var searchQuery by remember { mutableStateOf("") }
        var selectedLevel by remember { mutableStateOf("All") }

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
                    title = { Text("Paragraph Map (${paragraphsList.size})") },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.goBack() }) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = "Map Guidance",
                            tint = TealPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "LITERATURE VALLEY (${paragraphsList.size} Checkpoints)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TealPrimary
                                )
                            )
                            Text(
                                text = "Train long-form reading fluency across speeches, literature, and essays.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search 60 paragraphs...", fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", modifier = Modifier.size(20.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(20.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
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
                        FilterChip(
                            selected = selectedLevel == lvl,
                            onClick = { selectedLevel = lvl },
                            label = { Text(lvl, fontSize = 12.sp) },
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
                                        accentColor = TealPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                MapPinCircle(
                                    isCompleted = isCompleted,
                                    isActive = isActive,
                                    accentColor = TealPrimary,
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
                                    accentColor = TealPrimary,
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
                                        accentColor = TealPrimary
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

            // Smoothly auto-scroll down to show the results when score arrives
            LaunchedEffect(lastScore) {
                if (lastScore != null) {
                    practiceScrollState.animateScrollTo(practiceScrollState.maxValue)
                }
            }

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Paragraph Fluency") },
                        navigationIcon = {
                            IconButton(onClick = {
                                if (currentParaIndex in paragraphsList.indices) {
                                    scrollToTargetIndex = currentParaIndex
                                }
                                viewModel.activeParagraph.value = null
                                viewModel.lastScore.value = null
                                viewModel.scoredWords.value = emptyList()
                                viewModel.recognizedText.value = ""
                            }) {
                                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                    // 1. TOPIC TITLE ROW (Fixed header)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = cleanParagraphTitle(item.title),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.weight(1f)
                        )
                        SuggestionChip(
                            onClick = {},
                            label = { Text("Est: ${item.estimatedReadingTime}s") },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = TealPrimary.copy(alpha = 0.15f),
                                labelColor = TealPrimary
                            )
                        )
                    }

                    // 2. UNIFIED SCROLLABLE PAGE CONTENT
                    // Single scroll container for the paragraph, instructions, processing banner, You Spoke card, and results!
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
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = item.text,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        lineHeight = 28.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        letterSpacing = 0.5.sp
                                    ),
                                    modifier = Modifier.clickable { viewModel.speak(item.text) }
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                // Model control
                                Button(
                                    onClick = { viewModel.speak(item.text) },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (isTtsSpeaking) Color(0xFFFF5252) else MaterialTheme.colorScheme.secondary),
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                ) {
                                    Icon(imageVector = if (isTtsSpeaking) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Listen")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(if (isTtsSpeaking) "Stop Model Guide" else "Play Paragraph Model Guide")
                                }
                            }
                        }

                        // Practice Instruction
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.AutoMirrored.Filled.TrendingUp, contentDescription = "Fluency Tip", tint = StreakGold)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "FLUENCY & INTONATION TIP",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = StreakGold)
                                    )
                                    Text(
                                        text = "Take a breath at punctuation marks. Try to glide smoothly from word to word without artificial pauses.",
                                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                    containerColor = TealPrimary.copy(alpha = 0.12f)
                                ),
                                border = BorderStroke(1.5.dp, TealPrimary.copy(alpha = 0.7f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(28.dp),
                                        color = TealPrimary,
                                        strokeWidth = 3.dp
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Analyzing your speech...",
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = TealPrimary
                                            )
                                        )
                                        Text(
                                            text = "Transcribing audio and computing your fluency score. Please wait a moment...",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // Real-time Voice to Text Display (Shown during recording, processing or after evaluation)
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
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "READING FLUENCY:",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            )
                                            Text(
                                                text = if (score >= 60) "Passed (>= 60%)" else "Keep Practicing",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = if (score >= 60) Color(0xFF2ECC71) else StreakGold,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        }
                                        Text(
                                            text = "$score%",
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = when {
                                                    score >= 90 -> Color(0xFF2ECC71)
                                                    score >= 70 -> StreakGold
                                                    score >= 50 -> Color(0xFFFF9800)
                                                    else -> Color(0xFFFF5252)
                                                }
                                            )
                                        )
                                    }

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
                                                    .padding(2.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(color.copy(alpha = 0.1f))
                                                    .clickable { viewModel.speak(word) }
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
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

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // 3. DOCKED BOTTOM RECORDING BAR
                    // Always cleanly anchored at bottom of screen, easily reachable by thumb
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 8.dp,
                        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "RECORD YOUR ATTEMPT",
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
                                // User playback
                                IconButton(
                                    onClick = { viewModel.playRecordedVoice() },
                                    enabled = lastScore != null && !isProcessingSpeech,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(
                                            if (isPlayingBack) Color(0xFFFF5252) else MaterialTheme.colorScheme.secondary.copy(alpha = if (lastScore != null) 0.5f else 0.15f),
                                            CircleShape
                                        )
                                ) {
                                    Icon(
                                        imageVector = if (isPlayingBack) Icons.Default.Stop else Icons.Default.PlayArrow,
                                        contentDescription = "Compare",
                                        tint = if (lastScore != null) Color.White else Color.Gray
                                    )
                                }

                                Spacer(modifier = Modifier.width(20.dp))

                                // Large Mic Recorder
                                Box(contentAlignment = Alignment.Center) {
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
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
                                                    viewModel.stopRecording(item.text, "paragraph", item.id)
                                                } else {
                                                    viewModel.startRecording(item.text)
                                                }
                                            }
                                            .testTag("paragraph_record_mic"),
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
                                                modifier = Modifier.size(30.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(20.dp))

                                // Reset
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
                                        contentDescription = "Reset",
                                        tint = if (lastScore != null) Color.White else Color.Gray
                                    )
                                }
                            }

                            Text(
                                text = when {
                                    isProcessingSpeech -> "Analyzing your audio... Please wait"
                                    isRecording -> "Recording... Tap to stop and evaluate"
                                    lastScore != null -> "Tap Mic to practice again"
                                    else -> "Press Mic and read the paragraph aloud"
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
                                    if (hasNextPara && nextPara != null) {
                                        Button(
                                            onClick = {
                                                viewModel.activeParagraph.value = nextPara
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
                                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Lesson", tint = Color.Black)
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
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(if (hasNextPara) "Finish (Map)" else "Complete Reading Trail", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No active paragraph practice selected.")
            }
        }
    }
}