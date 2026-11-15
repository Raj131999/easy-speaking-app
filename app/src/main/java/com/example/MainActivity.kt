package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
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
import com.example.ui.components.DependencyDownloadBanner
import com.example.ui.screens.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: EnglishViewModel

    // Request Mic permission launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (::viewModel.isInitialized) {
            viewModel.needsMicPermissionRequest.value = false
            if (isGranted) {
                viewModel.clearSpeechStatusNotice()
            }
        }
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

        // Check/request MIC permission proactively for smooth speaking practice flow
        checkMicrophonePermission()

        setContent {
            val needsMicPermission by viewModel.needsMicPermissionRequest.collectAsState()
            LaunchedEffect(needsMicPermission) {
                if (needsMicPermission) {
                    viewModel.needsMicPermissionRequest.value = false
                    requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            }
            MyApplicationTheme {
                MainShell(viewModel = viewModel)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::viewModel.isInitialized) {
            viewModel.releaseResources()
        }
        MicrophoneCaptureService.stop(this)
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
    val screenStack by viewModel.screenStack.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()
    val rewardAmount by viewModel.showRewardOverlay.collectAsState()
    val progress by viewModel.userProgress.collectAsState()
    val modelDownloadState by viewModel.modelDownloadState.collectAsState()
    val speechStatusNotice by viewModel.speechStatusNotice.collectAsState()

    val activeGrammar by viewModel.activeGrammarLesson.collectAsState()
    val activeConversation by viewModel.activeConversation.collectAsState()
    val activeDailySentence by viewModel.activeDailySentence.collectAsState()
    val activeParagraph by viewModel.activeParagraph.collectAsState()
    val activeTongueTwister by viewModel.activeTongueTwister.collectAsState()

    val canGoBack = screenStack.size > 1 ||
            activeGrammar != null ||
            activeConversation != null ||
            activeDailySentence != null ||
            activeParagraph != null ||
            activeTongueTwister != null

    BackHandler(enabled = canGoBack) {
        viewModel.goBack()
    }

    var showReadyConfirmation by remember { mutableStateOf(false) }
    var wasNotReady by remember { mutableStateOf(false) }
    LaunchedEffect(modelDownloadState) {
        if (modelDownloadState !is ModelDownloadState.Ready) {
            wasNotReady = true
            showReadyConfirmation = false
        } else if (wasNotReady) {
            showReadyConfirmation = true
            delay(2500)
            showReadyConfirmation = false
            wasNotReady = false
        }
    }
    val hasBannerContent = (modelDownloadState !is ModelDownloadState.Ready) ||
        showReadyConfirmation ||
        !speechStatusNotice.isNullOrBlank()

    Scaffold(
        topBar = {
            if (currentScreen == Screen.Home) {
                HeaderBar(progress = progress)
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
            Column(modifier = Modifier.fillMaxSize()) {
                if (hasBannerContent) {
                    val bannerContainerModifier = if (currentScreen == Screen.Home) {
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                top = innerPadding.calculateTopPadding() + 8.dp,
                                start = 16.dp,
                                end = 16.dp
                            )
                    } else {
                        Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .consumeWindowInsets(WindowInsets.statusBars)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    }

                    DependencyDownloadBanner(
                        modelState = modelDownloadState,
                        speechStatusNotice = speechStatusNotice,
                        onRetryDownload = { viewModel.retryModelDownload() },
                        onDismissNotice = { viewModel.clearSpeechStatusNotice() },
                        modifier = bannerContainerModifier
                    )
                }

                // State-Based Content Router
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = "ScreenTransition",
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) { screen ->
                    val padModifier = Modifier.padding(
                        top = if (screen == Screen.Home && !hasBannerContent) innerPadding.calculateTopPadding() else 0.dp,
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
            }

            // --- REWARD / GAMIFICATION XP CELEBRATION OVERLAY ---
            rewardAmount?.let { xp ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(PolishNavy.copy(alpha = 0.45f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.88f)
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            com.example.ui.components.MascotCoach(
                                state = com.example.ui.components.MascotState.CELEBRATING,
                                coachMessage = "Awesome speaking!",
                                size = 92.dp
                            )

                            com.example.ui.components.Trophy3DBadge(
                                title = "DAILY MILESTONE",
                                xpReward = xp,
                                subtitle = "Fluency & pronunciation improving"
                            )

                            Button(
                                onClick = { viewModel.showRewardOverlay.value = null },
                                colors = ButtonDefaults.buttonColors(containerColor = PolishTeal),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                            ) {
                                Text("Continue", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    LaunchedEffect(xp) {
                        delay(2600)
                        viewModel.showRewardOverlay.value = null
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderBar(progress: com.example.data.UserProgress?) {
    val streak = progress?.currentStreak ?: 0
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(68.dp)
            .background(PolishBg)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(
                        androidx.compose.ui.graphics.Brush.linearGradient(
                            colors = listOf(PolishTeal, PolishIndigoText)
                        )
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = "Easy Speaking Logo",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column {
                Text(
                    text = "SPEECH COACH",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = PolishTeal,
                        letterSpacing = 1.4.sp
                    )
                )
                Text(
                    text = "Easy Speaking",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = PolishNavy
                    )
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Orange50,
                border = androidx.compose.foundation.BorderStroke(1.dp, Orange100)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Day streak",
                        tint = Orange600,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "$streak",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Orange600
                        )
                    )
                }
            }
        }
    }
}

