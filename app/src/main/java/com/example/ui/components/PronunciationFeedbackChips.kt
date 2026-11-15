package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AlignedWordFeedback
import com.example.ui.WordAlignmentStatus
import com.example.ui.WordScoreType
import com.example.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PronunciationFeedbackChips(
    alignedTimings: List<AlignedWordFeedback>,
    fallbackScoredWords: List<Pair<String, WordScoreType>>,
    playbackPositionSec: Float?,
    isPlayingBack: Boolean,
    missedWords: List<String>,
    unclearWords: List<String>,
    extraWords: List<String>,
    onWordClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val targetChips = alignedTimings.filter { it.status != WordAlignmentStatus.Extra && it.targetWord.isNotBlank() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("pronunciation_feedback_chips"),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isPlayingBack && alignedTimings.any { it.startSec != null }) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = "Playing audio",
                    tint = PolishTeal,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Following your voice word by word...",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = PolishTeal,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (targetChips.isNotEmpty()) {
                targetChips.forEach { item ->
                    val isActivePlayback = isPlayingBack &&
                        playbackPositionSec != null &&
                        item.startSec != null &&
                        item.endSec != null &&
                        playbackPositionSec >= (item.startSec - 0.08f) &&
                        playbackPositionSec <= (item.endSec + 0.14f)

                    val baseColor = when {
                        item.status == WordAlignmentStatus.Correct && !item.isUnclear -> Color(0xFF16A34A)
                        item.status == WordAlignmentStatus.Correct && item.isUnclear -> PolishAmberText
                        item.status == WordAlignmentStatus.Close -> PolishAmberText
                        else -> Red500
                    }
                    val bgColor = when {
                        isActivePlayback -> PolishTeal
                        item.status == WordAlignmentStatus.Correct && !item.isUnclear -> Color(0xFFDCFCE7)
                        item.status == WordAlignmentStatus.Correct && item.isUnclear -> PolishAmberBg
                        item.status == WordAlignmentStatus.Close -> PolishAmberBg
                        else -> Red50
                    }
                    val textColor = if (isActivePlayback) Color.White else baseColor
                    val borderColor = if (isActivePlayback) PolishNavy else baseColor.copy(alpha = 0.4f)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(bgColor)
                            .border(
                                width = if (isActivePlayback) 2.dp else 1.dp,
                                color = borderColor,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onWordClick(item.targetWord) }
                            .padding(horizontal = 9.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = item.targetWord,
                            color = textColor,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        if (item.isUnclear) {
                            Spacer(modifier = Modifier.width(3.dp))
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = "Unclear pronunciation",
                                tint = textColor,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            } else {
                fallbackScoredWords.forEach { (word, scoreType) ->
                    val color = when (scoreType) {
                        WordScoreType.Correct -> Color(0xFF16A34A)
                        WordScoreType.Hesitant -> PolishAmberText
                        WordScoreType.Incorrect -> Red500
                    }
                    val bg = when (scoreType) {
                        WordScoreType.Correct -> Color(0xFFDCFCE7)
                        WordScoreType.Hesitant -> PolishAmberBg
                        WordScoreType.Incorrect -> Red50
                    }
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(bg)
                            .clickable { onWordClick(word) }
                            .padding(horizontal = 9.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = word,
                            color = color,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }

        // Concise diagnostic breakdown for missed, unclear, and extra words
        if (missedWords.isNotEmpty() || unclearWords.isNotEmpty() || extraWords.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (missedWords.isNotEmpty()) {
                    DiagnosticRow(
                        label = "Missed",
                        words = missedWords,
                        containerColor = Red50,
                        borderColor = Red100,
                        textColor = Red700,
                        onWordClick = onWordClick
                    )
                }
                if (unclearWords.isNotEmpty()) {
                    DiagnosticRow(
                        label = "Unclear",
                        words = unclearWords,
                        containerColor = PolishAmberBg,
                        borderColor = Orange100,
                        textColor = PolishAmberText,
                        onWordClick = onWordClick
                    )
                }
                if (extraWords.isNotEmpty()) {
                    DiagnosticRow(
                        label = "Extra",
                        words = extraWords,
                        containerColor = Slate50,
                        borderColor = Slate200,
                        textColor = Slate500,
                        onWordClick = onWordClick
                    )
                }
            }
        }
    }
}

@Composable
private fun DiagnosticRow(
    label: String,
    words: List<String>,
    containerColor: Color,
    borderColor: Color,
    textColor: Color,
    onWordClick: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 7.dp)
                .clickable { words.firstOrNull()?.let(onWordClick) },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${label.uppercase()}: ",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    letterSpacing = 0.6.sp
                )
            )
            Text(
                text = words.take(6).joinToString(", ") + if (words.size > 6) " +${words.size - 6}" else "",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = PolishNavy,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}
