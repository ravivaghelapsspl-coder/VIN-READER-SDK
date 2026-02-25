package com.psspl.vinsdk

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

object VINValidator {
    fun isValidStructure(vin: String): Boolean {
        return vin.matches(Regex("^[A-HJ-NPR-Z0-9]{17}$"))
    }

    fun validateChecksum(vin: String): Boolean {
        if (!isValidStructure(vin)) return false
        val map = mapOf(
            'A' to 1, 'B' to 2, 'C' to 3, 'D' to 4, 'E' to 5, 'F' to 6, 'G' to 7, 'H' to 8,
            'J' to 1, 'K' to 2, 'L' to 3, 'M' to 4, 'N' to 5, 'P' to 7, 'R' to 9,
            'S' to 2, 'T' to 3, 'U' to 4, 'V' to 5, 'W' to 6, 'X' to 7, 'Y' to 8, 'Z' to 9,
            '0' to 0, '1' to 1, '2' to 2, '3' to 3, '4' to 4, '5' to 5, '6' to 6, '7' to 7, '8' to 8, '9' to 9
        )
        val weights = intArrayOf(8, 7, 6, 5, 4, 3, 2, 10, 0, 9, 8, 7, 6, 5, 4, 3, 2)
        var sum = 0
        for (i in 0 until 17) {
            val value = map[vin[i]] ?: return false
            sum += value * weights[i]
        }
        val remainder = sum % 11
        val expectedCheckChar = if (remainder == 10) 'X' else remainder.toString()[0]
        return vin[8] == expectedCheckChar
    }
    
    fun validate(vin: String, shouldVerifyChecksum: Boolean): Boolean {
        val cleanedVin = vin.trim().uppercase()
            .replace("*", "")
            .replace("O", "0")
            .replace("I", "1")
            .replace("Q", "0")
        
        if (!isValidStructure(cleanedVin)) return false
        if (!shouldVerifyChecksum) return true
        
        return validateChecksum(cleanedVin)
    }
}

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
@Composable
fun VINScannerCamera(
    modifier: Modifier = Modifier,
    isFlashlightOn: Boolean = false,
    shouldVerifyChecksum: Boolean = false,
    onScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    val shouldVerifyChecksumState = rememberUpdatedState(shouldVerifyChecksum)
    var cameraControl by remember { mutableStateOf<androidx.camera.core.CameraControl?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    if (hasCameraPermission) {
        val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

        DisposableEffect(cameraControl) {
            onDispose {
                cameraControl?.enableTorch(false)
            }
        }

        DisposableEffect(Unit) {
            onDispose {
                cameraExecutor.shutdown()
            }
        }

        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val hasMatched = java.util.concurrent.atomic.AtomicBoolean(false)

                        val imageAnalyzer = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also {
                                it.setAnalyzer(cameraExecutor) { imageProxy ->
                                    val mediaImage = imageProxy.image
                                    if (mediaImage != null) {
                                        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                                        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

                                        recognizer.process(image)
                                            .addOnSuccessListener { visionText ->
                                                if (hasMatched.get()) return@addOnSuccessListener
                                                
                                                // Calculate bounding box in image coordinates
                                                val rotation = imageProxy.imageInfo.rotationDegrees
                                                val isRotated = rotation == 90 || rotation == 270
                                                val imageWidth = if (isRotated) image.height.toFloat() else image.width.toFloat()
                                                val imageHeight = if (isRotated) image.width.toFloat() else image.height.toFloat()
                                                
                                                // Center 85% width, 12% height box
                                                val boxWidthOffset = imageWidth * 0.85f
                                                val boxHeightOffset = imageHeight * 0.12f
                                                val left = (imageWidth - boxWidthOffset) / 2f
                                                val top = (imageHeight - boxHeightOffset) / 2f
                                                val right = left + boxWidthOffset
                                                val bottom = top + boxHeightOffset
                                                
                                                val scannerBox = android.graphics.Rect(
                                                    left.toInt(),
                                                    top.toInt(),
                                                    right.toInt(),
                                                    bottom.toInt()
                                                )

                                                val validBlocks = mutableListOf<Pair<String, android.graphics.Rect>>()
                                                for (block in visionText.textBlocks) {
                                                    val blockBox = block.boundingBox
                                                    if (blockBox != null) {
                                                        Log.d("ScannerScreen", "Found Block anywhere: ${block.text.replace("\n", " ")} at bounds: $blockBox")
                                                    }
                                                    // Only scan if the text block intersects strongly within the scannerBox
                                                    if (blockBox != null) {
                                                        // Ensure the block's vertical height is significantly inside the 12% optical bracket.
                                                        // Often blockBoxes might bleed slightly depending on ML Kit's framing
                                                        val intersection = android.graphics.Rect(blockBox)
                                                        val doesIntersect = intersection.intersect(scannerBox)
                                                        
                                                        // Require at least 60% of the recognized text's height and width to be inside the box
                                                        val isMostlyInside = doesIntersect &&
                                                            (intersection.height().toFloat() / blockBox.height() > 0.6f) &&
                                                            (intersection.width().toFloat() / blockBox.width() > 0.6f)
                                                        
                                                        if (isMostlyInside) {
                                                            Log.d("ScannerScreen", "Block INSIDE scanner list: ${block.text.replace("\n", " ")}")
                                                            val rawText = block.text.uppercase()
                                                                .replace("O", "0")
                                                                .replace("I", "1")
                                                                .replace("Q", "0")
                                                                .replace(" ", "")
                                                                .replace("-", "")
                                                                .replace("_", "")
                                                            
                                                            if (rawText.length >= 6) {
                                                                validBlocks.add(Pair(rawText, blockBox))
                                                            }
                                                        } else {
                                                            Log.d("ScannerScreen", "Block OUTSIDE or intersecting weakly: ${block.text.replace("\n", " ")}")
                                                        }
                                                    }
                                                }
                                                
                                                validBlocks.sortBy { it.second.top } // Top-to-bottom

                                                var foundVin: String? = null
                                                val verifyChecksum = shouldVerifyChecksumState.value
                                                
                                                for (block in validBlocks) {
                                                    var text = block.first
                                                    if (text.length == 20 && (text.startsWith("VIN") || text.startsWith("V1N"))) text = text.substring(3)
                                                    else if (text.length == 26 && text.startsWith("CHASSISN0")) text = text.substring(9)
                                                    else if (text.length == 24 && text.startsWith("CHASSIS")) text = text.substring(7)
                                                    
                                                    if (VINValidator.validate(text, verifyChecksum)) {
                                                        foundVin = text
                                                        break
                                                    }
                                                }
                                                
                                                if (foundVin == null) {
                                                    run twoLineCheck@{
                                                        for (i in 0 until validBlocks.size) {
                                                            for (j in (i + 1) until validBlocks.size) {
                                                                val merged = validBlocks[i].first + validBlocks[j].first
                                                                if (merged.length == 17 && VINValidator.validate(merged, verifyChecksum)) {
                                                                    foundVin = merged
                                                                    return@twoLineCheck
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                                
                                                if (foundVin != null) {
                                                    Log.d("ScannerScreen", "✅ Valid matched VIN: $foundVin")
                                                    if (hasMatched.compareAndSet(false, true)) {
                                                        onScanned(foundVin!!)
                                                    }
                                                    return@addOnSuccessListener
                                                }
                                            }
                                            .addOnFailureListener { e ->
                                                Log.e("ScannerScreen", "Text recognition failed", e)
                                            }
                                            .addOnCompleteListener {
                                                imageProxy.close()
                                            }
                                    } else {
                                        imageProxy.close()
                                    }
                                }
                            }

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                        try {
                            cameraProvider.unbindAll()
                            val camera = cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalyzer
                            )
                            cameraControl = camera.cameraControl
                        } catch (exc: Exception) {
                            Log.e("ScannerScreen", "Use case binding failed", exc)
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                update = {
                    cameraControl?.enableTorch(isFlashlightOn)
                },
                modifier = modifier
            )
        }
    } else {
        // Permission denied or loading
        Box(modifier = modifier.background(Color.Black), contentAlignment = Alignment.Center) {
            Text(
                text = "Camera permission is required to use the scanner",
                color = Color.White,
                modifier = Modifier.padding(32.dp)
            )
        }
    }
}
