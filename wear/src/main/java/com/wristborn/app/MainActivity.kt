package com.wristborn.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.wear.compose.material.Text
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.wristborn.app.data.ProgressionData
import com.wristborn.app.engine.ArenaManager
import com.wristborn.app.ui.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WristbornApp()
        }
    }
}

@Composable
fun WristbornApp() {
    val context = LocalContext.current
    val navController = rememberSwipeDismissableNavController()
    val arenaManager = remember { ArenaManager(context) }
    val scope = rememberCoroutineScope()

    val progression by arenaManager.progressionManager.progressionFlow.collectAsStateWithLifecycle(initialValue = ProgressionData())

    // Permission handling
    val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    } else {
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    var permissionsGranted by remember {
        mutableStateOf(
            permissionsToRequest.all {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        permissionsGranted = results.values.all { it }
    }

    LaunchedEffect(Unit) {
        if (!permissionsGranted) {
            launcher.launch(permissionsToRequest)
        }
    }

    if (permissionsGranted) {
        val startDest = if (progression.hasCompletedOnboarding) {
            if (FeatureFlags.V1_VIRAL_MODE) Routes.ARENA_V1 else Routes.ARENA
        } else {
            Routes.ONBOARDING
        }

        SwipeDismissableNavHost(
            navController = navController,
            startDestination = startDest
        ) {
            composable(Routes.ONBOARDING) {
                OnboardingScreen(onComplete = {
                    scope.launch {
                        arenaManager.progressionManager.completeOnboarding()
                        val target = if (FeatureFlags.V1_VIRAL_MODE) Routes.ARENA_V1 else Routes.ARENA
                        navController.navigate(target) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                    }
                })
            }
            
            composable(Routes.ARENA) {
                ArenaIdleScreen(
                    arenaManager = arenaManager,
                    onTraining = { navController.navigate(Routes.TRAINING) },
                    onPractice = { navController.navigate(Routes.DUEL) },
                    onPvPDuel = { navController.navigate(Routes.PVP_DUEL) },
                    onProfile = { navController.navigate(Routes.PROFILE) },
                    onDailyTrial = { navController.navigate(Routes.DAILY_TRIAL) }
                )
            }
            composable(Routes.TRAINING) {
                TrainingScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.DUEL) {
                DuelScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.PVP_DUEL) {
                PvPDuelScreen(
                    arenaManager = arenaManager,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.PROFILE) {
                ProfileScreen(
                    arenaManager = arenaManager,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.DAILY_TRIAL) {
                DailyTrialScreen(
                    arenaManager = arenaManager,
                    onBack = { navController.popBackStack() }
                )
            }

            // Viral V1 Routes
            composable(Routes.ARENA_V1) {
                ArenaV1Screen(
                    arenaManager = arenaManager,
                    onPractice = { navController.navigate(Routes.DUEL_V1) },
                    onProfile = { navController.navigate(Routes.PROFILE) },
                    onPvPDuel = { navController.navigate(Routes.PVP_DUEL_V1) }
                )
            }
            composable(Routes.DUEL_V1) {
                DuelV1Screen(
                    arenaManager = arenaManager,
                    onBack = { navController.popBackStack() },
                    onResult = { winner -> navController.navigate(Routes.resultV1(winner)) }
                )
            }
            composable(Routes.PVP_DUEL_V1) {
                PvPDuelV1Screen(
                    arenaManager = arenaManager,
                    onBack = { navController.popBackStack() },
                    onResult = { winner -> navController.navigate(Routes.resultV1(winner)) }
                )
            }
            composable(
                route = Routes.RESULT_V1,
                arguments = listOf(navArgument("winner") { type = NavType.StringType })
            ) { backStackEntry ->
                val winner = backStackEntry.arguments?.getString("winner") ?: "DRAW"
                ResultV1Screen(
                    arenaManager = arenaManager,
                    winner = winner,
                    onRematch = { 
                        navController.popBackStack(Routes.ARENA_V1, false)
                        navController.navigate(Routes.DUEL_V1) 
                    },
                    onClose = { navController.popBackStack(Routes.ARENA_V1, false) }
                )
            }
        }
    } else {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                "Bluetooth permissions required.",
                textAlign = TextAlign.Center
            )
        }
    }
}
