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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.EnglishViewModel
import com.example.ui.components.MascotCoach
import com.example.ui.components.MascotState
import com.example.ui.components.PronunciationFeedbackChips
import com.example.ui.components.SonicVoiceOrb
import com.example.ui.components.SpokenVoiceToTextCard
import com.example.ui.components.Trophy3DBadge
import com.example.ui.theme.*

private fun cleanConversationTitle(title: String): String {
    return title
        .replace(Regex("(?i)^Topic\\s+\\d+\\s*:\\s*"), "")
        .trim('-', ':', ' ')
        .ifEmpty { "Conversation Practice" }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(
    viewModel: EnglishViewModel,
    modifier: Modifier = Modifier
) {
    val conv by viewModel.activeConversation.collectAsState()
    val conversationsList by viewModel.conversations.collectAsState()
    val currentIndex by viewModel.activeConversationIndex.collectAsState()
    val userRole by viewModel.userConversationRole.collectAsState()

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

    val listState = rememberLazyListState()
    val mapListState = rememberLazyListState()

    var showQuiz by remember { mutableStateOf(false) }
    var selectedQuizAnswer by remember { mutableStateOf<String?>(null) }
    var showQuizFeedback by remember { mutableStateOf(false) }
    var selectedLevel by remember(conv) { mutableStateOf("Basic") }
    var showLevelCompleteDialog by remember { mutableStateOf(false) }
    var completedLevelName by remember { mutableStateOf("") }
    var completedLevelXp by remember { mutableStateOf(0) }
    var scrollToTargetIndex by remember { mutableStateOf<Int?>(null) }

    val activeDialogueJson = remember(conv, selectedLevel) {
        conv?.let { item ->
            when (selectedLevel) {
                "Intermediate" -> item.intermediateDialogueJson.ifEmpty { item.dialogueJson }
                "Advanced" -> item.advancedDialogueJson.ifEmpty { item.intermediateDialogueJson.ifEmpty { item.dialogueJson } }
                else -> item.dialogueJson
            }
        } ?: ""
    }

    val dialogueLines = remember(activeDialogueJson) {
        if (activeDialogueJson.isNotEmpty()) viewModel.parseDialogueJson(activeDialogueJson) else emptyList()
    }

    val currentConvIndex = remember(conv, conversationsList) {
        if (conv == null) -1 else conversationsList.indexOfFirst { it.id == conv!!.id }
    }

    val conversationsCompletedCount = remember(conversationsList) {
        conversationsList.count { it.isCompleted }
    }

    LaunchedEffect(conv == null, conversationsCompletedCount, scrollToTargetIndex) {
        if (conv == null && conversationsList.isNotEmpty()) {
            val targetIdx = scrollToTargetIndex
                ?: conversationsList.indexOfFirst { !it.isCompleted }.takeIf { it >= 0 }
                ?: (conversationsList.size - 1)
            if (targetIdx in conversationsList.indices) {
                mapListState.animateScrollToItem(targetIdx)
            }
            scrollToTargetIndex = null
        }
    }

    BackHandler(enabled = (conv != null)) {
        if (showQuiz) {
            showQuiz = false
            showQuizFeedback = false
            selectedQuizAnswer = null
        } else {
            if (currentConvIndex >= 0) {
                scrollToTargetIndex = currentConvIndex
            }
            viewModel.activeConversation.value = null
            viewModel.lastScore.value = null
            viewModel.scoredWords.value = emptyList()
            viewModel.recognizedText.value = ""
        }
    }

    LaunchedEffect(currentIndex, dialogueLines.size) {
        if (dialogueLines.isNotEmpty()) {
            listState.animateScrollToItem(currentIndex)
        }
    }

    LaunchedEffect(conv) {
        showQuiz = false
        selectedQuizAnswer = null
        showQuizFeedback = false
        showLevelCompleteDialog = false
        completedLevelName = ""
        completedLevelXp = 0
    }

    if (conv == null) {
        // --- MAP VIEW OF CONVERSATIONS ---
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Conversation Map",
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
                                text = "CONVERSATIONAL DISTRICT",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PolishTeal,
                                    letterSpacing = 0.8.sp
                                )
                            )
                            Text(
                                text = "Practice real-life roleplays across 3 difficulty levels and pass the comprehension check.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Slate500
                                )
                            )
                        }
                    }
                }

                LazyColumn(
                    state = mapListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 48.dp, start = 16.dp, end = 16.dp)
                ) {
                    itemsIndexed(conversationsList) { index, conversation ->
                        val isCompleted = conversation.isCompleted
                        val completedCount = listOf(
                            conversation.isBasicCompleted,
                            conversation.isIntermediateCompleted,
                            conversation.isAdvancedCompleted
                        ).count { it }
                        val isFirstUncompleted = conversationsList.indexOfFirst { !it.isCompleted } == index
                        val isActive = isFirstUncompleted || (conversationsList.all { it.isCompleted } && index == 0)
                        val statusText = when {
                            isCompleted -> "Mastered (3/3 Levels)"
                            completedCount > 0 -> "In Progress ($completedCount/3 Levels)"
                            else -> "0/3 Levels Completed"
                        }
                        val isLeft = index % 2 == 0

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectConversation(conversation)
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isLeft) {
                                Box(modifier = Modifier.weight(1f)) {
                                    MapLocationCard(
                                        title = cleanConversationTitle(conversation.title),
                                        subtitle = conversation.scenario,
                                        score = if (isCompleted) 100 else if (completedCount > 0) (completedCount * 100 / 3) else null,
                                        isCompleted = isCompleted,
                                        isActive = isActive,
                                        accentColor = PolishTeal,
                                        customStatusText = statusText
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
                                        title = cleanConversationTitle(conversation.title),
                                        subtitle = conversation.scenario,
                                        score = if (isCompleted) 100 else if (completedCount > 0) (completedCount * 100 / 3) else null,
                                        isCompleted = isCompleted,
                                        isActive = isActive,
                                        accentColor = PolishTeal,
                                        customStatusText = statusText
                                    )
                                }
                            }
                        }

                        if (index < conversationsList.size - 1) {
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
                            text = "Roleplay Dialogue",
                            fontWeight = FontWeight.Bold,
                            color = PolishNavy
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (showQuiz) {
                                showQuiz = false
                                showQuizFeedback = false
                                selectedQuizAnswer = null
                            } else {
                                if (currentConvIndex >= 0) {
                                    scrollToTargetIndex = currentConvIndex
                                }
                                viewModel.activeConversation.value = null
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
                    actions = {
                        FilledTonalButton(
                            onClick = { viewModel.toggleRole() },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = PolishTealBg,
                                contentColor = PolishTeal
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Switch Role",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Role: $userRole",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
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
            conv?.let { item ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(PolishBg)
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    // Scenario Context Banner
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Slate100),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = "Scenario",
                                    tint = Orange600,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = cleanConversationTitle(item.title).uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Orange600,
                                        letterSpacing = 0.8.sp
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.scenario,
                                style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Level Selector Tabs (Basic, Intermediate, Advanced)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                    ) {
                        listOf("Basic", "Intermediate", "Advanced").forEach { lvl ->
                            val isLvlDone = when (lvl) {
                                "Basic" -> item.isBasicCompleted
                                "Intermediate" -> item.isIntermediateCompleted
                                "Advanced" -> item.isAdvancedCompleted
                                else -> false
                            }
                            val selected = selectedLevel == lvl

                            FilterChip(
                                selected = selected,
                                onClick = {
                                    if (selectedLevel != lvl) {
                                        selectedLevel = lvl
                                        viewModel.activeConversationIndex.value = 0
                                        showQuiz = false
                                        selectedQuizAnswer = null
                                        showQuizFeedback = false
                                        viewModel.lastScore.value = null
                                        viewModel.scoredWords.value = emptyList()
                                        viewModel.recognizedText.value = ""
                                        viewModel.triggerTtsForCurrentDialogue()
                                    }
                                },
                                leadingIcon = if (isLvlDone) {
                                    {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "$lvl Completed",
                                            tint = if (selected) Color.White else Emerald500,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                } else null,
                                label = {
                                    Text(
                                        text = lvl,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                shape = RoundedCornerShape(20.dp),
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
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (!showQuiz) {
                        // --- DIALOGUE SCROLL VIEW ---
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            itemsIndexed(dialogueLines) { index, line ->
                                val isActive = index == currentIndex
                                val isFuture = index > currentIndex
                                val isUserTurn = line.role == userRole

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .animateContentSize()
                                        .alpha(if (isFuture) 0.38f else 1.0f),
                                    horizontalArrangement = if (isUserTurn) Arrangement.End else Arrangement.Start
                                ) {
                                    if (!isUserTurn) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(PolishTealBg)
                                        ) {
                                            Text(
                                                text = line.speaker.take(1),
                                                fontWeight = FontWeight.Bold,
                                                color = PolishTeal
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }

                                    Card(
                                        shape = RoundedCornerShape(
                                            topStart = 18.dp,
                                            topEnd = 18.dp,
                                            bottomStart = if (isUserTurn) 18.dp else 4.dp,
                                            bottomEnd = if (isUserTurn) 4.dp else 18.dp
                                        ),
                                        colors = CardDefaults.cardColors(
                                            containerColor = when {
                                                isActive && isUserTurn -> PolishTealBg
                                                isActive -> Color.White
                                                isUserTurn -> Slate100
                                                else -> Color.White
                                            }
                                        ),
                                        border = BorderStroke(
                                            width = if (isActive) 1.5.dp else 1.dp,
                                            color = if (isActive) PolishTeal else Slate200
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth(0.8f)
                                            .clickable {
                                                if (!isUserTurn) {
                                                    viewModel.speak(line.text)
                                                }
                                            }
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = line.speaker,
                                                    style = MaterialTheme.typography.labelMedium.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isUserTurn) PolishTeal else Orange600
                                                    )
                                                )
                                                if (!isUserTurn) {
                                                    Icon(
                                                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                                        contentDescription = "Read",
                                                        tint = PolishTeal,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = line.text,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    color = PolishNavy
                                                )
                                            )
                                        }
                                    }

                                    if (isUserTurn) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(PolishIndigoBg)
                                        ) {
                                            Text(
                                                text = "Me",
                                                fontWeight = FontWeight.Bold,
                                                color = PolishIndigoText,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // --- SPEAKING PRACTICE / ADVANCE CONTROLS FOR ACTIVE INDEX ---
                        Spacer(modifier = Modifier.height(10.dp))

                        val activeLine = dialogueLines.getOrNull(currentIndex)
                        if (activeLine != null) {
                            val isUserTurn = activeLine.role == userRole

                            Card(
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Slate100),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateContentSize()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    if (isUserTurn) {
                                        Text(
                                            text = "YOUR TURN TO SPEAK ALOUD",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = PolishTeal,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.sp
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = activeLine.text,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center,
                                                color = PolishNavy
                                            )
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            // Play target example
                                            IconButton(
                                                onClick = { viewModel.speak(activeLine.text) },
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .background(
                                                        if (isTtsSpeaking) Red500 else PolishTealBg,
                                                        CircleShape
                                                    )
                                            ) {
                                                Icon(
                                                    imageVector = if (isTtsSpeaking) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                                                    contentDescription = if (isTtsSpeaking) "Stop" else "Hear Example",
                                                    tint = if (isTtsSpeaking) Color.White else PolishTeal
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(12.dp))

                                            SonicVoiceOrb(
                                                isRecording = isRecording,
                                                audioLevel = liveAudioLevel,
                                                isProcessing = isProcessingSpeech,
                                                onClick = {
                                                    if (isRecording) {
                                                        viewModel.stopRecording(activeLine.text, "conversation", item.id)
                                                    } else {
                                                        viewModel.startRecording(activeLine.text)
                                                    }
                                                },
                                                size = 56.dp,
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
                                                modifier = Modifier.testTag("conversation_record_mic")
                                            )

                                            Spacer(modifier = Modifier.width(12.dp))

                                            // Hear user voice
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
                                                    contentDescription = "Playback",
                                                    tint = when {
                                                        isPlayingBack -> Color.White
                                                        lastScore != null -> PolishIndigoText
                                                        else -> Slate400
                                                    }
                                                )
                                            }
                                        }

                                        Text(
                                            text = when {
                                                isProcessingSpeech -> "Analyzing your line... Please wait"
                                                isRecording -> "Listening... Tap the orb to finish"
                                                lastScore != null -> "Tap the orb to try again"
                                                else -> "Tap the orb to record your line"
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

                                        AnimatedVisibility(
                                            visible = isProcessingSpeech,
                                            enter = fadeIn() + expandVertically(),
                                            exit = fadeOut() + shrinkVertically()
                                        ) {
                                            Card(
                                                shape = RoundedCornerShape(12.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = PolishTealBg
                                                ),
                                                border = BorderStroke(1.dp, PolishTealLightBorder),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 6.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(12.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(22.dp),
                                                        color = PolishTeal,
                                                        strokeWidth = 2.5.dp
                                                    )
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    Column {
                                                        Text(
                                                            text = "Evaluating dialogue line...",
                                                            style = MaterialTheme.typography.bodySmall.copy(
                                                                fontWeight = FontWeight.Bold,
                                                                color = PolishTeal
                                                            )
                                                        )
                                                        Text(
                                                            text = "Checking pronunciation and phrasing...",
                                                            style = MaterialTheme.typography.labelSmall.copy(
                                                                color = Slate500
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))
                                        SpokenVoiceToTextCard(
                                            isRecording = isRecording,
                                            recognizedText = recognizedText,
                                            hasEvaluated = lastScore != null
                                        )

                                        lastScore?.let { score ->
                                            val isPassed = score >= 75
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(
                                                        text = "Pronunciation Score",
                                                        style = MaterialTheme.typography.labelMedium.copy(
                                                            color = Slate500,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    )
                                                    Text(
                                                        text = if (isPassed) "Line Passed (>= 75%)" else "Need 75% to pass line",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isPassed) Emerald600 else Orange600
                                                        )
                                                    )
                                                }
                                                Text(
                                                    text = "$score%",
                                                    style = MaterialTheme.typography.titleLarge.copy(
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = when {
                                                            score >= 75 -> Emerald600
                                                            score >= 50 -> Orange600
                                                            else -> Red500
                                                        }
                                                    )
                                                )
                                            }

                                            if (alignedWordTimings.isNotEmpty() || scoredWords.isNotEmpty()) {
                                                Spacer(modifier = Modifier.height(8.dp))
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

                                            Spacer(modifier = Modifier.height(12.dp))
                                            Button(
                                                onClick = {
                                                    if (currentIndex < dialogueLines.size - 1) {
                                                        viewModel.advanceDialogue(activeDialogueJson)
                                                        viewModel.lastScore.value = null
                                                        viewModel.scoredWords.value = emptyList()
                                                        viewModel.recognizedText.value = ""
                                                    } else {
                                                        if (item.comprehensionQuestion.isNotBlank()) {
                                                            showQuiz = true
                                                            viewModel.lastScore.value = null
                                                            viewModel.scoredWords.value = emptyList()
                                                            viewModel.recognizedText.value = ""
                                                        } else {
                                                            viewModel.completeConversationLevel(item.id, selectedLevel) { _, xp ->
                                                                completedLevelName = selectedLevel
                                                                completedLevelXp = xp
                                                                showLevelCompleteDialog = true
                                                                viewModel.lastScore.value = null
                                                                viewModel.scoredWords.value = emptyList()
                                                                viewModel.recognizedText.value = ""
                                                            }
                                                        }
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = PolishTeal,
                                                    contentColor = Color.White
                                                ),
                                                shape = RoundedCornerShape(14.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(48.dp)
                                            ) {
                                                Text(
                                                    text = if (currentIndex < dialogueLines.size - 1) {
                                                        "Continue Conversation"
                                                    } else if (item.comprehensionQuestion.isNotBlank()) {
                                                        "Continue to Quiz"
                                                    } else {
                                                        "Complete $selectedLevel Level"
                                                    },
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                    contentDescription = "Next",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    } else {
                                        // Partner's turn
                                        Text(
                                            text = "${activeLine.speaker.uppercase()} IS SPEAKING",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Orange600,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.sp
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = activeLine.text,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center,
                                                color = PolishNavy
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            FilledTonalButton(
                                                onClick = { viewModel.speak(activeLine.text) },
                                                colors = ButtonDefaults.filledTonalButtonColors(
                                                    containerColor = if (isTtsSpeaking) Red500 else PolishIndigoBg,
                                                    contentColor = if (isTtsSpeaking) Color.White else PolishIndigoText
                                                ),
                                                shape = RoundedCornerShape(14.dp),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(48.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (isTtsSpeaking) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                                                    contentDescription = "Hear",
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = if (isTtsSpeaking) "Stop Voice" else "Play Again",
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }

                                            Button(
                                                onClick = {
                                                    if (currentIndex < dialogueLines.size - 1) {
                                                        viewModel.advanceDialogue(activeDialogueJson)
                                                    } else {
                                                        if (item.comprehensionQuestion.isNotBlank()) {
                                                            showQuiz = true
                                                        } else {
                                                            viewModel.completeConversationLevel(item.id, selectedLevel) { _, xp ->
                                                                completedLevelName = selectedLevel
                                                                completedLevelXp = xp
                                                                showLevelCompleteDialog = true
                                                            }
                                                        }
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = PolishTeal,
                                                    contentColor = Color.White
                                                ),
                                                shape = RoundedCornerShape(14.dp),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(48.dp)
                                            ) {
                                                Text(
                                                    text = if (currentIndex < dialogueLines.size - 1) "Next Line" else if (item.comprehensionQuestion.isNotBlank()) "Quiz" else "Complete",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                    contentDescription = "Next",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Vocabulary Tip: ${item.vocabularyCallout}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = PolishTeal,
                                fontWeight = FontWeight.Medium
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        // --- COMPREHENSION QUIZ SCREEN ---
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Slate100),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(18.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .verticalScroll(rememberScrollState()),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(CircleShape)
                                            .background(PolishAmberBg)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Grade,
                                            contentDescription = "Quiz",
                                            tint = Orange600,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }

                                    Text(
                                        text = "Comprehension Check",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = PolishNavy
                                        )
                                    )

                                    Text(
                                        text = item.comprehensionQuestion,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Medium,
                                            textAlign = TextAlign.Center,
                                            color = PolishNavy
                                        ),
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )

                                    val parsedOptions = remember(item.comprehensionOptions) {
                                        item.comprehensionOptions.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                    }

                                    parsedOptions.forEach { option ->
                                        val isSelected = selectedQuizAnswer == option
                                        val isCorrect = option.equals(item.comprehensionAnswer.trim(), ignoreCase = true)

                                        val cardColor = when {
                                            showQuizFeedback && isCorrect -> Emerald50
                                            showQuizFeedback && isSelected && !isCorrect -> Red50
                                            isSelected -> PolishTealBg
                                            else -> PolishBg
                                        }

                                        val borderColor = when {
                                            showQuizFeedback && isCorrect -> Emerald500
                                            showQuizFeedback && isSelected && !isCorrect -> Red500
                                            isSelected -> PolishTeal
                                            else -> Slate200
                                        }

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(cardColor)
                                                .border(
                                                    width = if (isSelected || (showQuizFeedback && isCorrect)) 1.5.dp else 1.dp,
                                                    color = borderColor,
                                                    shape = RoundedCornerShape(14.dp)
                                                )
                                                .clickable {
                                                    if (!showQuizFeedback) {
                                                        selectedQuizAnswer = option
                                                    }
                                                }
                                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                RadioButton(
                                                    selected = isSelected,
                                                    onClick = {
                                                        if (!showQuizFeedback) {
                                                            selectedQuizAnswer = option
                                                        }
                                                    },
                                                    colors = RadioButtonDefaults.colors(
                                                        selectedColor = PolishTeal,
                                                        unselectedColor = Slate400
                                                    )
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = option,
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = PolishNavy
                                                )
                                                Spacer(modifier = Modifier.weight(1f))
                                                if (showQuizFeedback) {
                                                    if (isCorrect) {
                                                        Icon(
                                                            imageVector = Icons.Default.CheckCircle,
                                                            contentDescription = "Correct",
                                                            tint = Emerald500
                                                        )
                                                    } else if (isSelected) {
                                                        Icon(
                                                            imageVector = Icons.Default.Cancel,
                                                            contentDescription = "Incorrect",
                                                            tint = Red500
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    if (showQuizFeedback) {
                                        val userWasCorrect = selectedQuizAnswer?.equals(item.comprehensionAnswer.trim(), ignoreCase = true) == true
                                        Card(
                                            shape = RoundedCornerShape(14.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (userWasCorrect) Emerald50 else PolishAmberBg
                                            ),
                                            border = BorderStroke(1.dp, if (userWasCorrect) Emerald100 else Orange100),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 4.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = if (userWasCorrect) Icons.Default.CheckCircle else Icons.Default.Info,
                                                    contentDescription = null,
                                                    tint = if (userWasCorrect) Emerald600 else Orange600,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = if (userWasCorrect) "Excellent! Correct answer." else "Correct answer: ${item.comprehensionAnswer.trim()}",
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (userWasCorrect) Emerald600 else Orange600
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                val expectedXp = when (selectedLevel) {
                                    "Basic" -> 20
                                    "Intermediate" -> 25
                                    "Advanced" -> 35
                                    else -> 20
                                }

                                if (!showQuizFeedback) {
                                    Button(
                                        onClick = {
                                            if (selectedQuizAnswer != null) {
                                                showQuizFeedback = true
                                            }
                                        },
                                        enabled = selectedQuizAnswer != null,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = PolishTeal,
                                            contentColor = Color.White,
                                            disabledContainerColor = Slate200,
                                            disabledContentColor = Slate400
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Text(
                                            text = if (selectedQuizAnswer != null) "Check Answer & Continue" else "Select an Answer",
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (selectedQuizAnswer != null) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                contentDescription = "Submit",
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                } else {
                                    val userWasCorrect = selectedQuizAnswer?.equals(item.comprehensionAnswer.trim(), ignoreCase = true) == true
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        if (!userWasCorrect) {
                                            OutlinedButton(
                                                onClick = {
                                                    showQuizFeedback = false
                                                    selectedQuizAnswer = null
                                                },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(48.dp),
                                                shape = RoundedCornerShape(14.dp)
                                            ) {
                                                Text("Try Again")
                                            }
                                        }
                                        Button(
                                            onClick = {
                                                viewModel.completeConversationLevel(item.id, selectedLevel) { _, xp ->
                                                    completedLevelName = selectedLevel
                                                    completedLevelXp = xp
                                                    showLevelCompleteDialog = true
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = PolishTeal,
                                                contentColor = Color.White
                                            ),
                                            modifier = Modifier
                                                .weight(if (!userWasCorrect) 1.5f else 1f)
                                                .height(48.dp),
                                            shape = RoundedCornerShape(14.dp)
                                        ) {
                                            Text(
                                                text = "Complete (+$expectedXp XP)",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                contentDescription = "Finish",
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }

                                TextButton(
                                    onClick = {
                                        showQuiz = false
                                        showQuizFeedback = false
                                        selectedQuizAnswer = null
                                    },
                                    modifier = Modifier
                                        .align(Alignment.CenterHorizontally)
                                        .padding(top = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Review",
                                        modifier = Modifier.size(16.dp),
                                        tint = Slate500
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Review Conversation Dialogue",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                                    )
                                }
                            }
                        }
                    }
                }

                // Level Completion Dialog
                if (showLevelCompleteDialog) {
                    val currentItem = conversationsList.find { it.id == item.id } ?: item
                    val isBasicDone = currentItem.isBasicCompleted || completedLevelName == "Basic"
                    val isInterDone = currentItem.isIntermediateCompleted || completedLevelName == "Intermediate"
                    val isAdvDone = currentItem.isAdvancedCompleted || completedLevelName == "Advanced"
                    val isAllDone = isBasicDone && isInterDone && isAdvDone

                    val nextLevelToSuggest = when (completedLevelName) {
                        "Basic" -> if (!isInterDone) "Intermediate" else if (!isAdvDone) "Advanced" else null
                        "Intermediate" -> if (!isAdvDone) "Advanced" else if (!isBasicDone) "Basic" else null
                        "Advanced" -> if (!isBasicDone) "Basic" else if (!isInterDone) "Intermediate" else null
                        else -> null
                    }

                    AlertDialog(
                        onDismissRequest = {
                            showLevelCompleteDialog = false
                            viewModel.activeConversation.value = null
                        },
                        shape = RoundedCornerShape(24.dp),
                        containerColor = Color.White,
                        title = {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                MascotCoach(
                                    state = MascotState.CELEBRATING,
                                    coachMessage = if (isAllDone) "All 3 levels mastered!" else "$completedLevelName cleared!",
                                    size = 76.dp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (isAllDone) "Topic Mastered!" else "$completedLevelName Completed!",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        color = PolishNavy
                                    )
                                )
                            }
                        },
                        text = {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Trophy3DBadge(
                                    title = "$completedLevelName Mastery",
                                    xpReward = completedLevelXp,
                                    subtitle = "Dialogue Roleplay Complete"
                                )

                                Text(
                                    text = "Topic Completion Progress:",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = PolishNavy
                                    )
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf(
                                        Triple("Basic", isBasicDone, 20),
                                        Triple("Intermediate", isInterDone, 25),
                                        Triple("Advanced", isAdvDone, 35)
                                    ).forEach { (lvl, isDone, xp) ->
                                        Card(
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (isDone) Emerald50 else PolishBg
                                            ),
                                            border = BorderStroke(1.dp, if (isDone) Emerald500 else Slate200)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(8.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Icon(
                                                    imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                                    contentDescription = lvl,
                                                    tint = if (isDone) Emerald500 else Slate400,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = lvl,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 11.sp,
                                                        color = PolishNavy
                                                    )
                                                )
                                                Text(
                                                    text = if (isDone) "Done" else "${xp}XP",
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontSize = 10.sp,
                                                        color = if (isDone) Emerald600 else Slate500
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }

                                if (isAllDone) {
                                    Text(
                                        text = "All 3 levels completed! This conversation topic is now marked as mastered on your map.",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Slate500),
                                        textAlign = TextAlign.Center
                                    )
                                } else if (nextLevelToSuggest != null) {
                                    Text(
                                        text = "Complete Basic, Intermediate, and Advanced levels to master this entire topic.",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Slate500),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (nextLevelToSuggest != null && !isAllDone) {
                                    Button(
                                        onClick = {
                                            showLevelCompleteDialog = false
                                            selectedLevel = nextLevelToSuggest
                                            viewModel.activeConversationIndex.value = 0
                                            showQuiz = false
                                            selectedQuizAnswer = null
                                            showQuizFeedback = false
                                            viewModel.lastScore.value = null
                                            viewModel.scoredWords.value = emptyList()
                                            viewModel.recognizedText.value = ""
                                            viewModel.triggerTtsForCurrentDialogue()
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = PolishTeal,
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                    ) {
                                        Text(
                                            text = "Next Level: $nextLevelToSuggest",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                OutlinedButton(
                                    onClick = {
                                        showLevelCompleteDialog = false
                                        viewModel.activeConversation.value = null
                                        viewModel.lastScore.value = null
                                        viewModel.scoredWords.value = emptyList()
                                        viewModel.recognizedText.value = ""
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                ) {
                                    Text(
                                        text = if (isAllDone) "Return to Map" else "Save & Return to Map",
                                        fontWeight = FontWeight.SemiBold,
                                        color = PolishNavy
                                    )
                                }
                            }
                        }
                    )
                }
            } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No active conversation selected.", color = Slate500)
            }
        }
    }
}
