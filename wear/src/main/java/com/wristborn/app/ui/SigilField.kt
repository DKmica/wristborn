package com.wristborn.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.wristborn.app.engine.SigilToken

@Composable
fun SigilField(
    onTokenCaptured: (SigilToken) -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .size(100.dp)
            .background(
                if (isPressed) Color.White.copy(alpha = 0.3f) else Color.DarkGray,
                CircleShape
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        val startTime = System.currentTimeMillis()
                        try {
                            awaitRelease()
                            val duration = System.currentTimeMillis() - startTime
                            if (duration < 300) {
                                onTokenCaptured(SigilToken.SHORT)
                            } else {
                                onTokenCaptured(SigilToken.LONG)
                            }
                        } finally {
                            isPressed = false
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color.Cyan.copy(alpha = 0.6f), CircleShape)
        )
    }
}
