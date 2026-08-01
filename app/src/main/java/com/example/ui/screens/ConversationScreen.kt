package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
    val lastScore by viewModel.lastScore.collectAsState()
    val scoredWords by viewModel.scoredWords.collectAsState()

    val listState = rememberLazyListState()

    var showQuiz by remember { mutableStateOf(false) }
    var selectedQuizAnswer by remember { mutableStateOf<String?>(null) }
    var showQuizFeedback by remember { mutableStateOf(false) }
    var selectedLevel by remember(conv) { mutableStateOf("Basic") }

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

    // Auto-scroll list when the active index changes
    LaunchedEffect(currentIndex, dialogueLines.size) {
        if (dialogueLines.isNotEmpty()) {
            listState.animateScrollToItem(currentIndex)
        }
    }

    // Reset local quiz state when the active conversation changes
    LaunchedEffect(conv) {
        showQuiz = false
        selectedQuizAnswer = null
        showQuizFeedback = false
    }

    if (conv == null) {
        // --- MAP VIEW OF CONVERSATIONS ---
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Conversation Map") },
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
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp, start = 16.dp, end = 16.dp)
                ) {
                    itemsIndexed(conversationsList) { index, conversation ->
                        val isCompleted = conversation.isCompleted
                        val isFirstUncompleted = conversationsList.indexOfFirst { !it.isCompleted } == index
                        val isActive = isFirstUncompleted || (conversationsList.all { it.isCompleted } && index == 0)

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
                                        score = if (isCompleted) 100 else null, // Dialogues are perfect when completed
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
                                        title = cleanConversationTitle(conversation.title),
                                        subtitle = conversation.scenario,
                                        score = if (isCompleted) 100 else null,
                                        isCompleted = isCompleted,
                                        isActive = isActive,
                                        accentColor = TealPrimary
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
                        IconButton(onClick = { viewModel.activeConversation.value = null }) {
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
                        FilterChip(
                            selected = selectedLevel == lvl,
                            onClick = {
                                if (selectedLevel != lvl) {
                                    selectedLevel = lvl
                                    viewModel.activeConversationIndex.value = 0
                                }
                            },
                            label = { Text(lvl, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
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
                                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f), CircleShape)
                                        ) {
                                            Icon(imageVector = Icons.Default.VolumeUp, contentDescription = "Hear Example", tint = Color.White)
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
                                            modifier = Modifier
                                                .size(64.dp)
                                                .background(if (isRecording) Color(0xFFFF5252) else TealPrimary, CircleShape)
                                                .testTag("conversation_record_mic")
                                        ) {
                                            Icon(
                                                imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                                                contentDescription = if (isRecording) "Stop" else "Record",
                                                tint = Color.Black,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(20.dp))

                                        // Hear user voice
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
                                                .size(44.dp)
                                                .background(
                                                    if (isPlayingBack) Color(0xFFFF5252) else MaterialTheme.colorScheme.secondary.copy(alpha = if (lastScore != null) 0.4f else 0.1f),
                                                    CircleShape
                                                )
                                        ) {
                                            Icon(
                                                imageVector = if (isPlayingBack) Icons.Default.VolumeMute else Icons.Default.PlayArrow,
                                                contentDescription = "Playback",
                                                tint = if (lastScore != null) Color.White else Color.Gray
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = if (isRecording) "Listening... Tap to end" else "Tap Mic to record your line",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = if (isRecording) Color(0xFFFF5252) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )

                                    // Display score feedback
                                    lastScore?.let { score ->
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "Pronunciation score: ",
                                                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            )
                                            Text(
                                                text = "$score%",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (score >= 90) Color(0xFF2ECC71) else StreakGold
                                                )
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))
                                        Button(
                                            onClick = {
                                                viewModel.advanceDialogue()
                                                viewModel.lastScore.value = null // clear score for next line
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Perfect! Continue Conversation", color = Color.Black, fontWeight = FontWeight.Bold)
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
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                        ) {
                                            Icon(imageVector = Icons.Default.VolumeUp, contentDescription = "Hear")
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Play Voice Again")
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Button(
                                            onClick = {
                                                if (currentIndex < dialogueLines.size - 1) {
                                                    viewModel.advanceDialogue()
                                                } else {
                                                    // Go to comprehension check
                                                    showQuiz = true
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                                        ) {
                                            Text("Next Line", color = Color.Black)
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
                                .padding(24.dp)
                                .fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(StreakGold.copy(alpha = 0.2f))
                            ) {
                                Icon(imageVector = Icons.Default.Grade, contentDescription = "Success", tint = StreakGold, modifier = Modifier.size(32.dp))
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
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            item.comprehensionOptions.split(",").forEach { option ->
                                val isSelected = selectedQuizAnswer == option
                                val isCorrect = option == item.comprehensionAnswer

                                val cardColor = when {
                                    showQuizFeedback && isCorrect -> Color(0xFF2ECC71).copy(alpha = 0.15f)
                                    showQuizFeedback && isSelected && !isCorrect -> Color(0xFFE74C3C).copy(alpha = 0.15f)
                                    isSelected -> TealPrimary.copy(alpha = 0.1f)
                                    else -> MaterialTheme.colorScheme.background
                                }

                                val borderColor = when {
                                    showQuizFeedback && isCorrect -> Color(0xFF2ECC71)
                                    showQuizFeedback && isSelected && !isCorrect -> Color(0xFFE74C3C)
                                    isSelected -> TealPrimary
                                    else -> Color.Transparent
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(cardColor)
                                        .clickable {
                                            if (!showQuizFeedback) {
                                                selectedQuizAnswer = option
                                            }
                                        }
                                        .padding(16.dp)
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
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
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

                            Spacer(modifier = Modifier.weight(1f))

                            if (!showQuizFeedback) {
                                Button(
                                    onClick = { showQuizFeedback = true },
                                    enabled = selectedQuizAnswer != null,
                                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                ) {
                                    Text("Check Answer", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(
                                    onClick = {
                                        // Finalize Conversation
                                        viewModel.advanceDialogue()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                ) {
                                    Text("Complete Lesson & Claim 30 XP", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No active conversation selected.")
        }
    }
}
}
