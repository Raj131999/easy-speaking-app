package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ConversationSet
import com.example.ui.DialogueLine
import com.example.ui.EnglishViewModel
import com.example.ui.Screen
import com.example.ui.WordScoreType
import com.example.ui.components.SpokenVoiceToTextCard
import com.example.ui.theme.CardSurfaceDark
import com.example.ui.theme.StreakGold
import com.example.ui.theme.TealPrimary

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
    val isPlayingBack by viewModel.isPlayingBack.collectAsState()
    val isTtsSpeaking by viewModel.isTtsSpeaking.collectAsState()
    val lastScore by viewModel.lastScore.collectAsState()
    val scoredWords by viewModel.scoredWords.collectAsState()
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

    // Auto-scroll map to next uncompleted conversation or explicit target
    LaunchedEffect(conv == null, conversationsCompletedCount, scrollToTargetIndex) {
        if (conv == null && conversationsList.isNotEmpty()) {
            val targetIdx = scrollToTargetIndex ?: conversationsList.indexOfFirst { !it.isCompleted }.takeIf { it >= 0 } ?: (conversationsList.size - 1)
            if (targetIdx in conversationsList.indices) {
                mapListState.animateScrollToItem(targetIdx)
            }
            scrollToTargetIndex = null
        }
    }

    // Intercept back button when practicing conversation to return to that conversation on the map
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

    // Auto-scroll list when the active index changes
    LaunchedEffect(currentIndex, dialogueLines.size) {
        if (dialogueLines.isNotEmpty()) {
            listState.animateScrollToItem(currentIndex)
        }
    }

    // Reset local state when the active conversation changes
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
                    title = { Text("Conversation Map") },
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
                                text = "CONVERSATIONAL DISTRICT",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TealPrimary
                                )
                            )
                            Text(
                                text = "Explore different real-life locations in town and practice active speaking roleplays. Complete quizzes to finish a location!",
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
                    itemsIndexed(conversationsList) { index, conversation ->
                        val isCompleted = conversation.isCompleted
                        val completedCount = listOf(conversation.isBasicCompleted, conversation.isIntermediateCompleted, conversation.isAdvancedCompleted).count { it }
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
                                        accentColor = TealPrimary,
                                        customStatusText = statusText
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
                                        title = cleanConversationTitle(conversation.title),
                                        subtitle = conversation.scenario,
                                        score = if (isCompleted) 100 else if (completedCount > 0) (completedCount * 100 / 3) else null,
                                        isCompleted = isCompleted,
                                        isActive = isActive,
                                        accentColor = TealPrimary,
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
                    title = { Text("Roleplay Dialogue") },
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
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        // Role Toggle Button
                        TextButton(
                            onClick = { viewModel.toggleRole() },
                            colors = ButtonDefaults.textButtonColors(contentColor = TealPrimary)
                        ) {
                            Icon(imageVector = Icons.Default.Sync, contentDescription = "Switch")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Role: User as $userRole")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            modifier = modifier.fillMaxSize()
        ) { innerPadding ->
            conv?.let { item ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                // Scenario Context Banner
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Place, contentDescription = "Scenario", tint = StreakGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = cleanConversationTitle(item.title).uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = StreakGold)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.scenario,
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

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

                        FilterChip(
                            selected = selectedLevel == lvl,
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
                                        tint = Color(0xFF2ECC71),
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
                                selectedContainerColor = TealPrimary.copy(alpha = 0.2f),
                                selectedLabelColor = TealPrimary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (!showQuiz) {
                    // --- DIALOGUE SCROLL VIEW ---
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(dialogueLines) { index, line ->
                            val isActive = index == currentIndex
                            val isPast = index < currentIndex
                            val isFuture = index > currentIndex

                            val isUserTurn = line.role == userRole

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateContentSize()
                                    .alpha(if (isFuture) 0.3f else 1.0f),
                                horizontalArrangement = if (isUserTurn) Arrangement.End else Arrangement.Start
                            ) {
                                if (!isUserTurn) {
                                    // Speaker Avatar Left
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(TealPrimary.copy(alpha = 0.2f))
                                    ) {
                                        Text(line.speaker.take(1), fontWeight = FontWeight.Bold, color = TealPrimary)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                }

                                Card(
                                    shape = RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = if (isUserTurn) 16.dp else 4.dp,
                                        bottomEnd = if (isUserTurn) 4.dp else 16.dp
                                    ),
                                    colors = CardDefaults.cardColors(
                                        containerColor = when {
                                            isActive && isUserTurn -> TealPrimary.copy(alpha = 0.15f)
                                            isActive -> MaterialTheme.colorScheme.surface
                                            isUserTurn -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                                            else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                                        }
                                    ),
                                    border = if (isActive) BorderStroke(1.5.dp, TealPrimary) else null,
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
                                                    color = if (isUserTurn) TealPrimary else StreakGold
                                                )
                                            )
                                            if (!isUserTurn) {
                                                Icon(
                                                    imageVector = Icons.Default.VolumeUp,
                                                    contentDescription = "Read",
                                                    tint = TealPrimary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = line.text,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                    }
                                }

                                if (isUserTurn) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    // User Avatar Right
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(StreakGold.copy(alpha = 0.2f))
                                    ) {
                                        Text("Me", fontWeight = FontWeight.Bold, color = StreakGold)
                                    }
                                }
                            }
                        }
                    }

                    // --- SPEAKING PRACTICE / ADVANCE CONTROLS FOR ACTIVE INDEX ---
                    Spacer(modifier = Modifier.height(16.dp))

                    val activeLine = dialogueLines.getOrNull(currentIndex)
                    if (activeLine != null) {
                        val isUserTurn = activeLine.role == userRole

                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                                        text = "YOUR TURN TO SPEAK ALOUD:",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = TealPrimary,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = activeLine.text,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Record controls
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        // Play target example
                                        IconButton(
                                            onClick = { viewModel.speak(activeLine.text) },
                                            modifier = Modifier
                                                .size(44.dp)
                                                .background(if (isTtsSpeaking) Color(0xFFFF5252) else MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f), CircleShape)
                                        ) {
                                            Icon(
                                                imageVector = if (isTtsSpeaking) Icons.Default.Stop else Icons.Default.VolumeUp,
                                                contentDescription = if (isTtsSpeaking) "Stop" else "Hear Example",
                                                tint = Color.White
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(20.dp))

                                        // Mic button
                                        IconButton(
                                            onClick = {
                                                if (isRecording) {
                                                    viewModel.stopRecording(activeLine.text, "conversation", item.id)
                                                } else {
                                                    viewModel.startRecording(activeLine.text)
                                                }
                                            },
                                            enabled = !isProcessingSpeech,
                                            modifier = Modifier
                                                .size(64.dp)
                                                .background(
                                                    when {
                                                        isProcessingSpeech -> StreakGold
                                                        isRecording -> Color(0xFFFF5252)
                                                        else -> TealPrimary
                                                    },
                                                    CircleShape
                                                )
                                                .testTag("conversation_record_mic")
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
                                                    contentDescription = if (isRecording) "Stop" else "Record",
                                                    tint = Color.Black,
                                                    modifier = Modifier.size(28.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(20.dp))

                                        // Hear user voice
                                        IconButton(
                                            onClick = { viewModel.playRecordedVoice() },
                                            enabled = lastScore != null && !isProcessingSpeech,
                                            modifier = Modifier
                                                .size(44.dp)
                                                .background(
                                                    if (isPlayingBack) Color(0xFFFF5252) else MaterialTheme.colorScheme.secondary.copy(alpha = if (lastScore != null) 0.4f else 0.1f),
                                                    CircleShape
                                                )
                                        ) {
                                            Icon(
                                                imageVector = if (isPlayingBack) Icons.Default.Stop else Icons.Default.PlayArrow,
                                                contentDescription = "Playback",
                                                tint = if (lastScore != null) Color.White else Color.Gray
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = when {
                                            isProcessingSpeech -> "Analyzing your line... Please wait"
                                            isRecording -> "Listening... Tap to end"
                                            lastScore != null -> "Tap Mic to try again"
                                            else -> "Tap Mic to record your line"
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
                                                        text = "Evaluating dialogue line...",
                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            color = TealPrimary
                                                        )
                                                    )
                                                    Text(
                                                        text = "Checking pronunciation and phrasing...",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Real-time Voice to Text Display
                                    Spacer(modifier = Modifier.height(8.dp))
                                    SpokenVoiceToTextCard(
                                        isRecording = isRecording,
                                        recognizedText = recognizedText,
                                        hasEvaluated = lastScore != null
                                    )

                                    // Display score feedback
                                    lastScore?.let { score ->
                                        val isPassed = score >= 75
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = "Pronunciation score: ",
                                                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                )
                                                Text(
                                                    text = if (isPassed) "Line Passed (>= 75%)" else "Need 75% to pass line",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isPassed) Color(0xFF2ECC71) else StreakGold
                                                    )
                                                )
                                            }
                                            Text(
                                                text = "$score%",
                                                style = MaterialTheme.typography.bodyMedium.copy(
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
                                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = if (currentIndex < dialogueLines.size - 1) "Perfect! Continue Conversation" else if (item.comprehensionQuestion.isNotBlank()) "Continue to Quiz" else "Complete $selectedLevel Level",
                                                color = Color.Black,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Next", tint = Color.Black)
                                        }
                                    }
                                } else {
                                    // Partner's turn (A or B)
                                    Text(
                                        text = "${activeLine.speaker} IS SPEAKING...",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = StreakGold,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = activeLine.text,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row {
                                        Button(
                                            onClick = { viewModel.speak(activeLine.text) },
                                            colors = ButtonDefaults.buttonColors(containerColor = if (isTtsSpeaking) Color(0xFFFF5252) else MaterialTheme.colorScheme.secondary)
                                        ) {
                                            Icon(imageVector = if (isTtsSpeaking) Icons.Default.Stop else Icons.Default.VolumeUp, contentDescription = "Hear")
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(if (isTtsSpeaking) "Stop Voice" else "Play Voice Again")
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
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
                                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                                        ) {
                                            Text(
                                                text = if (currentIndex < dialogueLines.size - 1) "Next Line" else if (item.comprehensionQuestion.isNotBlank()) "Quiz" else "Complete",
                                                color = Color.Black
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Next", tint = Color.Black)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Expander Vocab callout
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Vocabulary Tip: ${item.vocabularyCallout}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TealPrimary,
                            fontWeight = FontWeight.Medium
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    // --- COMPREHENSION QUIZ SCREEN ---
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            // Scrollable question, star, and options container
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
                                        .background(StreakGold.copy(alpha = 0.2f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Grade,
                                        contentDescription = "Quiz",
                                        tint = StreakGold,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }

                                Text(
                                    text = "Comprehension Check",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                )

                                Text(
                                    text = item.comprehensionQuestion,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Medium,
                                        textAlign = TextAlign.Center
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
                                        showQuizFeedback && isCorrect -> Color(0xFF2ECC71).copy(alpha = 0.15f)
                                        showQuizFeedback && isSelected && !isCorrect -> Color(0xFFE74C3C).copy(alpha = 0.15f)
                                        isSelected -> TealPrimary.copy(alpha = 0.12f)
                                        else -> MaterialTheme.colorScheme.background
                                    }

                                    val borderColor = when {
                                        showQuizFeedback && isCorrect -> Color(0xFF2ECC71)
                                        showQuizFeedback && isSelected && !isCorrect -> Color(0xFFE74C3C)
                                        isSelected -> TealPrimary
                                        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(cardColor)
                                            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
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
                                                }
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = option,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.weight(1f))
                                            if (showQuizFeedback) {
                                                if (isCorrect) {
                                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Correct", tint = Color(0xFF2ECC71))
                                                } else if (isSelected) {
                                                    Icon(imageVector = Icons.Default.Cancel, contentDescription = "Incorrect", tint = Color(0xFFE74C3C))
                                                }
                                            }
                                        }
                                    }
                                }

                                if (showQuizFeedback) {
                                    val userWasCorrect = selectedQuizAnswer?.equals(item.comprehensionAnswer.trim(), ignoreCase = true) == true
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (userWasCorrect) Color(0xFF2ECC71).copy(alpha = 0.12f) else Color(0xFFFF9800).copy(alpha = 0.12f)
                                        ),
                                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (userWasCorrect) Icons.Default.CheckCircle else Icons.Default.Info,
                                                contentDescription = null,
                                                tint = if (userWasCorrect) Color(0xFF2ECC71) else Color(0xFFFF9800),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = if (userWasCorrect) "Excellent! Correct answer." else "Correct answer: ${item.comprehensionAnswer.trim()}",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (userWasCorrect) Color(0xFF2ECC71) else Color(0xFFFF9800)
                                                )
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Fixed Docked Action Button Bar (ALWAYS visible!)
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
                                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = if (selectedQuizAnswer != null) "Check Answer & Continue" else "Select an Answer",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (selectedQuizAnswer != null) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Submit", tint = Color.Black)
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
                                            modifier = Modifier.weight(1f).height(48.dp),
                                            shape = RoundedCornerShape(12.dp)
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
                                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                                        modifier = Modifier.weight(if (!userWasCorrect) 1.5f else 1f).height(48.dp),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = "Complete Level (+$expectedXp XP)",
                                            color = Color.Black,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Finish", tint = Color.Black)
                                    }
                                }
                            }

                            // Secondary button: Return to dialogue
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
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Review",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Review Conversation Dialogue",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    containerColor = MaterialTheme.colorScheme.surface,
                    title = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(if (isAllDone) StreakGold.copy(alpha = 0.2f) else TealPrimary.copy(alpha = 0.2f))
                            ) {
                                Icon(
                                    imageVector = if (isAllDone) Icons.Default.EmojiEvents else Icons.Default.CheckCircle,
                                    contentDescription = "Complete",
                                    tint = if (isAllDone) StreakGold else TealPrimary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (isAllDone) "Topic Mastered!" else "$completedLevelName Level Completed!",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            )
                        }
                    },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = StreakGold.copy(alpha = 0.12f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "XP",
                                        tint = StreakGold,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "+$completedLevelXp XP Awarded",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = StreakGold
                                        )
                                    )
                                }
                            }

                            Text(
                                text = "Topic Completion Progress:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
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
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isDone) Color(0xFF2ECC71).copy(alpha = 0.12f) else MaterialTheme.colorScheme.background
                                        ),
                                        border = if (isDone) BorderStroke(1.dp, Color(0xFF2ECC71)) else null
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                             Icon(
                                                imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                                contentDescription = lvl,
                                                tint = if (isDone) Color(0xFF2ECC71) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = lvl,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp
                                                )
                                            )
                                            Text(
                                                text = if (isDone) "Done" else "${xp}XP",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontSize = 10.sp,
                                                    color = if (isDone) Color(0xFF2ECC71) else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            )
                                        }
                                    }
                                }
                            }

                            if (isAllDone) {
                                Text(
                                    text = "All 3 levels completed! This conversation topic is now fully marked as completed on your map.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                                    textAlign = TextAlign.Center
                                )
                            } else if (nextLevelToSuggest != null) {
                                Text(
                                    text = "Complete Basic, Intermediate, and Advanced levels to mark this entire topic as completed.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
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
                                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Next Level: $nextLevelToSuggest", color = Color.Black, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = Color.Black)
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
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(if (isAllDone) "Return to Map" else "Save & Return to Map")
                            }
                        }
                    }
                )
            }
        } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No active conversation selected.")
        }
    }
}
}
