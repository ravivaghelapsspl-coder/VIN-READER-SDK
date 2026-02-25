package com.pssplvinsdkdemo

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(
    onScanVIN: () -> Unit,
    vinNumber: String,
    onVinNumberChange: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { /* Menu */ },
                    modifier = Modifier
                        .background(Color(0xFF00AEEF), CircleShape)
                        .size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = Color.White
                    )
                }
            }

            // Logo Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = Color(0xFF00AEEF), fontWeight = FontWeight.Bold, fontSize = 40.sp)) {
                            append("tru")
                        }
                        withStyle(style = SpanStyle(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 40.sp)) {
                            append("tire")
                        }
                    }
                )
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = Color.Gray, fontSize = 14.sp)) {
                            append("powered by ")
                        }
                        withStyle(style = SpanStyle(color = Color(0xFF00AEEF), fontWeight = FontWeight.Bold, fontSize = 14.sp)) {
                            append("tru")
                        }
                        withStyle(style = SpanStyle(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)) {
                            append("video")
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Car Outline
            CarOutline(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(horizontal = 64.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Enter Vehicle Information Section
            Text(
                text = "Enter Vehicle Information",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Input fields container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .background(Color(0xFF2C2C2C), RoundedCornerShape(24.dp))
                    .padding(16.dp)
            ) {
                Column {
                    // VIN Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = vinNumber,
                            onValueChange = onVinNumberChange,
                            placeholder = { Text("VIN #", color = Color.LightGray) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color(0xFF00AEEF),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = onScanVIN) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Scan VIN",
                                tint = Color(0xFF00AEEF)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Start Scan Button
            Button(
                onClick = { /* Start Scan Action */ },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 32.dp)
                    .width(200.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (vinNumber.isNotEmpty()) Color(0xFF00AEEF) else Color.Gray,
                    contentColor = if (vinNumber.isNotEmpty()) Color.White else Color.LightGray
                )
            ) {
                Text(
                    text = "Start Scan",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun CarOutline(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val strokeWidth = 1.5.dp.toPx()
        val color = Color.Gray

        // Top-down car simple lines
        val bodyRect = Rect(width * 0.3f, height * 0.1f, width * 0.7f, height * 0.9f)
        drawRoundRect(
            color = color,
            topLeft = Offset(bodyRect.left, bodyRect.top),
            size = Size(bodyRect.width, bodyRect.height),
            cornerRadius = CornerRadius(40f, 40f),
            style = Stroke(strokeWidth)
        )

        // Wheels
        val wheelWidth = width * 0.08f
        val wheelHeight = height * 0.15f
        listOf(
            Offset(width * 0.18f, height * 0.2f),
            Offset(width * 0.74f, height * 0.2f),
            Offset(width * 0.18f, height * 0.65f),
            Offset(width * 0.74f, height * 0.65f)
        ).forEach { offset ->
            drawRoundRect(
                color = color,
                topLeft = offset,
                size = Size(wheelWidth, wheelHeight),
                cornerRadius = CornerRadius(20f, 20f),
                style = Stroke(strokeWidth)
            )
        }

        // Front window
        drawPath(
            path = Path().apply {
                moveTo(width * 0.35f, height * 0.25f)
                lineTo(width * 0.65f, height * 0.25f)
                lineTo(width * 0.62f, height * 0.4f)
                lineTo(width * 0.38f, height * 0.4f)
                close()
            },
            color = color,
            style = Stroke(strokeWidth)
        )

        // Back window
        drawPath(
            path = Path().apply {
                moveTo(width * 0.35f, height * 0.75f)
                lineTo(width * 0.65f, height * 0.75f)
                lineTo(width * 0.62f, height * 0.6f)
                lineTo(width * 0.38f, height * 0.6f)
                close()
            },
            color = color,
            style = Stroke(strokeWidth)
        )
    }
}
