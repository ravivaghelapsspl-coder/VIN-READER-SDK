package com.pssplvinsdkdemo

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun HomeScreen(
    onScanVIN: () -> Unit,
    scanStatus: ScanStatus
) {
    val bgColor = Color(0xFFF4F5F9)
    val primaryBlue = Color(0xFF007AFF)
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "VIN Scanner",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(64.dp))
            
            // Scanner Icon Graphic
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(Color(0xFFE2EAF8), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(64.dp)) {
                    val stroke = Stroke(width = 8f, cap = StrokeCap.Round)
                    val color = primaryBlue
                    val length = 16.dp.toPx()
                    val corner = 8.dp.toPx()
                    
                    // Top Left
                    drawPath(
                        path = androidx.compose.ui.graphics.Path().apply {
                            moveTo(0f, length)
                            lineTo(0f, corner)
                            quadraticTo(0f, 0f, corner, 0f)
                            lineTo(length, 0f)
                        },
                        color = color,
                        style = stroke
                    )
                    // Top Right
                    drawPath(
                        path = androidx.compose.ui.graphics.Path().apply {
                            moveTo(size.width - length, 0f)
                            lineTo(size.width - corner, 0f)
                            quadraticTo(size.width, 0f, size.width, corner)
                            lineTo(size.width, length)
                        },
                        color = color,
                        style = stroke
                    )
                    // Bottom Left
                    drawPath(
                        path = androidx.compose.ui.graphics.Path().apply {
                            moveTo(0f, size.height - length)
                            lineTo(0f, size.height - corner)
                            quadraticTo(0f, size.height, corner, size.height)
                            lineTo(length, size.height)
                        },
                        color = color,
                        style = stroke
                    )
                    // Bottom Right
                    drawPath(
                        path = androidx.compose.ui.graphics.Path().apply {
                            moveTo(size.width, size.height - length)
                            lineTo(size.width, size.height - corner)
                            quadraticTo(size.width, size.height, size.width - corner, size.height)
                            lineTo(size.width - length, size.height)
                        },
                        color = color,
                        style = stroke
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Ready to Scan",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Align the vehicle identification number within the camera guide for automatic detection.",
                fontSize = 15.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp),
                lineHeight = 22.sp
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = onScanVIN,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryBlue,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.QrCodeScanner,
                    contentDescription = "Scan Icon",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Start VIN Scan",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            if (scanStatus !is ScanStatus.None) {
                Spacer(modifier = Modifier.height(32.dp))
                
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "LAST SCAN RESULT",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            when (scanStatus) {
                                is ScanStatus.Cancelled -> {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .background(Color(0xFFF38B2A), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Cancelled",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column {
                                            Text(text = "Cancelled", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                                            Text(text = scanStatus.message, color = Color.Gray, fontSize = 14.sp)
                                        }
                                    }
                                }
                                is ScanStatus.Error -> {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .background(Color.Red, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Error",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column {
                                            Text(text = "Error", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                                            Text(text = scanStatus.message, color = Color.Gray, fontSize = 14.sp)
                                        }
                                    }
                                }
                                is ScanStatus.Success -> {
                                    val result = scanStatus.result
                                    val format = SimpleDateFormat("dd MMM yyyy 'at' h:mm a", Locale.getDefault())
                                    val dateStr = format.format(result.timestamp)
                                    val confStr = "${(result.confidence * 100).toInt()}.0%"
                                    
                                    ResultRow("VIN", result.vin, primaryBlue, isBoldValue = true)
                                    HorizontalDivider(color = Color(0xFFF0F0F0))
                                    ResultRow("Confidence", confStr, Color.Black)
                                    HorizontalDivider(color = Color(0xFFF0F0F0))
                                    ResultRow("Date", dateStr, Color.Black)
                                }
                                else -> {}
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ResultRow(label: String, value: String, valueColor: Color, isBoldValue: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color.Gray, fontSize = 15.sp)
        Text(
            text = value,
            color = valueColor,
            fontSize = 15.sp,
            fontWeight = if (isBoldValue) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}
