package com.pssplvinsdkdemo

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.psspl.vinsdk.VINScannerCamera

@Composable
fun ScannerScreen(
    title: String,
    boxColor: Color,
    onClose: () -> Unit,
    onScanned: (String) -> Unit
) {
    var isFlashlightOn by remember { mutableStateOf(false) }
    var shouldVerifyChecksum by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        
        VINScannerCamera(
            modifier = Modifier.fillMaxSize(),
            isFlashlightOn = isFlashlightOn,
            shouldVerifyChecksum = shouldVerifyChecksum,
            onScanned = {
                isFlashlightOn = false
                onScanned(it)
            }
        )

        // Overlay
        ScannerOverlay(
            modifier = Modifier.fillMaxSize(),
            boxColor = boxColor
        )

        // Title Text
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.4f))
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.weight(0.6f))
        }

        // Top Close Button
        IconButton(
            onClick = {
                isFlashlightOn = false
                onClose()
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 48.dp, end = 16.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Color.White
            )
        }

        // Bottom Buttons
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 48.dp, start = 24.dp, end = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically

        ) {
            // ISO Toggle
           /* Row(
                modifier = Modifier
                    .background(
                        if (shouldVerifyChecksum) Color.Green.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.5f),
                        RoundedCornerShape(20.dp)
                    )
                    .clickable { shouldVerifyChecksum = !shouldVerifyChecksum }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ISO 3779",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(if (shouldVerifyChecksum) Color.Green else Color.White, CircleShape)
                )
            }*/

            // Flash Toggle
            Row(
                modifier = Modifier
                    .background(
                        if (isFlashlightOn) Color.Green.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.5f), 
                        RoundedCornerShape(20.dp)
                    )
                    .clickable { isFlashlightOn = !isFlashlightOn }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isFlashlightOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    contentDescription = "Flash Toggle",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isFlashlightOn) "On" else "Off",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
fun ScannerOverlay(modifier: Modifier = Modifier, boxColor: Color) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val boxWidth = width * 0.85f
        val boxHeight = height * 0.12f
        val boxLeft = (width - boxWidth) / 2f
        val boxTop = (height - boxHeight) / 2f

        // Draw translucent dark background with a transparent hole
        val path = Path().apply {
            addRect(Rect(0f, 0f, width, height))
            addRoundRect(
                RoundRect(
                    left = boxLeft,
                    top = boxTop,
                    right = boxLeft + boxWidth,
                    bottom = boxTop + boxHeight,
                    cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
                )
            )
            fillType = PathFillType.EvenOdd
        }
        drawPath(path, Color.Black.copy(alpha = 0.7f))

        // Draw the colored bounding box
        drawRoundRect(
            color = boxColor,
            topLeft = androidx.compose.ui.geometry.Offset(boxLeft, boxTop),
            size = androidx.compose.ui.geometry.Size(boxWidth, boxHeight),
            cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
            style = Stroke(width = 3.dp.toPx())
        )
    }
}
