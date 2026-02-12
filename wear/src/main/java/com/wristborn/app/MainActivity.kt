package com.wristborn.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.wristborn.app.ui.ArenaIdleScreen
import com.wristborn.app.ui.DuelScreen
import com.wristborn.app.ui.Routes
import com.wristborn.app.ui.TrainingScreen

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
    val navController = rememberSwipeDismissableNavController()

    SwipeDismissableNavHost(
        navController = navController,
        startDestination = Routes.ARENA
    ) {
        composable(Routes.ARENA) {
            ArenaIdleScreen(
                onTraining = { navController.navigate(Routes.TRAINING) },
                onPractice = { navController.navigate(Routes.DUEL) }
            )
        }
        composable(Routes.TRAINING) {
            TrainingScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.DUEL) {
            DuelScreen(onBack = { navController.popBackStack() })
        }
    }
}
