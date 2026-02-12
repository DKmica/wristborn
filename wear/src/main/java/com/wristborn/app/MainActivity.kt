package com.wristborn.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.wristborn.app.engine.ArenaState
import com.wristborn.app.ui.ArenaIdleScreen
import com.wristborn.app.ui.DuelScreen
import com.wristborn.app.ui.Routes
import com.wristborn.app.ui.TrainingScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val navController = rememberSwipeDismissableNavController()
                var arenaState by rememberSaveable { mutableStateOf(ArenaState.PASSIVE) }

                SwipeDismissableNavHost(
                    navController = navController,
                    startDestination = Routes.ARENA
                ) {
                    composable(Routes.ARENA) {
                        ArenaIdleScreen(
                            arenaState = arenaState,
                            onToggleArm = {
                                arenaState = if (arenaState == ArenaState.ARMED) {
                                    ArenaState.PASSIVE
                                } else {
                                    ArenaState.ARMED
                                }
                            },
                            onOpenTraining = { navController.navigate(Routes.TRAINING) },
                            onOpenDuel = { navController.navigate(Routes.DUEL) }
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
        }
    }
}
