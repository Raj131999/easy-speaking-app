package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
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
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GrammarLesson
import com.example.ui.EnglishViewModel
import com.example.ui.Screen
import com.example.ui.WordScoreType
import com.example.ui.components.SpokenVoiceToTextCard
import com.example.ui.theme.StreakGold
import com.example.ui.theme.TealPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrammarScreen(
    viewModel: EnglishViewModel,
    modifier: Modifier = Modifier
) {
    val lesson by viewModel.activeGrammarLesson.collectAsState()
    val lessons by viewModel.grammarLessons.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()
    val isProcessing by viewModel.isProcessingSpeech.collectAsState()
    val isPlayingBack by viewModel.isPlayingBack.collectAsState()
    val isTtsSpeaking by viewModel.isTtsSpeaking.collectAsState()
    val lastScore by viewModel.lastScore.collectAsState()
    val scoredWords by viewModel.scoredWords.collectAsState()
    val recognizedText by viewModel.recognizedText.collectAsState()

    val scrollState = rememberScrollState()

    var selectedAnswer by remember { mutableStateOf<String?>(null) }
    var showQuizFeedback by remember { mutableStateOf(false) }

    // Tab selection state
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Speech Patterns", "Useful Lessons")

    // Filter speech patterns vs useful lessons
    val speechLessons = remember(lessons) { lessons.filter { it.id <= 15 } }
    val usefulLessons = remember(lessons) { lessons.filter { it.id > 15 } }

    val groupedUsefulLessons = remember(usefulLessons) {
        usefulLessons.groupBy { it.category }
    }
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    // LazyListStates for maps
    val speechMapListState = rememberLazyListState()
    val categoryMapListState = rememberLazyListState()
    val categoryLessonsMapListState = rememberLazyListState()

    // Determine current lesson list and next lesson
    val currentLessonList = remember(lesson, speechLessons, usefulLessons, selectedCategory, groupedUsefulLessons) {
        if (lesson == null) emptyList()
        else if (lesson!!.id <= 15) speechLessons
        else groupedUsefulLessons[lesson!!.category] ?: usefulLessons
    }
    val currentLessonIndex = remember(lesson, currentLessonList) {
        if (lesson == null) -1
        else currentLessonList.indexOfFirst { it.id == lesson!!.id }
    }
    val hasNextLesson = currentLessonIndex in 0 until (currentLessonList.size - 1)
    val nextLesson = if (hasNextLesson) currentLessonList[currentLessonIndex + 1] else null

    // Auto scroll map to next uncompleted lesson
    LaunchedEffect(lesson == null, selectedTab) {
        if (lesson == null && selectedTab == 0 && speechLessons.isNotEmpty()) {
            val targetIdx = speechLessons.indexOfFirst { !it.isCompleted }.takeIf { it >= 0 } ?: 0
            if (targetIdx > 0) {
                speechMapListState.animateScrollToItem(targetIdx)
            }
        }
    }

    LaunchedEffect(lesson == null, selectedTab, selectedCategory) {
        if (lesson == null && selectedTab == 1 && selectedCategory != null) {
            val catLessons = groupedUsefulLessons[selectedCategory] ?: emptyList()
            val targetIdx = catLessons.indexOfFirst { !it.isCompleted }.takeIf { it >= 0 } ?: 0
            if (targetIdx > 0) {
                categoryLessonsMapListState.animateScrollToItem(targetIdx)
            }
        } else if (lesson == null && selectedTab == 1 && selectedCategory == null) {
            val categoryList = groupedUsefulLessons.keys.toList()
            val targetIdx = categoryList.indexOfFirst { cat ->
                val list = groupedUsefulLessons[cat] ?: emptyList()
                list.isNotEmpty() && !list.all { it.isCompleted }
            }.takeIf { it >= 0 } ?: 0
            if (targetIdx > 0) {
                categoryMapListState.animateScrollToItem(targetIdx)
            }
        }
    }

    // Intercept back button when in category view to return to categories list
    BackHandler(enabled = (lesson == null && selectedCategory != null)) {
        selectedCategory = null
    }

    // Reset local quiz state when the active lesson changes
    LaunchedEffect(lesson) {
        selectedAnswer = null
        if (lesson?.optionsString?.isEmpty() == true) {
            showQuizFeedback = true
        } else {
            showQuizFeedback = false
        }
    }

    if (lesson == null) {
        // --- MAP VIEW OF GRAMMAR LESSONS ---
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Spoken Grammar") },
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
                // Tab Selection Row
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = TealPrimary
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                            }
                        )
                    }
                }

                if (selectedTab == 0) {
                    // --- TAB 1: SPEECH PATTERNS TRAIL ---
                    Column(modifier = Modifier.fillMaxSize()) {
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
                                        text = "GRAMMAR TRAIL",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = TealPrimary
                                        )
                                    )
                                    Text(
                                        text = "Master essential spoken grammar structures step-by-step. Speak clearly to complete lessons on the trail!",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                            }
                        }

                        // Map Path
                        LazyColumn(
                            state = speechMapListState,
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f),
                            contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp, start = 16.dp, end = 16.dp)
                        ) {
                            itemsIndexed(speechLessons) { index, item ->
                                val isCompleted = item.isCompleted
                                val isFirstUncompleted = speechLessons.indexOfFirst { !it.isCompleted } == index
                                val isActive = isFirstUncompleted || (speechLessons.all { it.isCompleted } && index == 0)

                                // Alternate left & right detail cards around the center line
                                val isLeft = index % 2 == 0

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.activeGrammarLesson.value = item
                                            viewModel.lastScore.value = null
                                            viewModel.scoredWords.value = emptyList()
                                        }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isLeft) {
                                        Box(modifier = Modifier.weight(1f)) {
                                            MapLocationCard(
                                                title = item.title,
                                                subtitle = "${item.level} • ${item.speechPrompt}",
                                                score = if (isCompleted) 100 else null,
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
                                                title = item.title,
                                                subtitle = "${item.level} • ${item.speechPrompt}",
                                                score = if (isCompleted) 100 else null,
                                                isCompleted = isCompleted,
                                                isActive = isActive,
                                                accentColor = TealPrimary
                                            )
                                        }
                                    }
                                }

                                // Connect to next item
                                if (index < speechLessons.size - 1) {
                                    MapConnector()
                                }
                            }
                        }
                    }
                } else {
                    // --- TAB 2: USEFUL LESSONS MAP ---
                    if (selectedCategory == null) {
                        // Category Selection Map Trail
                        val categoryList = remember(groupedUsefulLessons) { groupedUsefulLessons.keys.toList() }
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Category instruction header
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
                                            text = "GRAMMAR PATHS",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = TealPrimary
                                            )
                                        )
                                        Text(
                                            text = "Expand your daily English vocabulary across 32 comprehensive grammar categories. Tap a location on the map to begin!",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                    }
                                }
                            }

                            // Category Map Path
                            LazyColumn(
                                state = categoryMapListState,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .weight(1f),
                                contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp, start = 16.dp, end = 16.dp)
                            ) {
                                itemsIndexed(categoryList) { index, categoryName ->
                                    val categoryLessons = groupedUsefulLessons[categoryName] ?: emptyList()
                                    val totalLessons = categoryLessons.size
                                    val completedLessons = categoryLessons.count { it.isCompleted }
                                    val isCompleted = totalLessons > 0 && completedLessons == totalLessons
                                    val isActive = !isCompleted && completedLessons > 0
                                    val statusText = when {
                                        isCompleted -> "Completed"
                                        completedLessons > 0 -> "In Progress ($completedLessons/$totalLessons)"
                                        else -> "Not Completed"
                                    }

                                    // Alternate left & right detail cards around the center line
                                    val isLeft = index % 2 == 0

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedCategory = categoryName
                                            }
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (isLeft) {
                                            Box(modifier = Modifier.weight(1f)) {
                                                MapLocationCard(
                                                    title = categoryName,
                                                    subtitle = "$completedLessons / $totalLessons Completed",
                                                    score = if (totalLessons > 0) (completedLessons * 100 / totalLessons) else null,
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
                                                    title = categoryName,
                                                    subtitle = "$completedLessons / $totalLessons Completed",
                                                    score = if (totalLessons > 0) (completedLessons * 100 / totalLessons) else null,
                                                    isCompleted = isCompleted,
                                                    isActive = isActive,
                                                    accentColor = TealPrimary,
                                                    customStatusText = statusText
                                                )
                                            }
                                        }
                                    }

                                    // Connect to next item
                                    if (index < categoryList.size - 1) {
                                        MapConnector()
                                    }
                                }
                            }
                        }
                    } else {
                        // Category Selected -> Show Lessons Map Trail
                        val categoryName = selectedCategory!!
                        val categoryLessons = groupedUsefulLessons[categoryName] ?: emptyList()
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Row with Back button and Category Title
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { selectedCategory = null },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(TealPrimary.copy(alpha = 0.1f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back to Categories",
                                        tint = TealPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = categoryName,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Instruction Header
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = TealPrimary.copy(alpha = 0.08f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayCircle,
                                        contentDescription = "Lesson Guidance",
                                        tint = TealPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(
                                        text = "Complete each spoken lesson step-by-step to master the patterns!",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                            }

                            // Lessons Map Path
                            LazyColumn(
                                state = categoryLessonsMapListState,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .weight(1f),
                                contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp, start = 16.dp, end = 16.dp)
                            ) {
                                itemsIndexed(categoryLessons) { index, item ->
                                    val isCompleted = item.isCompleted
                                    val isFirstUncompleted = categoryLessons.indexOfFirst { !it.isCompleted } == index
                                    val isActive = isFirstUncompleted || (categoryLessons.all { it.isCompleted } && index == 0)

                                    // Alternate left & right detail cards around the center line
                                    val isLeft = index % 2 == 0

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.activeGrammarLesson.value = item
                                                viewModel.lastScore.value = null
                                                viewModel.scoredWords.value = emptyList()
                                            }
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (isLeft) {
                                            Box(modifier = Modifier.weight(1f)) {
                                                MapLocationCard(
                                                    title = item.title,
                                                    subtitle = "${item.level} • ${item.speechPrompt}",
                                                    score = if (isCompleted) 100 else null,
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
                                                    title = item.title,
                                                    subtitle = "${item.level} • ${item.speechPrompt}",
                                                    score = if (isCompleted) 100 else null,
                                                    isCompleted = isCompleted,
                                                    isActive = isActive,
                                                    accentColor = TealPrimary
                                                )
                                            }
                                        }
                                    }

                                    // Connect to next item
                                    if (index < categoryLessons.size - 1) {
                                        MapConnector()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        // --- LESSON PRACTICE VIEW ---
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Spoken Grammar") },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.activeGrammarLesson.value = null }) {
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
            lesson?.let { item ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(innerPadding)
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Lesson Title & Level
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        SuggestionChip(
                            onClick = {},
                            label = { Text(item.level) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = TealPrimary.copy(alpha = 0.15f),
                                labelColor = TealPrimary
                            )
                        )
                    }

                    // Grammar Rule Explanation Box
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Grammar Rule",
                                    tint = TealPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "HOW SPOKEN ENGLISH WORKS",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TealPrimary,
                                        letterSpacing = 1.sp
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = item.explanation,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    lineHeight = 22.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }

                    // Native Audio Example Card
                    val exampleLines = item.exampleText.split("\n").filter { it.isNotBlank() }
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Examples",
                                    tint = TealPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (exampleLines.size > 1) "EXAMPLES & USAGE" else "EXAMPLE SENTENCE",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TealPrimary,
                                        letterSpacing = 1.sp
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                if (exampleLines.size > 1) {
                                    IconButton(
                                        onClick = { viewModel.speak(viewModel.extractSpokenExample(item.exampleText)) },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(TealPrimary.copy(alpha = 0.15f), CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Play All Examples",
                                            tint = TealPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                            exampleLines.forEach { line ->
                                val speechText = viewModel.extractSpokenExample(line)
                                val annotatedLine = remember(line) {
                                    buildAnnotatedString {
                                        if (line.contains(":")) {
                                            val colonIdx = line.indexOf(':')
                                            val topicPart = line.substring(0, colonIdx + 1)
                                            val examplePart = line.substring(colonIdx + 1)
                                            withStyle(
                                                SpanStyle(
                                                    fontWeight = FontWeight.Bold,
                                                    color = TealPrimary
                                                )
                                            ) {
                                                append(topicPart)
                                            }
                                            withStyle(
                                                SpanStyle(
                                                    fontWeight = FontWeight.Normal,
                                                    color = Color.Unspecified
                                                )
                                            ) {
                                                append(examplePart)
                                            }
                                        } else {
                                            append(line)
                                        }
                                    }
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { viewModel.speak(speechText) }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { viewModel.speak(speechText) },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(TealPrimary.copy(alpha = 0.15f), CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                            contentDescription = "Hear Line",
                                            tint = TealPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = annotatedLine,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = MaterialTheme.colorScheme.onSurface,
                                            lineHeight = 20.sp
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    // --- GRAMMAR MULTIPLE CHOICE CHECK ---
                    if (item.optionsString.isNotEmpty()) {
                        Text(
                            text = "Quick Comprehension Check",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        if (item.quizQuestion.isNotBlank()) {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.HelpOutline,
                                        contentDescription = "Question",
                                        tint = TealPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = item.quizQuestion,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            lineHeight = 20.sp
                                        )
                                    )
                                }
                            }
                        }

                        item.optionsString.split(",").forEach { option ->
                            val isSelected = selectedAnswer == option
                            val isCorrect = option == item.correctOption

                            val cardColor = when {
                                showQuizFeedback && isCorrect -> Color(0xFF2ECC71).copy(alpha = 0.15f)
                                showQuizFeedback && isSelected && !isCorrect -> Color(0xFFE74C3C).copy(alpha = 0.15f)
                                isSelected -> TealPrimary.copy(alpha = 0.1f)
                                else -> MaterialTheme.colorScheme.surface
                            }

                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = cardColor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (!showQuizFeedback) {
                                            selectedAnswer = option
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = {
                                            if (!showQuizFeedback) {
                                                selectedAnswer = option
                                            }
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = option,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    if (showQuizFeedback) {
                                        if (isCorrect) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Correct",
                                                tint = Color(0xFF2ECC71)
                                            )
                                        } else if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Cancel,
                                                contentDescription = "Incorrect",
                                                tint = Color(0xFFE74C3C)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (!showQuizFeedback) {
                            Button(
                                onClick = { showQuizFeedback = true },
                                enabled = selectedAnswer != null,
                                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            ) {
                                Text("Check Answer", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // --- SPEAKING WORKOUT PRACTICE ---
                    AnimatedVisibility(
                        visible = showQuizFeedback,
                        enter = fadeIn() + expandVertically()
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            Text(
                                text = "Step 2: Read Aloud Speaking Drill",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TealPrimary
                            )

                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "SAY THIS SENTENCE:",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            letterSpacing = 1.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = item.speechPrompt,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(20.dp))

                                    // Playback reference model
                                    Button(
                                        onClick = { viewModel.speak(item.speechPrompt) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isTtsSpeaking) Color(0xFFFF5252) else MaterialTheme.colorScheme.secondary
                                        ),
                                        modifier = Modifier.height(40.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isTtsSpeaking) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                                            contentDescription = if (isTtsSpeaking) "Stop" else "Hear",
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isTtsSpeaking) "Stop Audio" else "Hear Model Audio",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(24.dp))

                                    // Large Mic Button
                                    Box(contentAlignment = Alignment.Center) {
                                        val micColor = if (isRecording) Color(0xFFFF5252) else TealPrimary
                                        Box(
                                            modifier = Modifier
                                                .size(72.dp)
                                                .clip(CircleShape)
                                                .background(micColor)
                                                .clickable {
                                                    if (isRecording) {
                                                        viewModel.stopRecording(item.speechPrompt, "grammar", item.id)
                                                    } else {
                                                        viewModel.startRecording(item.speechPrompt)
                                                    }
                                                }
                                                .testTag("record_mic_button"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                                                contentDescription = if (isRecording) "Stop" else "Record",
                                                tint = Color.Black,
                                                modifier = Modifier.size(32.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = if (isRecording) "Recording... Tap to stop" else "Tap to Record & Compare",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = if (isRecording) Color(0xFFFF5252) else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = if (isRecording) FontWeight.Bold else FontWeight.Normal
                                        )
                                    )

                                    // Real-time Voice to Text Display
                                    Spacer(modifier = Modifier.height(12.dp))
                                    SpokenVoiceToTextCard(
                                        isRecording = isRecording,
                                        isProcessing = isProcessing,
                                        recognizedText = recognizedText,
                                        hasEvaluated = lastScore != null
                                    )

                                    // Scoring & feedback visualization
                                    lastScore?.let { score ->
                                        Spacer(modifier = Modifier.height(20.dp))
                                        Text(
                                            text = "Pronunciation Score",
                                            style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        )
                                        Text(
                                            text = "$score%",
                                            style = MaterialTheme.typography.displayMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (score >= 90) Color(0xFF2ECC71) else if (score >= 80) StreakGold else Color(0xFFFF5252)
                                            )
                                        )

                                        // Display Word-by-Word correctness
                                        Spacer(modifier = Modifier.height(12.dp))
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
                                                Card(
                                                    colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f)),
                                                    shape = RoundedCornerShape(6.dp),
                                                    modifier = Modifier
                                                        .padding(4.dp)
                                                        .clickable { viewModel.speak(word) }
                                                ) {
                                                    Text(
                                                        text = word,
                                                        color = color,
                                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }
                                        }

                                        // Voice Playback and Next / Finish controls
                                        Spacer(modifier = Modifier.height(20.dp))
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(10.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                OutlinedButton(
                                                    onClick = { viewModel.playRecordedVoice() },
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(12.dp),
                                                    colors = ButtonDefaults.outlinedButtonColors(
                                                        contentColor = if (isPlayingBack) Color(0xFFFF5252) else TealPrimary
                                                    )
                                                ) {
                                                    Icon(
                                                        imageVector = if (isPlayingBack) Icons.Default.Stop else Icons.Default.PlayArrow,
                                                        contentDescription = "Play"
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(if (isPlayingBack) "Stop Voice" else "Hear Your Voice")
                                                }

                                                if (hasNextLesson && nextLesson != null) {
                                                    Button(
                                                        onClick = {
                                                            viewModel.activeGrammarLesson.value = nextLesson
                                                            viewModel.lastScore.value = null
                                                            viewModel.scoredWords.value = emptyList()
                                                            viewModel.recognizedText.value = ""
                                                        },
                                                        modifier = Modifier.weight(1f),
                                                        shape = RoundedCornerShape(12.dp),
                                                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                                                    ) {
                                                        Text("Next Lesson", color = Color.Black, fontWeight = FontWeight.Bold)
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Next", tint = Color.Black)
                                                    }
                                                }
                                            }

                                            Button(
                                                onClick = {
                                                    val activeItem = lesson
                                                    if (activeItem != null) {
                                                        if (activeItem.id <= 15) {
                                                            selectedTab = 0
                                                        } else {
                                                            selectedTab = 1
                                                            selectedCategory = activeItem.category
                                                        }
                                                    }
                                                    viewModel.activeGrammarLesson.value = null
                                                    viewModel.lastScore.value = null
                                                    viewModel.scoredWords.value = emptyList()
                                                    viewModel.recognizedText.value = ""
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (hasNextLesson) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.secondary
                                                ),
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Map,
                                                    contentDescription = "Map View",
                                                    tint = if (hasNextLesson) MaterialTheme.colorScheme.onSurfaceVariant else Color.White
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = if (hasNextLesson) "Finish Lesson (View Next on Map)" else "Complete Lesson Trail",
                                                    color = if (hasNextLesson) MaterialTheme.colorScheme.onSurfaceVariant else Color.White,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No grammar lesson selected.")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        content = { content() }
    )
}
