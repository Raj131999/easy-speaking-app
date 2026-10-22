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
    val isPlayingBack by viewModel.isPlayingBack.collectAsState()
    val isTtsSpeaking by viewModel.isTtsSpeaking.collectAsState()
    val lastScore by viewModel.lastScore.collectAsState()
    val scoredWords by viewModel.scoredWords.collectAsState()
    val recognizedText by viewModel.recognizedText.collectAsState()
    val isProcessingSpeech by viewModel.isProcessingSpeech.collectAsState()

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
    var scrollToTargetIndex by remember { mutableStateOf<Int?>(null) }

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

    val speechCompletedCount = speechLessons.count { it.isCompleted }
    val categoryCompletedCount = remember(groupedUsefulLessons, selectedCategory) {
        if (selectedCategory != null) {
            (groupedUsefulLessons[selectedCategory] ?: emptyList()).count { it.isCompleted }
        } else {
            groupedUsefulLessons.values.flatten().count { it.isCompleted }
        }
    }

    // Auto scroll map to next uncompleted lesson or explicit target lesson
    LaunchedEffect(lesson == null, selectedTab, speechCompletedCount, scrollToTargetIndex) {
        if (lesson == null && selectedTab == 0 && speechLessons.isNotEmpty()) {
            val targetIdx = scrollToTargetIndex ?: speechLessons.indexOfFirst { !it.isCompleted }.takeIf { it >= 0 } ?: (speechLessons.size - 1)
            if (targetIdx in speechLessons.indices) {
                speechMapListState.animateScrollToItem(targetIdx)
            }
            scrollToTargetIndex = null
        }
    }

    LaunchedEffect(lesson == null, selectedTab, selectedCategory, categoryCompletedCount, scrollToTargetIndex) {
        if (lesson == null && selectedTab == 1 && selectedCategory != null) {
            val catLessons = groupedUsefulLessons[selectedCategory] ?: emptyList()
            if (catLessons.isNotEmpty()) {
                val targetIdx = scrollToTargetIndex ?: catLessons.indexOfFirst { !it.isCompleted }.takeIf { it >= 0 } ?: (catLessons.size - 1)
                if (targetIdx in catLessons.indices) {
                    categoryLessonsMapListState.animateScrollToItem(targetIdx)
                }
            }
            scrollToTargetIndex = null
        } else if (lesson == null && selectedTab == 1 && selectedCategory == null) {
            val categoryList = groupedUsefulLessons.keys.toList()
            val targetIdx = categoryList.indexOfFirst { cat ->
                val list = groupedUsefulLessons[cat] ?: emptyList()
                list.isNotEmpty() && !list.all { it.isCompleted }
            }.takeIf { it >= 0 } ?: 0
            if (targetIdx in categoryList.indices) {
                categoryMapListState.animateScrollToItem(targetIdx)
            }
            scrollToTargetIndex = null
        }
    }

    // Intercept back button when practicing a lesson to return to that lesson on the map
    BackHandler(enabled = (lesson != null)) {
        val activeItem = lesson
        if (activeItem != null) {
            if (activeItem.id <= 15) {
                selectedTab = 0
            } else {
                selectedTab = 1
                selectedCategory = activeItem.category
            }
            if (currentLessonIndex >= 0) {
                scrollToTargetIndex = currentLessonIndex
            }
        }
        viewModel.activeGrammarLesson.value = null
        viewModel.lastScore.value = null
        viewModel.scoredWords.value = emptyList()
        viewModel.recognizedText.value = ""
    }

    // Intercept back button when in category view to return to categories list
    BackHandler(enabled = (lesson == null && selectedCategory != null)) {
        val categoryList = groupedUsefulLessons.keys.toList()
        val catIdx = categoryList.indexOf(selectedCategory).takeIf { it >= 0 }
        if (catIdx != null) {
            scrollToTargetIndex = catIdx
        }
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
                                    onClick = {
                                        val categoryList = groupedUsefulLessons.keys.toList()
                                        val catIdx = categoryList.indexOf(selectedCategory).takeIf { it >= 0 }
                                        if (catIdx != null) {
                                            scrollToTargetIndex = catIdx
                                        }
                                        selectedCategory = null
                                    },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(TealPrimary.copy(alpha = 0.1f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
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
                        IconButton(onClick = {
                            val activeItem = lesson
                            if (activeItem != null) {
                                if (activeItem.id <= 15) {
                                    selectedTab = 0
                                } else {
                                    selectedTab = 1
                                    selectedCategory = activeItem.category
                                }
                                if (currentLessonIndex >= 0) {
                                    scrollToTargetIndex = currentLessonIndex
                                }
                            }
                            viewModel.activeGrammarLesson.value = null
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
                                    imageVector = Icons.Default.VolumeUp,
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
                                            imageVector = Icons.Default.VolumeUp,
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
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                        modifier = Modifier.height(40.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.VolumeUp, contentDescription = "Hear", modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Hear Model Audio", style = MaterialTheme.typography.bodyMedium)
                                    }

                                    Spacer(modifier = Modifier.height(24.dp))

                                    // Large Mic Button
                                    Box(contentAlignment = Alignment.Center) {
                                        val micColor = when {
                                            isProcessingSpeech -> StreakGold
                                            isRecording -> Color(0xFFFF5252)
                                            else -> TealPrimary
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(72.dp)
                                                .clip(CircleShape)
                                                .background(micColor)
                                                .clickable(enabled = !isProcessingSpeech) {
                                                    if (isRecording) {
                                                        viewModel.stopRecording(item.speechPrompt, "grammar", item.id)
                                                    } else {
                                                        viewModel.startRecording(item.speechPrompt)
                                                    }
                                                }
                                                .testTag("record_mic_button"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isProcessingSpeech) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(30.dp),
                                                    color = Color.Black,
                                                    strokeWidth = 3.dp
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                                                    contentDescription = if (isRecording) "Stop Recording" else "Record Speaking",
                                                    tint = Color.Black,
                                                    modifier = Modifier.size(32.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = when {
                                            isProcessingSpeech -> "Analyzing your speech... Please wait"
                                            isRecording -> "Recording... Tap to stop"
                                            lastScore != null -> "Tap to Record & Compare Again"
                                            else -> "Tap to Record & Compare"
                                        },
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = when {
                                                isProcessingSpeech -> StreakGold
                                                isRecording -> Color(0xFFFF5252)
                                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                                            },
                                            fontWeight = if (isRecording || isProcessingSpeech) FontWeight.Bold else FontWeight.Normal
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
                                                        text = "Evaluating speech pattern...",
                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            color = TealPrimary
                                                        )
                                                    )
                                                    Text(
                                                        text = "Analyzing pronunciation and accuracy...",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Real-time Voice to Text Display
                                    Spacer(modifier = Modifier.height(12.dp))
                                    SpokenVoiceToTextCard(
                                        isRecording = isRecording,
                                        recognizedText = recognizedText,
                                        hasEvaluated = lastScore != null
                                    )

                                    // Scoring & feedback visualization
                                    lastScore?.let { score ->
                                        val isPassed = score >= 75
                                        Spacer(modifier = Modifier.height(20.dp))
                                        Text(
                                            text = "Pronunciation Score",
                                            style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        )
                                        Text(
                                            text = "$score%",
                                            style = MaterialTheme.typography.displayMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = when {
                                                    score >= 90 -> Color(0xFF2ECC71)
                                                    score >= 75 -> Color(0xFF2ECC71)
                                                    score >= 50 -> StreakGold
                                                    else -> Color(0xFFFF5252)
                                                }
                                            )
                                        )

                                        // Lesson passing indicator
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isPassed) Color(0xFF2ECC71).copy(alpha = 0.15f) else StreakGold.copy(alpha = 0.15f),
                                            border = BorderStroke(1.dp, if (isPassed) Color(0xFF2ECC71) else StreakGold),
                                            modifier = Modifier.padding(top = 4.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = if (isPassed) Icons.Default.CheckCircle else Icons.Default.Info,
                                                    contentDescription = null,
                                                    tint = if (isPassed) Color(0xFF2ECC71) else StreakGold,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = if (isPassed) "Lesson Passed (>= 75%)" else "Need 75% to complete lesson (Current: $score%)",
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isPassed) Color(0xFF2ECC71) else StreakGold
                                                    )
                                                )
                                            }
                                        }

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
                                                        if (hasNextLesson) {
                                                            scrollToTargetIndex = currentLessonIndex + 1
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
