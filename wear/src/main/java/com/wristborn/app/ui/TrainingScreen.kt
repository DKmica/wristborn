package com.wristborn.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.rememberScalingLazyListState
import com.wristborn.app.engine.Element

@Composable
fun TrainingScreen(onBack: () -> Unit) {
    val listState = rememberScalingLazyListState()
    var rounds by remember { mutableStateOf(0) }
    var streak by remember { mutableStateOf(0) }
    var bestStreak by remember { mutableStateOf(0) }
    var targetElement by remember { mutableStateOf(randomElement()) }
    var feedback by remember { mutableStateOf("Match the target element.") }

    Scaffold(timeText = { TimeText() }) {
        ScalingLazyColumn(state = listState) {
            item { Text("Element Drill") }
            item { Text("Target: ${targetElement.name}") }
            item { Text("Streak: $streak • Best: $bestStreak") }
            item { Text("Rounds: $rounds") }
            item { Text(feedback) }

            Element.entries.forEach { element ->
                item {
                    Button(onClick = {
                        rounds += 1
                        if (element == targetElement) {
                            streak += 1
                            bestStreak = maxOf(bestStreak, streak)
                            feedback = "Perfect ${element.name} cast!"
                        } else {
                            streak = 0
                            feedback = "Miss. You cast ${element.name}."
                        }
                        targetElement = randomElement()
                    }) {
                        Text(element.name)
                    }
                }
            }

            item {
                Button(onClick = {
                    rounds = 0
                    streak = 0
                    bestStreak = 0
                    feedback = "Run reset. Focus up."
                    targetElement = randomElement()
                }) {
                    Text("Reset Drill")
                }
            }

            item {
                Button(onClick = onBack) { Text("Back") }
            }
        }
    }
}

private fun randomElement(): Element = Element.entries.random()
