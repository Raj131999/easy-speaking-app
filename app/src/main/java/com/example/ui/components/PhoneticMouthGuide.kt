package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

data class PhoneticGuideItem(
    val sound: String,
    val title: String,
    val ipa: String,
    val practiceWords: List<String>,
    val tonguePlacement: String,
    val lipPlacement: String,
    val airflowHint: String,
    val isVoiced: Boolean
)

val PHONETIC_PRESETS = listOf(
    PhoneticGuideItem(
        sound = "TH",
        title = "The 'TH' Sound",
        ipa = "/θ/ & /ð/",
        practiceWords = listOf("Think", "Three", "Breathe", "Together", "Weather"),
        tonguePlacement = "Place the tip of your tongue gently between your upper and lower front teeth.",
        lipPlacement = "Relax your lips and keep your mouth slightly parted.",
        airflowHint = "Blow air continuously through the gap without biting your tongue.",
        isVoiced = true
    ),
    PhoneticGuideItem(
        sound = "R vs L",
        title = "'R' vs 'L' Distinction",
        ipa = "/r/ vs /l/",
        practiceWords = listOf("Right / Light", "Read / Lead", "Red / Led", "Room / Loom"),
        tonguePlacement = "For 'R': Curl tongue back, DO NOT touch the roof. For 'L': Press tongue tip firmly against the ridge behind front teeth.",
        lipPlacement = "For 'R': Slightly round your lips. For 'L': Neutral smiling lips.",
        airflowHint = "Air flows smoothly around sides of tongue for 'L'.",
        isVoiced = true
    ),
    PhoneticGuideItem(
        sound = "V vs W",
        title = "'V' vs 'W' Placement",
        ipa = "/v/ vs /w/",
        practiceWords = listOf("Vine / Wine", "Vest / West", "Vary / Wary", "Voice / Wise"),
        tonguePlacement = "Tongue rests low in mouth for both.",
        lipPlacement = "For 'V': Rest your top teeth on your lower inner lip. For 'W': Round your lips tight into a circle (no teeth contact).",
        airflowHint = "For 'V': Air causes friction against the lower lip. For 'W': Air pushes smoothly through the rounded mouth.",
        isVoiced = true
    ),
    PhoneticGuideItem(
        sound = "S vs SH",
        title = "'S' vs 'SH' Clarity",
        ipa = "/s/ vs /ʃ/",
        practiceWords = listOf("See / She", "Sea / Shore", "Sip / Ship", "Class / Crash"),
        tonguePlacement = "For 'S': Tongue tip stays right behind upper teeth. For 'SH': Pull tongue blade slightly backward toward the roof.",
        lipPlacement = "For 'S': Wide smile. For 'SH': Flare/pout your lips forward.",
        airflowHint = "For 'S': Sharp narrow whistling hiss. For 'SH': Soft broad whoosh of air.",
        isVoiced = false
    )
)

/**
 * 2D/3D Anatomical Tongue & Mouth Placement Visualizer (Phonetics Guide)
 * 100% offline, pure Jetpack Compose Canvas cross-section diagram.
 */
@Composable
fun PhoneticMouthVisualizer(
    phoneticItem: PhoneticGuideItem,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "airflow_anim")
    val airflowPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "airflow_phase"
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // 1. Background Grid & Coordinates
                val gridColor = Color(0xFF1E293B)
                for (x in 0 until w.toInt() step 40) {
                    drawLine(gridColor, Offset(x.toFloat(), 0f), Offset(x.toFloat(), h), 1f)
                }

                // Center reference point for mouth anatomy
                val mouthX = w * 0.42f
                val mouthY = h * 0.48f

                // 2. Upper Palate & Teeth (Light ivory)
                val palatePath = Path().apply {
                    moveTo(w * 0.15f, mouthY - 45f)
                    cubicTo(
                        w * 0.28f, mouthY - 55f,
                        mouthX - 20f, mouthY - 40f,
                        mouthX + 10f, mouthY - 15f
                    )
                }
                drawPath(palatePath, color = Color(0xFF64748B), style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round))

                // Upper Incisors (Teeth)
                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(mouthX + 8f, mouthY - 14f),
                    size = Size(14.dp.toPx(), 20.dp.toPx()),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )

                // Upper Lip (Profile outline)
                val upperLipPath = Path().apply {
                    moveTo(mouthX + 10f, mouthY - 35f)
                    cubicTo(
                        mouthX + 35f, mouthY - 25f,
                        mouthX + 45f, mouthY - 12f,
                        mouthX + 26f, mouthY - 8f
                    )
                }
                drawPath(upperLipPath, color = Color(0xFFFF7675), style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round))

                // 3. Lower Jaw & Lower Teeth
                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(mouthX + 4f, mouthY + 16f),
                    size = Size(14.dp.toPx(), 20.dp.toPx()),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )

                // Lower Lip
                val lowerLipPath = Path().apply {
                    moveTo(mouthX + 24f, mouthY + 22f)
                    cubicTo(
                        mouthX + 45f, mouthY + 26f,
                        mouthX + 35f, mouthY + 45f,
                        mouthX + 10f, mouthY + 50f
                    )
                }
                drawPath(lowerLipPath, color = Color(0xFFFF7675), style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round))

                // 4. Dynamic Tongue Profile based on Sound
                val tonguePath = Path()
                val tongueColor = Color(0xFFE17055)
                val tongueGradient = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFF7675), Color(0xFFD63031)),
                    startY = mouthY - 30f,
                    endY = mouthY + 50f
                )

                when (phoneticItem.sound) {
                    "TH" -> {
                        // Tongue tip extends between upper and lower teeth!
                        tonguePath.moveTo(w * 0.18f, mouthY + 30f)
                        tonguePath.cubicTo(
                            mouthX - 30f, mouthY + 20f,
                            mouthX - 5f, mouthY + 8f,
                            mouthX + 22f, mouthY + 2f // protruding between teeth
                        )
                        tonguePath.cubicTo(
                            mouthX + 15f, mouthY + 14f,
                            mouthX - 10f, mouthY + 35f,
                            w * 0.18f, mouthY + 45f
                        )
                    }
                    "R vs L" -> {
                        // Curved retroflex tongue (R) or alveolar contact (L)
                        tonguePath.moveTo(w * 0.18f, mouthY + 30f)
                        tonguePath.cubicTo(
                            mouthX - 35f, mouthY + 10f,
                            mouthX - 10f, mouthY - 25f, // curled back tip
                            mouthX - 8f, mouthY - 12f
                        )
                        tonguePath.cubicTo(
                            mouthX - 25f, mouthY + 15f,
                            mouthX - 35f, mouthY + 35f,
                            w * 0.18f, mouthY + 45f
                        )
                    }
                    "V vs W" -> {
                        // Lower lip meets upper teeth, tongue relaxed
                        tonguePath.moveTo(w * 0.18f, mouthY + 30f)
                        tonguePath.cubicTo(
                            mouthX - 30f, mouthY + 25f,
                            mouthX - 10f, mouthY + 18f,
                            mouthX, mouthY + 18f
                        )
                        tonguePath.cubicTo(
                            mouthX - 10f, mouthY + 35f,
                            mouthX - 25f, mouthY + 45f,
                            w * 0.18f, mouthY + 45f
                        )
                    }
                    else -> {
                        // S vs SH
                        tonguePath.moveTo(w * 0.18f, mouthY + 30f)
                        tonguePath.cubicTo(
                            mouthX - 30f, mouthY + 10f,
                            mouthX - 5f, mouthY - 6f, // just behind front teeth
                            mouthX + 4f, mouthY - 4f
                        )
                        tonguePath.cubicTo(
                            mouthX - 10f, mouthY + 25f,
                            mouthX - 20f, mouthY + 40f,
                            w * 0.18f, mouthY + 45f
                        )
                    }
                }
                drawPath(tonguePath, brush = tongueGradient)
                drawPath(tonguePath, color = Color.White.copy(alpha = 0.35f), style = Stroke(width = 2.dp.toPx()))

                // 5. Airflow Stream Animation (Cyan glowing arrows moving out)
                val airflowY = mouthY + 2f
                val startAirX = mouthX - 20f
                val totalAirDist = w * 0.45f

                for (dotIdx in 0..4) {
                    val progress = ((airflowPhase + dotIdx * 0.2f) % 1f)
                    val currentX = startAirX + progress * totalAirDist
                    val alpha = (1f - progress).coerceIn(0.1f, 1f)
                    drawCircle(
                        color = Color(0xFF00CEC9).copy(alpha = alpha),
                        radius = (3.5f + (1f - progress) * 3f).dp.toPx(),
                        center = Offset(currentX, airflowY - progress * 4f)
                    )
                }
            }

            // Overlay Diagram Badge
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(14.dp)
            ) {
                Text(
                    text = "CROSS-SECTION 2D/3D VISUALIZER",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00CEC9),
                        letterSpacing = 1.sp
                    )
                )
                Text(
                    text = phoneticItem.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }

            // IPA Symbol Badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(14.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF334155))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = phoneticItem.ipa,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = StreakGold
                    )
                )
            }
        }
    }
}

fun resolvePhoneticGuideSound(soundFocus: String): String {
    val norm = soundFocus.trim().lowercase()
    return when {
        norm.contains("r/l") || norm.contains("r vs l") || norm == "r" || norm == "l" -> "R vs L"
        norm.contains("v/w") || norm.contains("v vs w") || norm == "v" || norm == "w" -> "V vs W"
        norm.contains("sh") || norm.contains("s/sh") || norm.contains("s vs sh") || norm == "s" -> "S vs SH"
        else -> "TH"
    }
}

/**
 * Full interactive Phonetics Guide modal dialog
 */
@Composable
fun PhoneticMouthGuideDialog(
    initialSound: String = "TH",
    onDismiss: () -> Unit,
    onSpeakWord: (String) -> Unit
) {
    var selectedIndex by remember {
        mutableStateOf(PHONETIC_PRESETS.indexOfFirst { it.sound == initialSound }.coerceAtLeast(0))
    }
    val currentPreset = PHONETIC_PRESETS[selectedIndex]

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header with Close Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Phonetics & Mouth Guide",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = PolishNavy
                        )
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Sound Selector Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedIndex,
                    edgePadding = 0.dp,
                    containerColor = Color.Transparent,
                    divider = {},
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    PHONETIC_PRESETS.forEachIndexed { index, preset ->
                        Tab(
                            selected = selectedIndex == index,
                            onClick = { selectedIndex = index },
                            text = {
                                Text(
                                    text = preset.sound,
                                    fontWeight = if (selectedIndex == index) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 2D Anatomy Canvas Visualizer
                    PhoneticMouthVisualizer(phoneticItem = currentPreset)

                    // Placement Steps & Instructions
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = PolishTealLight),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PolishTealLightBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "ANATOMICAL PLACEMENT STEPS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PolishTeal,
                                    letterSpacing = 1.sp
                                )
                            )

                            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 5.dp)
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(PolishTeal)
                                )
                                Column {
                                    Text("Tongue:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PolishNavy)
                                    Text(currentPreset.tonguePlacement, fontSize = 13.sp, color = PolishNavy)
                                }
                            }

                            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 5.dp)
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(ElectricIndigo)
                                )
                                Column {
                                    Text("Lips & Teeth:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PolishNavy)
                                    Text(currentPreset.lipPlacement, fontSize = 13.sp, color = PolishNavy)
                                }
                            }

                            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 5.dp)
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Orange500)
                                )
                                Column {
                                    Text("Airflow:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PolishNavy)
                                    Text(currentPreset.airflowHint, fontSize = 13.sp, color = PolishNavy)
                                }
                            }
                        }
                    }

                    // Practice Words with Pronunciation Audio
                    Text(
                        text = "Listen & Repeat Practice Words",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = PolishNavy
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        currentPreset.practiceWords.take(3).forEach { word ->
                            OutlinedButton(
                                onClick = { onSpeakWord(word) },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Speak $word",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = word, maxLines = 1, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
