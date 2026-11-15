package com.example.ui.screens

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.EnglishViewModel
import com.example.ui.Screen
import com.example.ui.components.HolographicCard
import com.example.ui.components.PhoneticMouthGuideDialog
import com.example.ui.components.SpeechBubblePopDialog
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    viewModel: EnglishViewModel,
    modifier: Modifier = Modifier
) {
    val progress by viewModel.userProgress.collectAsState()
    val appOpenDates by viewModel.appOpenDates.collectAsState()
    val grammar by viewModel.grammarLessons.collectAsState()
    val convs by viewModel.conversations.collectAsState()
    val sentences by viewModel.dailySentences.collectAsState()
    val paragraphs by viewModel.paragraphs.collectAsState()
    val twisters by viewModel.tongueTwisters.collectAsState()
    val missedWordReviews by viewModel.missedWordReviews.collectAsState()
    val dismissedKeys by viewModel.dismissedWeakItemKeys.collectAsState()
    val isStruggledSectionDismissed by viewModel.isStruggledSectionDismissed.collectAsState()

    var showMouthGuideDialog by remember { mutableStateOf(false) }
    var showBubblePopDialog by remember { mutableStateOf(false) }

    // Calculate module completions and counts
    val grammarDone = grammar.count { it.isCompleted }
    val grammarTotal = grammar.size.takeIf { it > 0 } ?: 1
    val grammarComp = grammarDone.toFloat() / grammarTotal

    val convDone = convs.count { it.isCompleted }
    val convTotal = convs.size.takeIf { it > 0 } ?: 1
    val convComp = convDone.toFloat() / convTotal

    val sentenceDone = sentences.count { it.isCompleted || it.lastAccuracy >= 75 }
    val sentenceTotal = sentences.size.takeIf { it > 0 } ?: 1
    val sentenceComp = sentenceDone.toFloat() / sentenceTotal

    val paraDone = paragraphs.count { it.isCompleted }
    val paraTotal = paragraphs.size.takeIf { it > 0 } ?: 1
    val paraComp = paraDone.toFloat() / paraTotal

    val twisterDone = twisters.count { it.isCompleted || it.maxAccuracy >= 75 }
    val twisterTotal = twisters.size.takeIf { it > 0 } ?: 1
    val twisterComp = twisterDone.toFloat() / twisterTotal

    // Filter focus points (only items that haven't passed the 75% threshold, and not dismissed)
    val weakSentences = sentences.filter {
        it.timesPracticed > 0 && it.lastAccuracy < 75 && !dismissedKeys.contains("sentence_${it.id}")
    }
    val weakTwisters = twisters.filter {
        it.practiceCount > 0 && it.maxAccuracy < 75 && !dismissedKeys.contains("twister_${it.id}")
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(PolishBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp)
    ) {
        // --- STREAK HERO HEADER (3D Holographic Card) ---
        item {
            StreakHeaderCard(progress = progress)
        }

        // --- VOICE STUDIO BENTO TOOLS ROW ---
        item {
            VoiceStudioToolsRow(
                onOpenMouthGuide = { showMouthGuideDialog = true },
                onOpenWarmupGame = { showBubblePopDialog = true }
            )
        }

        // --- WEEKLY WORKOUT PRACTICE SUMMARY ---
        item {
            WeeklyPracticeGrid(progress = progress, appOpenDates = appOpenDates)
        }

        // --- SPACED REPETITION REVIEW / MASTERY CARD ---
        item {
            ReviewHubCard(sentences = sentences, onReviewClick = {
                viewModel.startReviewSession()
            })
        }

        // --- MISSED WORDS SPACED REVIEW CARD ---
        val activeMissedWords = missedWordReviews.filter { it.consecutiveCorrect < 2 }
        if (activeMissedWords.isNotEmpty()) {
            item {
                MissedWordsSpacedReviewCard(
                    words = activeMissedWords.take(10),
                    onWordTap = { word -> viewModel.speak(word) },
                    onPracticeClick = { viewModel.startReviewSession() }
                )
            }
        }

        // --- COURSE MAP / SKILL TREE TITLE ---
        item {
            Text(
                text = "Course Curriculum",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
        }

        // --- SKILL TREE PATH ---
        item {
            SkillTreePath(
                grammarComp = grammarComp,
                grammarDone = grammarDone,
                grammarTotal = grammar.size,
                convComp = convComp,
                convDone = convDone,
                convTotal = convs.size,
                sentenceComp = sentenceComp,
                sentenceDone = sentenceDone,
                sentenceTotal = sentences.size,
                paraComp = paraComp,
                paraDone = paraDone,
                paraTotal = paragraphs.size,
                twisterComp = twisterComp,
                twisterDone = twisterDone,
                twisterTotal = twisters.size,
                onGrammarTap = {
                    viewModel.activeGrammarLesson.value = null
                    viewModel.navigateTo(Screen.Grammar)
                },
                onConversationTap = {
                    viewModel.activeConversation.value = null
                    viewModel.navigateTo(Screen.Conversation)
                },
                onSentencesTap = {
                    viewModel.activeDailySentence.value = null
                    viewModel.navigateTo(Screen.DailySentences)
                },
                onParagraphTap = {
                    viewModel.activeParagraph.value = null
                    viewModel.navigateTo(Screen.Paragraph)
                },
                onTwisterTap = {
                    viewModel.activeTongueTwister.value = null
                    viewModel.navigateTo(Screen.TongueTwister)
                }
            )
        }

        // --- TARGETED PRONUNCIATION FOCUS DRILLS ---
        if (!isStruggledSectionDismissed && (weakSentences.isNotEmpty() || weakTwisters.isNotEmpty())) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Pronunciation Focus",
                        tint = PolishTeal,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Pronunciation Focus Drills",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                        Text(
                            text = "Targeted drills to reach the 75% passing mark",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                    IconButton(
                        onClick = { viewModel.dismissStruggledSection() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Hide Pronunciation Focus",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            items(weakSentences.take(3)) { item ->
                FocusPracticeCard(
                    title = "Daily Sentence",
                    subtext = item.text,
                    accuracy = item.lastAccuracy,
                    focus = item.soundFocus.takeIf { it.isNotEmpty() } ?: "Fluency",
                    onClick = {
                        viewModel.startReviewSession(item)
                    },
                    onDismiss = {
                        viewModel.dismissWeakItem("sentence_${item.id}")
                    }
                )
            }

            items(weakTwisters.take(3)) { item ->
                FocusPracticeCard(
                    title = "Tongue Twister",
                    subtext = item.text,
                    accuracy = item.maxAccuracy,
                    focus = item.soundFocus,
                    onClick = {
                        viewModel.activeTongueTwister.value = item
                        viewModel.navigateTo(Screen.TongueTwister)
                    },
                    onDismiss = {
                        viewModel.dismissWeakItem("twister_${item.id}")
                    }
                )
            }
        }
    }

    if (showMouthGuideDialog) {
        PhoneticMouthGuideDialog(
            initialSound = "TH",
            onDismiss = { showMouthGuideDialog = false },
            onSpeakWord = { word -> viewModel.speak(word) }
        )
    }

    if (showBubblePopDialog) {
        SpeechBubblePopDialog(
            onSpeakWord = { word -> viewModel.speak(word) },
            onClose = { showBubblePopDialog = false }
        )
    }
}

@Composable
fun VoiceStudioToolsRow(
    onOpenMouthGuide: () -> Unit,
    onOpenWarmupGame: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Slate100),
            modifier = Modifier
                .weight(1f)
                .clickable { onOpenMouthGuide() }
                .testTag("open_mouth_guide_card")
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PolishIndigoBg)
                ) {
                    Icon(
                        imageVector = Icons.Default.RecordVoiceOver,
                        contentDescription = "Phonetics Mouth Guide",
                        tint = PolishIndigoText,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "Mouth Guide",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = PolishNavy
                    )
                )
                Text(
                    text = "Visual tongue & lip cues",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate500),
                    maxLines = 1
                )
            }
        }

        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Slate100),
            modifier = Modifier
                .weight(1f)
                .clickable { onOpenWarmupGame() }
                .testTag("open_warmup_game_card")
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PolishTealBg)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Speech Bubble Warmup",
                        tint = PolishTeal,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "Bubble Warmup",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = PolishNavy
                    )
                )
                Text(
                    text = "Interactive sound pop",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate500),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun StreakHeaderCard(progress: UserProgress?) {
    val streak = progress?.currentStreak ?: 0
    val todayXp = progress?.todayXP ?: 0
    val targetXp = progress?.dailyGoalXP ?: 50
    val totalXp = progress?.totalXP ?: 0
    val progressFraction = (todayXp.toFloat() / targetXp).coerceIn(0f, 1f)

    HolographicCard(
        maxTiltAngle = 8f,
        cornerRadius = 26.dp,
        containerColor = PolishNavy,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("streak_header_card")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            PolishNavy,
                            Color(0xFF134E4A)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "TODAY'S WORKOUT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5EEAD4),
                            letterSpacing = 1.2.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (streak > 0) "$streak Day Streak · $totalXp XP" else "Start Today's Session",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Daily Goal: $todayXp / $targetXp XP",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFCCFBF1)
                            )
                        )
                        Text(
                            text = "${(progressFraction * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF5EEAD4)
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF2DD4BF),
                        trackColor = Color.White.copy(alpha = 0.18f),
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.14f))
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Streak Flame",
                        tint = Color(0xFFFB923C),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun WeeklyPracticeGrid(progress: UserProgress?, appOpenDates: List<String>) {
    val days = listOf("S", "M", "T", "W", "T", "F", "S")
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    val cal = Calendar.getInstance()
    val todayStr = sdf.format(cal.time)
    val currentDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1 = Sun ... 7 = Sat

    val weekDates = (1..7).map { dayOfWeekIndex ->
        val dayCal = Calendar.getInstance()
        dayCal.add(Calendar.DAY_OF_YEAR, dayOfWeekIndex - currentDayOfWeek)
        sdf.format(dayCal.time)
    }

    val activeSet = appOpenDates.toSet()

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Slate100),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "WEEKLY PRACTICE CALENDAR",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Slate500,
                    letterSpacing = 1.5.sp
                )
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                days.forEachIndexed { index, dayLabel ->
                    val dateStr = weekDates[index]
                    val isToday = dateStr == todayStr
                    val isPracticed = activeSet.contains(dateStr)

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isPracticed -> PolishTeal
                                        isToday -> PolishTealLight
                                        else -> Slate50
                                    }
                                )
                                .border(
                                    width = 1.dp,
                                    color = when {
                                        isPracticed -> PolishTeal
                                        isToday -> PolishTealLightBorder
                                        else -> Slate100
                                    },
                                    shape = CircleShape
                                )
                        ) {
                            if (isPracticed) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Completed",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                                Text(
                                    text = dayLabel,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = when {
                                            isToday -> PolishTeal
                                            else -> Slate400
                                        }
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewHubCard(
    sentences: List<DailySentence>,
    onReviewClick: () -> Unit
) {
    val reviewCount = sentences.count { it.timesPracticed > 0 }
    if (reviewCount == 0) return

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .clickable { onReviewClick() }
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Slate800)
            ) {
                Icon(
                    imageVector = Icons.Default.Autorenew,
                    contentDescription = "Review",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Review Mode",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    text = "Tackle your weak points and practice previous material.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Slate400
                    )
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = "Go",
                tint = Slate400,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun SkillTreePath(
    grammarComp: Float,
    grammarDone: Int,
    grammarTotal: Int,
    convComp: Float,
    convDone: Int,
    convTotal: Int,
    sentenceComp: Float,
    sentenceDone: Int,
    sentenceTotal: Int,
    paraComp: Float,
    paraDone: Int,
    paraTotal: Int,
    twisterComp: Float,
    twisterDone: Int,
    twisterTotal: Int,
    onGrammarTap: () -> Unit,
    onConversationTap: () -> Unit,
    onSentencesTap: () -> Unit,
    onParagraphTap: () -> Unit,
    onTwisterTap: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SkillTreeNode(
            title = "Grammar Lessons",
            subtext = "Syllable stress, gonna/wanna...",
            doneCount = grammarDone,
            totalCount = grammarTotal,
            progress = grammarComp,
            icon = Icons.AutoMirrored.Filled.MenuBook,
            color = PolishIndigoText,
            onClick = onGrammarTap
        )

        SkillTreeConnector()

        SkillTreeNode(
            title = "Conversation Sets",
            subtext = "Ordering Food, Job Interviews...",
            doneCount = convDone,
            totalCount = convTotal,
            progress = convComp,
            icon = Icons.Default.RecordVoiceOver,
            color = PolishTeal,
            onClick = onConversationTap
        )

        SkillTreeConnector()

        SkillTreeNode(
            title = "Daily Sentences",
            subtext = "200 daily workout phrases",
            doneCount = sentenceDone,
            totalCount = sentenceTotal,
            progress = sentenceComp,
            icon = Icons.Default.Bolt,
            color = Orange600,
            onClick = onSentencesTap
        )

        SkillTreeConnector()

        SkillTreeNode(
            title = "Paragraph Reading",
            subtext = "Fluency & Intonation practice",
            doneCount = paraDone,
            totalCount = paraTotal,
            progress = paraComp,
            icon = Icons.Default.Article,
            color = Rose500,
            onClick = onParagraphTap
        )

        SkillTreeConnector()

        SkillTreeNode(
            title = "Tongue Twisters",
            subtext = "Sound drills ('th', 'r/l')",
            doneCount = twisterDone,
            totalCount = twisterTotal,
            progress = twisterComp,
            icon = Icons.Default.GraphicEq,
            color = Violet600,
            onClick = onTwisterTap
        )
    }
}

@Composable
fun SkillTreeNode(
    title: String,
    subtext: String,
    doneCount: Int,
    totalCount: Int,
    progress: Float,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    val (nodeIconBgColor, nodeTextColor) = when (color) {
        PolishIndigoText -> PolishIndigoBg to PolishIndigoText
        PolishTeal -> PolishTealBg to PolishTealText
        Orange600 -> PolishAmberBg to PolishAmberText
        Rose500 -> Rose50 to Rose500
        else -> Violet50 to Violet600
    }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Slate100),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(54.dp)
            ) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    color = nodeTextColor,
                    trackColor = nodeTextColor.copy(alpha = 0.12f),
                    strokeWidth = 3.5.dp
                )
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(nodeIconBgColor)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = nodeTextColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = PolishNavy
                    )
                )
                Text(
                    text = subtext,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Slate500,
                        fontWeight = FontWeight.Medium
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$doneCount / $totalCount completed",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = nodeTextColor,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = nodeTextColor
                    )
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Slate400,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun SkillTreeConnector() {
    Box(
        modifier = Modifier
            .width(2.dp)
            .height(18.dp)
            .background(Slate200)
    )
}

@Composable
fun FocusPracticeCard(
    title: String,
    subtext: String,
    accuracy: Int,
    focus: String,
    onClick: () -> Unit,
    onDismiss: (() -> Unit)? = null
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = PolishTeal,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(PolishTeal.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Sound: $focus",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = PolishTeal,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = subtext,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Current: $accuracy% · Goal: 75% to pass",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = StreakGold,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    )
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action button to retry drill
            FilledTonalButton(
                onClick = onClick,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = PolishTeal.copy(alpha = 0.15f),
                    contentColor = PolishTeal
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Practice drill",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Retry", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
            }

            if (onDismiss != null) {
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss item",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun WeakItemCard(
    title: String,
    subtext: String,
    accuracy: Int,
    focus: String,
    onClick: () -> Unit,
    onDismiss: (() -> Unit)? = null
) {
    FocusPracticeCard(title, subtext, accuracy, focus, onClick, onDismiss)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MissedWordsSpacedReviewCard(
    words: List<MissedWordReview>,
    onWordTap: (String) -> Unit,
    onPracticeClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Slate100),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("missed_words_review_card")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "FOCUS WORDS FOR REVIEW",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = PolishTeal,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = "Tap any word to hear it clearly",
                        style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                    )
                }
                TextButton(onClick = onPracticeClick) {
                    Text("Practice", color = PolishTeal, fontWeight = FontWeight.Bold)
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                words.forEach { entry ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Orange50,
                        border = BorderStroke(1.dp, Orange100),
                        modifier = Modifier.clickable { onWordTap(entry.word) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Hear ${entry.word}",
                                tint = Orange600,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = entry.word,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PolishNavy
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
