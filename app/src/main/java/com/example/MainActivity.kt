package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.EnglishRepository
import com.example.ui.*
import com.example.ui.screens.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: EnglishViewModel

    // Request Mic permission launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Permission result handled gracefully; if denied, we use simulated voice capture
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize SQLite Room DB
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = EnglishRepository(database.englishDao())
        
        // Instantiate ViewModel
        val factory = EnglishViewModelFactory(application, repository)
        viewModel = ViewModelProvider(this, factory)[EnglishViewModel::class.java]

        // Check/request MIC permission proactively for smooth offline flow
        checkMicrophonePermission()

        setContent {
            MyApplicationTheme {
                MainShell(viewModel = viewModel)
            }
        }
    }

    private fun checkMicrophonePermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
}

@Composable
fun MainShell(viewModel: EnglishViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val rewardAmount by viewModel.showRewardOverlay.collectAsState()
    val progress by viewModel.userProgress.collectAsState()

    // Temporary local context
    val context = LocalContext.current

    Scaffold(
        topBar = {
            if (currentScreen == Screen.Home) {
                HeaderBar(progress = progress, onProfileClick = { viewModel.navigateTo(Screen.Settings) })
            }
        },
        bottomBar = {
            // Display standard Bottom Navigation bar on Home and Settings only
            if (currentScreen == Screen.Home || currentScreen == Screen.Settings) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .border(width = 1.dp, color = Slate100, shape = RoundedCornerShape(0.dp))
                ) {
                    NavigationBarItem(
                        selected = currentScreen == Screen.Home,
                        onClick = { viewModel.navigateTo(Screen.Home) },
                        icon = { Icon(imageVector = Icons.Default.DirectionsRun, contentDescription = "Gym") },
                        label = { Text("Daily Gym") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PolishTeal,
                            selectedTextColor = PolishTeal,
                            indicatorColor = PolishTealLight,
                            unselectedIconColor = Slate400,
                            unselectedTextColor = Slate400
                        ),
                        modifier = Modifier.testTag("nav_item_gym")
                    )

                    NavigationBarItem(
                        selected = currentScreen == Screen.Settings,
                        onClick = { viewModel.navigateTo(Screen.Settings) },
                        icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PolishTeal,
                            selectedTextColor = PolishTeal,
                            indicatorColor = PolishTealLight,
                            unselectedIconColor = Slate400,
                            unselectedTextColor = Slate400
                        ),
                        modifier = Modifier.testTag("nav_item_settings")
                    )
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // State-Based Content Router
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "ScreenTransition"
            ) { screen ->
                val padModifier = Modifier.padding(
                    top = if (screen == Screen.Home) innerPadding.calculateTopPadding() else 0.dp,
                    bottom = if (screen == Screen.Home || screen == Screen.Settings) innerPadding.calculateBottomPadding() else 0.dp
                )

                when (screen) {
                    Screen.Home -> HomeScreen(viewModel = viewModel, modifier = padModifier)
                    Screen.Grammar -> GrammarScreen(viewModel = viewModel, modifier = padModifier)
                    Screen.Conversation -> ConversationScreen(viewModel = viewModel, modifier = padModifier)
                    Screen.DailySentences -> DailySentencesScreen(viewModel = viewModel, modifier = padModifier)
                    Screen.Paragraph -> ParagraphScreen(viewModel = viewModel, modifier = padModifier)
                    Screen.TongueTwister -> TongueTwistersScreen(viewModel = viewModel, modifier = padModifier)
                    Screen.Settings -> SettingsScreen(viewModel = viewModel, modifier = padModifier)
                }
            }

            // --- REWARD / GAMIFICATION XP CELEBRATION OVERLAY ---
            rewardAmount?.let { xp ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    // Celebration Banner
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(Orange600.copy(alpha = 0.2f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Grade,
                                    contentDescription = "Star",
                                    tint = Orange600,
                                    modifier = Modifier.size(44.dp)
                                )
                            }

                            Text(
                                text = "EXCELLENT WORK!",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Orange600,
                                    letterSpacing = 1.2.sp
                                )
                            )

                            Text(
                                text = "Your spoken English is improving. Keep up the daily workout!",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )

                            Text(
                                text = "+$xp XP AWARDED",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PolishTeal
                                )
                            )

                            Button(
                                onClick = { viewModel.showRewardOverlay.value = null },
                                colors = ButtonDefaults.buttonColors(containerColor = PolishTeal),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Awesome!", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Auto-dismiss the overlay after 2.5 seconds
                    LaunchedEffect(xp) {
                        delay(2500)
                        viewModel.showRewardOverlay.value = null
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderBar(progress: com.example.data.UserProgress?, onProfileClick: () -> Unit) {
    val streak = progress?.currentStreak ?: 0
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(64.dp)
            .background(PolishBg)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "ENGLISH LEARNER",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Slate500,
                    letterSpacing = 1.5.sp,
                    fontSize = 11.sp
                )
            )
            Text(
                text = "easy speaking",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = PolishNavy
                )
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Streak badge with orange styling
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Orange50)
                    .border(1.dp, Orange100, RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "$streak",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Orange600
                    )
                )
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(Orange500),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(Color.White)
                    )
                }
            }

            // User profile button / avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(Slate200)
                    .border(2.dp, Color.White, androidx.compose.foundation.shape.CircleShape)
                    .clickable { onProfileClick() },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(Slate400)
                )
            }
        }
    }
}

