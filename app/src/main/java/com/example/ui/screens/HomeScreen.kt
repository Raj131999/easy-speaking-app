package com.example.ui.screens

import java.util.Calendar

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
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    viewModel: EnglishViewModel,
    modifier: Modifier = Modifier
) {
    val progress by viewModel.userProgress.collectAsState()
    val grammar by viewModel.grammarLessons.collectAsState()
    val convs by viewModel.conversations.collectAsState()
    val sentences by viewModel.dailySentences.collectAsState()
    val paragraphs by viewModel.paragraphs.collectAsState()
    val twisters by viewModel.tongueTwisters.collectAsState()

    // Calculate module completions
    val grammarComp = grammar.count { it.isCompleted }.toFloat() / (grammar.size.takeIf { it > 0 } ?: 1)
    val convComp = convs.count { it.isCompleted }.toFloat() / (convs.size.takeIf { it > 0 } ?: 1)
    val sentenceComp = sentences.count { it.timesPracticed > 0 }.toFloat() / (sentences.size.takeIf { it > 0 } ?: 1)
    val paraComp = paragraphs.count { it.isCompleted }.toFloat() / (paragraphs.size.takeIf { it > 0 } ?: 1)
    val twisterComp = twisters.count { it.isCompleted }.toFloat() / (twisters.size.takeIf { it > 0 } ?: 1)

    // Filter weak points (accuracy < 85)
    val weakSentences = sentences.filter { it.timesPracticed > 0 && it.lastAccuracy < 85 }
    val weakTwisters = twisters.filter { it.practiceCount > 0 && it.maxAccuracy < 85 }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        // --- STREAK HERO HEADER ---
        item {
            StreakHeaderCard(progress = progress)
        }

        // --- WEEKLY WORKOUT PRACTICE SUMMARY ---
        item {
            WeeklyPracticeGrid(progress = progress)
        }

        // --- SPACED REPETITION REVIEW / MASTERY CARD ---
        item {
            ReviewHubCard(sentences = sentences, onReviewClick = {
                val reviewItem = sentences.firstOrNull { it.reviewScheduledTime > 0 } ?: sentences.firstOrNull()
                if (reviewItem != null) {
                    viewModel.activeDailySentence.value = reviewItem
                    viewModel.navigateTo(Screen.DailySentences)
                }
            })
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
                convComp = convComp,
                sentenceComp = sentenceComp,
                paraComp = paraComp,
                twisterComp = twisterComp,
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

        // --- WEAK POINTS (SPACED REPETITION) ---
        if (weakSentences.isNotEmpty() || weakTwisters.isNotEmpty()) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Weak Areas",
                        tint = Color(0xFFFF4D4D),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Struggled Sounds & Phrases",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                }
            }

            items(weakSentences.take(3)) { item ->
                WeakItemCard(
                    title = "Daily Sentence",
                    subtext = item.text,
                    accuracy = item.lastAccuracy,
                    focus = item.soundFocus.takeIf { it.isNotEmpty() } ?: "Fluency",
                    onClick = {
                        viewModel.activeDailySentence.value = item
                        viewModel.navigateTo(Screen.DailySentences)
                    }
                )
            }

            items(weakTwisters.take(3)) { item ->
                WeakItemCard(
                    title = "Tongue Twister",
                    subtext = item.text,
                    accuracy = item.maxAccuracy,
                    focus = item.soundFocus,
                    onClick = {
                        viewModel.activeTongueTwister.value = item
                        viewModel.navigateTo(Screen.TongueTwister)
                    }
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

    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = PolishTealLight
        ),
        border = BorderStroke(1.dp, PolishTealLightBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("streak_header_card")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "TODAY'S WORKOUT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = PolishTeal,
                        letterSpacing = 1.2.sp
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (streak > 0) "$streak Day Streak! ($totalXp XP)" else "Start Today's Lesson!",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = PolishNavy
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Daily Goal: $todayXp/$targetXp XP",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            color = PolishTeal
                        )
                    )
                    Text(
                        text = "${(progressFraction * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = PolishTeal
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
                    color = PolishTeal,
                    trackColor = Color.White.copy(alpha = 0.5f),
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // White elegant card container for the flame / active workout badge
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, PolishTealLightBorder, RoundedCornerShape(16.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = "Streak Flame",
                    tint = Orange600,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun WeeklyPracticeGrid(progress: UserProgress?) {
    val days = listOf("S", "M", "T", "W", "T", "F", "S")
    val todayCal = Calendar.getInstance()
    val todayDayOfWeek = todayCal.get(Calendar.DAY_OF_WEEK) // 1 = Sun, 2 = Mon ...

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
                days.forEachIndexed { index, day ->
                    val isToday = index + 1 == todayDayOfWeek
                    val isPracticed = index + 1 <= todayDayOfWeek && (progress?.currentStreak ?: 0) > 0

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
                                    text = day,
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
                imageVector = Icons.Default.ArrowForwardIos,
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
    convComp: Float,
    sentenceComp: Float,
    paraComp: Float,
    twisterComp: Float,
    onGrammarTap: () -> Unit,
    onConversationTap: () -> Unit,
    onSentencesTap: () -> Unit,
    onParagraphTap: () -> Unit,
    onTwisterTap: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SkillTreeNode(
            title = "Grammar Lessons",
            subtext = "Syllable stress, gonna/wanna...",
            progress = grammarComp,
            icon = Icons.Default.MenuBook,
            color = PolishTeal,
            onClick = onGrammarTap
        )

        SkillTreeConnector()

        SkillTreeNode(
            title = "Conversation Sets",
            subtext = "Ordering Food, Job Interviews...",
            progress = convComp,
            icon = Icons.Default.RecordVoiceOver,
            color = Color(0xFF6C5CE7),
            onClick = onConversationTap
        )

        SkillTreeConnector()

        SkillTreeNode(
            title = "Daily Sentences",
            subtext = "200 daily workout phrases",
            progress = sentenceComp,
            icon = Icons.Default.DirectionsRun,
            color = Orange600,
            onClick = onSentencesTap
        )

        SkillTreeConnector()

        SkillTreeNode(
            title = "Paragraph Reading",
            subtext = "Fluency & Intonation practice",
            progress = paraComp,
            icon = Icons.Default.TextFields,
            color = Color(0xFFFD79A8),
            onClick = onParagraphTap
        )

        SkillTreeConnector()

        SkillTreeNode(
            title = "Tongue Twisters",
            subtext = "Sound drills ('th', 'r/l')",
            progress = twisterComp,
            icon = Icons.Default.MusicNote,
            color = Color(0xFF00CEC9),
            onClick = onTwisterTap
        )
    }
}

@Composable
fun SkillTreeNode(
    title: String,
    subtext: String,
    progress: Float,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    val (nodeBgColor, nodeIconBgColor, nodeTextColor) = when (color) {
        PolishTeal -> Triple(Color.White, PolishIndigoBg, PolishIndigoText)
        Color(0xFF6C5CE7) -> Triple(Color.White, PolishTealBg, PolishTealText)
        Orange600 -> Triple(Color.White, PolishAmberBg, PolishAmberText)
        Color(0xFFFD79A8) -> Triple(Color.White, PolishIndigoBg, PolishIndigoText)
        else -> Triple(Color.White, PolishTealBg, PolishTealText)
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = nodeBgColor),
        border = BorderStroke(1.dp, Slate100),
        modifier = Modifier
            .fillMaxWidth(0.92f)
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
                    trackColor = nodeTextColor.copy(alpha = 0.1f),
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
            }

            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = nodeTextColor
                )
            )
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
fun WeakItemCard(
    title: String,
    subtext: String,
    accuracy: Int,
    focus: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Red50),
        border = BorderStroke(1.dp, Red100),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Red500)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Red900,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Red100)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Sound: $focus",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Red900,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtext,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Red700,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "$accuracy%",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Red900
                )
            )
        }
    }
}
