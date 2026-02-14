package com.wristborn.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*
import com.wristborn.app.data.ProgressionData
import com.wristborn.app.engine.ArenaManager

@Composable
fun ProfileScreen(
    arenaManager: ArenaManager,
    onBack: () -> Unit
) {
    val progression by arenaManager.progressionManager.progressionFlow.collectAsState(initial = ProgressionData())
    val listState = rememberScalingLazyListState()

    Scaffold(timeText = { TimeText() }) {
        ScalingLazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item { Text("Duelist Profile", style = MaterialTheme.typography.title3) }
            
            item {
                Text(
                    "Level ${progression.level}",
                    color = Color.Cyan,
                    style = MaterialTheme.typography.title2
                )
            }
            
            item {
                Text(
                    "${progression.xp} XP",
                    style = MaterialTheme.typography.caption2
                )
            }

            item {
                Text("Wins: ${progression.totalWins} (Streak: ${progression.winStreak})", style = MaterialTheme.typography.body2)
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Element Mastery", style = MaterialTheme.typography.caption1)
            }

            item { MasteryRow("Fire", progression.fireMastery) }
            item { MasteryRow("Wind", progression.windMastery) }
            item { MasteryRow("Arcane", progression.arcaneMastery) }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Your Share Code", style = MaterialTheme.typography.caption2)
            }
            
            item {
                Text(
                    generateShareCode(arenaManager),
                    style = MaterialTheme.typography.title3,
                    color = Color.Yellow
                )
            }

            item {
                Button(onClick = onBack, modifier = Modifier.padding(top = 8.dp)) {
                    Text("Back")
                }
            }
        }
    }
}

@Composable
fun MasteryRow(label: String, value: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(0.8f),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.body2)
        Text(value.toString(), color = Color.Gray)
    }
}

private fun generateShareCode(arenaManager: ArenaManager): String {
    // Simple mock share code for MVP
    return "WB-7X2P"
}
