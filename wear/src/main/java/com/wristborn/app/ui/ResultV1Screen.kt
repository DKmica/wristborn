package com.wristborn.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*
import com.wristborn.app.data.ProgressionData
import com.wristborn.app.engine.ArenaManager
import kotlinx.coroutines.launch

@Composable
fun ResultV1Screen(
    arenaManager: ArenaManager,
    winner: String,
    onRematch: () -> Unit,
    onClose: () -> Unit
) {
    val progression by arenaManager.progressionManager.progressionFlow.collectAsState(initial = ProgressionData())
    val scope = rememberCoroutineScope()
    val isWin = winner == "PLAYER"

    // Record win/loss on first entry
    LaunchedEffect(Unit) {
        if (isWin) {
            arenaManager.progressionManager.recordWin()
        } else if (winner == "OPPONENT") {
            arenaManager.progressionManager.recordLoss()
        }
    }

    Scaffold(timeText = { TimeText() }) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isWin) "VICTORY" else if (winner == "DRAW") "DRAW" else "DEFEAT",
                style = MaterialTheme.typography.display3,
                color = if (isWin) Color.Green else if (winner == "DRAW") Color.Yellow else Color.Red,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "STREAK: ${progression.winStreak}",
                style = MaterialTheme.typography.caption1,
                color = if (progression.winStreak >= 3) Color.Magenta else Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "SHARE CODE: ${progression.shareCode ?: "------"}",
                style = MaterialTheme.typography.caption2,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = onRematch,
                    colors = ButtonDefaults.primaryButtonColors(),
                    modifier = Modifier.size(ButtonDefaults.SmallButtonSize)
                ) {
                    Text("RE")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onClose,
                    colors = ButtonDefaults.secondaryButtonColors(),
                    modifier = Modifier.size(ButtonDefaults.SmallButtonSize)
                ) {
                    Text("X")
                }
            }
        }
    }
}
