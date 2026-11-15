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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.example.ui.EnglishViewModel
import com.example.ui.components.MascotCoach
import com.example.ui.components.MascotState
import com.example.ui.components.PronunciationFeedbackChips
import com.example.ui.components.SonicVoiceOrb
import com.example.ui.components.SpokenVoiceToTextCard
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrammarScreen(
    viewModel: EnglishViewModel,
    modifier: Modifier = Modifier
) {
    val lesson by viewModel.activeGrammarLesson.collectAsState()
    val lessons by viewModel.grammarLessons.collectAsState()
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

    val scrollState = rememberScrollState()

    var selectedAnswer by remember { mutableStateOf<String?>(null) }
    var showQuizFeedback by remember { mutableStateOf(false) }

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Speech Patterns", "Useful Lessons")

    val speechLessons = remember(lessons) { lessons.filter { it.id <= 15 } }
    val usefulLessons = remember(lessons) { lessons.filter { it.id > 15 } }

    val groupedUsefulLessons = remember(usefulLessons) {
        usefulLessons.groupBy { it.category }
    }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var scrollToTargetIndex by remember { mutableStateOf<Int?>(null) }

    val speechMapListState = rememberLazyListState()
    val categoryMapListState = rememberLazyListState()
    val categoryLessonsMapListState = rememberLazyListState()

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

    LaunchedEffect(lesson == null, selectedTab, speechCompletedCount, scrollToTargetIndex) {
        if (lesson == null && selectedTab == 0 && speechLessons.isNotEmpty()) {
            val targetIdx = scrollToTargetIndex
                ?: speechLessons.indexOfFirst { !it.isCompleted }.takeIf { it >= 0 }
                ?: (speechLessons.size - 1)
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
                val targetIdx = scrollToTargetIndex
                    ?: catLessons.indexOfFirst { !it.isCompleted }.takeIf { it >= 0 }
                    ?: (catLessons.size - 1)
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

    BackHandler(enabled = (lesson == null && selectedCategory != null)) {
        val categoryList = groupedUsefulLessons.keys.toList()
        val catIdx = categoryList.indexOf(selectedCategory).takeIf { it >= 0 }
        if (catIdx != null) {
            scrollToTargetIndex = catIdx
        }
        selectedCategory = null
    }

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
                    title = {
                        Text(
                            text = "Spoken Grammar",
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
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.White,
                    contentColor = PolishTeal,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = PolishTeal
                        )
                    },
                    divider = { HorizontalDivider(color = Slate100) }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            selectedContentColor = PolishTeal,
                            unselectedContentColor = Slate500,
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
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Slate100),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
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
                                        text = "GRAMMAR TRAIL",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = PolishTeal,
                                            letterSpacing = 0.8.sp
                                        )
                                    )
                                    Text(
                                        text = "Master essential spoken grammar structures step-by-step on the trail.",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Slate500
                                        )
                                    )
                                }
                            }
                        }

                        LazyColumn(
                            state = speechMapListState,
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f),
                            contentPadding = PaddingValues(top = 8.dp, bottom = 48.dp, start = 16.dp, end = 16.dp)
                        ) {
                            itemsIndexed(speechLessons) { index, item ->
                                val isCompleted = item.isCompleted
                                val isFirstUncompleted = speechLessons.indexOfFirst { !it.isCompleted } == index
                                val isActive = isFirstUncompleted || (speechLessons.all { it.isCompleted } && index == 0)
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
                                                title = item.title,
                                                subtitle = "${item.level} • ${item.speechPrompt}",
                                                score = if (isCompleted) 100 else null,
                                                isCompleted = isCompleted,
                                                isActive = isActive,
                                                accentColor = PolishTeal
                                            )
                                        }
                                    }
                                }

                                if (index < speechLessons.size - 1) {
                                    MapConnector()
                                }
                            }
                        }
                    }
                } else {
                    // --- TAB 2: USEFUL LESSONS MAP ---
                    if (selectedCategory == null) {
                        val categoryList = remember(groupedUsefulLessons) { groupedUsefulLessons.keys.toList() }
                        Column(modifier = Modifier.fillMaxSize()) {
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Slate100),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(PolishIndigoBg)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Map,
                                            contentDescription = "Map Guidance",
                                            tint = PolishIndigoText,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "GRAMMAR PATHS",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = PolishIndigoText,
                                                letterSpacing = 0.8.sp
                                            )
                                        )
                                        Text(
                                            text = "Expand daily grammar fluency across structured spoken categories.",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Slate500
                                            )
                                        )
                                    }
                                }
                            }

                            LazyColumn(
                                state = categoryMapListState,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .weight(1f),
                                contentPadding = PaddingValues(top = 8.dp, bottom = 48.dp, start = 16.dp, end = 16.dp)
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
                                        else -> "Ready"
                                    }
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
                                                    title = categoryName,
                                                    subtitle = "$completedLessons / $totalLessons Completed",
                                                    score = if (totalLessons > 0) (completedLessons * 100 / totalLessons) else null,
                                                    isCompleted = isCompleted,
                                                    isActive = isActive,
                                                    accentColor = PolishTeal,
                                                    customStatusText = statusText
                                                )
                                            }
                                        }
                                    }

                                    if (index < categoryList.size - 1) {
                                        MapConnector()
                                    }
                                }
                            }
                        }
                    } else {
                        val categoryName = selectedCategory!!
                        val categoryLessons = groupedUsefulLessons[categoryName] ?: emptyList()
                        Column(modifier = Modifier.fillMaxSize()) {
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
                                        .size(48.dp)
                                        .background(PolishTealBg, CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back to Categories",
                                        tint = PolishTeal,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = categoryName,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = PolishNavy,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Slate100),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayCircle,
                                        contentDescription = "Lesson Guidance",
                                        tint = PolishTeal,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Complete each spoken lesson step-by-step to master the patterns!",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Slate500
                                        )
                                    )
                                }
                            }

                            LazyColumn(
                                state = categoryLessonsMapListState,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .weight(1f),
                                contentPadding = PaddingValues(top = 12.dp, bottom = 48.dp, start = 16.dp, end = 16.dp)
                            ) {
                                itemsIndexed(categoryLessons) { index, item ->
                                    val isCompleted = item.isCompleted
                                    val isFirstUncompleted = categoryLessons.indexOfFirst { !it.isCompleted } == index
                                    val isActive = isFirstUncompleted || (categoryLessons.all { it.isCompleted } && index == 0)
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
                                                    title = item.title,
                                                    subtitle = "${item.level} • ${item.speechPrompt}",
                                                    score = if (isCompleted) 100 else null,
                                                    isCompleted = isCompleted,
                                                    isActive = isActive,
                                                    accentColor = PolishTeal
                                                )
                                            }
                                        }
                                    }

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
                    title = {
                        Text(
                            text = "Spoken Grammar",
                            fontWeight = FontWeight.Bold,
                            color = PolishNavy
                        )
                    },
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
            lesson?.let { item ->
                val mascotState = when {
                    isRecording -> MascotState.LISTENING
                    lastScore != null && lastScore!! >= 75 -> MascotState.CELEBRATING
                    lastScore != null -> MascotState.ENCOURAGING
                    else -> MascotState.IDLE
                }
                val coachMessage = when {
                    isProcessingSpeech -> "Checking grammar & clarity..."
                    isRecording -> "Speak smoothly — I'm listening!"
                    lastScore != null && lastScore!! >= 75 -> "Well spoken! ${lastScore}%"
                    lastScore != null -> "Give it one more try!"
                    else -> "Listen to the example, then tap the orb"
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(PolishBg)
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
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = PolishNavy
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = PolishTealBg,
                            border = BorderStroke(1.dp, PolishTealLightBorder)
                        ) {
                            Text(
                                text = item.level,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PolishTeal
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    // Grammar Rule Explanation Box
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Slate100),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Grammar Rule",
                                    tint = PolishTeal,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "HOW SPOKEN ENGLISH WORKS",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = PolishTeal,
                                        letterSpacing = 1.sp
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = item.explanation,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    lineHeight = 23.sp,
                                    color = PolishNavy
                                )
                            )
                        }
                    }

                    // Native Audio Example Card
                    val exampleLines = item.exampleText.split("\n").filter { it.isNotBlank() }
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Slate100),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Examples",
                                    tint = PolishTeal,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (exampleLines.size > 1) "EXAMPLES & USAGE" else "EXAMPLE SENTENCE",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = PolishTeal,
                                        letterSpacing = 1.sp
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                if (exampleLines.size > 1) {
                                    IconButton(
                                        onClick = { viewModel.speak(viewModel.extractSpokenExample(item.exampleText)) },
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(PolishTealBg, CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Play All Examples",
                                            tint = PolishTeal,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(color = Slate100)

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
                                                    color = PolishTeal
                                                )
                                            ) {
                                                append(topicPart)
                                            }
                                            withStyle(
                                                SpanStyle(
                                                    fontWeight = FontWeight.Normal,
                                                    color = PolishNavy
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
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { viewModel.speak(speechText) }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { viewModel.speak(speechText) },
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(PolishTealBg, CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                            contentDescription = "Hear Line",
                                            tint = PolishTeal,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = annotatedLine,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = PolishNavy,
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
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = PolishNavy
                            ),
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        if (item.quizQuestion.isNotBlank()) {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = PolishIndigoBg),
                                border = BorderStroke(1.dp, Color(0xFFE0E7FF)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.HelpOutline,
                                        contentDescription = "Question",
                                        tint = PolishIndigoText,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = item.quizQuestion,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = PolishNavy,
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
                                showQuizFeedback && isCorrect -> Emerald50
                                showQuizFeedback && isSelected && !isCorrect -> Red50
                                isSelected -> PolishTealBg
                                else -> Color.White
                            }
                            val borderColor = when {
                                showQuizFeedback && isCorrect -> Emerald500
                                showQuizFeedback && isSelected && !isCorrect -> Red500
                                isSelected -> PolishTeal
                                else -> Slate200
                            }

                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = cardColor),
                                border = BorderStroke(if (isSelected || (showQuizFeedback && isCorrect)) 1.5.dp else 1.dp, borderColor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (!showQuizFeedback) {
                                            selectedAnswer = option
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = {
                                            if (!showQuizFeedback) {
                                                selectedAnswer = option
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
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Medium,
                                            color = PolishNavy
                                        )
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

                        if (!showQuizFeedback) {
                            Button(
                                onClick = { showQuizFeedback = true },
                                enabled = selectedAnswer != null,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PolishTeal,
                                    contentColor = Color.White,
                                    disabledContainerColor = Slate200,
                                    disabledContentColor = Slate400
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            ) {
                                Text("Check Answer", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // --- SPEAKING WORKOUT PRACTICE ---
                    AnimatedVisibility(
                        visible = showQuizFeedback,
                        enter = fadeIn() + expandVertically()
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            HorizontalDivider(color = Slate200, modifier = Modifier.padding(vertical = 4.dp))
                            Text(
                                text = "Step 2: Read Aloud Speaking Drill",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = PolishTeal
                            )

                            Card(
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Slate100),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "SAY THIS SENTENCE",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Slate500,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = item.speechPrompt,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            color = PolishNavy
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    FilledTonalButton(
                                        onClick = { viewModel.speak(item.speechPrompt) },
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = if (isTtsSpeaking) Red500 else PolishIndigoBg,
                                            contentColor = if (isTtsSpeaking) Color.White else PolishIndigoText
                                        ),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.height(48.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isTtsSpeaking) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                                            contentDescription = "Hear",
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isTtsSpeaking) "Stop Model" else "Hear Model Audio",
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(18.dp))

                                    MascotCoach(
                                        state = mascotState,
                                        audioLevel = liveAudioLevel,
                                        coachMessage = coachMessage,
                                        size = 66.dp
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    SonicVoiceOrb(
                                        isRecording = isRecording,
                                        audioLevel = liveAudioLevel,
                                        isProcessing = isProcessingSpeech,
                                        onClick = {
                                            if (isRecording) {
                                                viewModel.stopRecording(item.speechPrompt, "grammar", item.id)
                                            } else {
                                                viewModel.startRecording(item.speechPrompt)
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
                                        modifier = Modifier.testTag("record_mic_button")
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = when {
                                            isProcessingSpeech -> "Analyzing your speech... Please wait"
                                            isRecording -> "Recording... Tap the orb to stop"
                                            lastScore != null -> "Tap the orb to Record & Compare Again"
                                            else -> "Tap the orb to Record & Compare"
                                        },
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = when {
                                                isProcessingSpeech -> Orange600
                                                isRecording -> Red500
                                                else -> Slate500
                                            },
                                            fontWeight = if (isRecording || isProcessingSpeech) FontWeight.Bold else FontWeight.Medium
                                        )
                                    )

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
                                                .padding(vertical = 8.dp)
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
                                                        text = "Evaluating speech pattern...",
                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            color = PolishTeal
                                                        )
                                                    )
                                                    Text(
                                                        text = "Analyzing pronunciation and accuracy...",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            color = Slate500
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))
                                    SpokenVoiceToTextCard(
                                        isRecording = isRecording,
                                        recognizedText = recognizedText,
                                        hasEvaluated = lastScore != null
                                    )

                                    lastScore?.let { score ->
                                        val isPassed = score >= 75
                                        Spacer(modifier = Modifier.height(18.dp))
                                        Text(
                                            text = "Pronunciation Score",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                color = Slate500,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        Text(
                                            text = "$score%",
                                            style = MaterialTheme.typography.displayMedium.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                color = when {
                                                    score >= 75 -> Emerald600
                                                    score >= 50 -> Orange600
                                                    else -> Red500
                                                }
                                            )
                                        )

                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isPassed) Emerald50 else PolishAmberBg,
                                            border = BorderStroke(1.dp, if (isPassed) Emerald500 else Orange500),
                                            modifier = Modifier.padding(top = 4.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = if (isPassed) Icons.Default.CheckCircle else Icons.Default.Info,
                                                    contentDescription = null,
                                                    tint = if (isPassed) Emerald600 else Orange600,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = if (isPassed) "Lesson Passed (>= 75%)" else "Need 75% to complete lesson (Current: $score%)",
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isPassed) Emerald600 else Orange600
                                                    )
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))
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

                                        Spacer(modifier = Modifier.height(18.dp))
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(10.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                OutlinedButton(
                                                    onClick = { viewModel.playRecordedVoice() },
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(48.dp),
                                                    shape = RoundedCornerShape(14.dp),
                                                    border = BorderStroke(1.dp, if (isPlayingBack) Red500 else PolishTeal),
                                                    colors = ButtonDefaults.outlinedButtonColors(
                                                        contentColor = if (isPlayingBack) Red500 else PolishTeal
                                                    )
                                                ) {
                                                    Icon(
                                                        imageVector = if (isPlayingBack) Icons.Default.Stop else Icons.Default.PlayArrow,
                                                        contentDescription = "Play"
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = if (isPlayingBack) "Stop" else "Hear Voice",
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }

                                                if (hasNextLesson && nextLesson != null) {
                                                    Button(
                                                        onClick = {
                                                            viewModel.activeGrammarLesson.value = nextLesson
                                                            viewModel.lastScore.value = null
                                                            viewModel.scoredWords.value = emptyList()
                                                            viewModel.recognizedText.value = ""
                                                        },
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .height(48.dp),
                                                        shape = RoundedCornerShape(14.dp),
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = PolishTeal,
                                                            contentColor = Color.White
                                                        )
                                                    ) {
                                                        Text("Next Lesson", color = Color.White, fontWeight = FontWeight.Bold)
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
                                                    containerColor = if (hasNextLesson) Slate100 else PolishTeal,
                                                    contentColor = if (hasNextLesson) PolishNavy else Color.White
                                                ),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(48.dp),
                                                shape = RoundedCornerShape(14.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Map,
                                                    contentDescription = "Map View",
                                                    tint = if (hasNextLesson) PolishNavy else Color.White
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = if (hasNextLesson) "Finish Lesson (View Next on Map)" else "Complete Lesson Trail",
                                                    color = if (hasNextLesson) PolishNavy else Color.White,
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
                Text("No grammar lesson selected.", color = Slate500)
            }
        }
    }
}
