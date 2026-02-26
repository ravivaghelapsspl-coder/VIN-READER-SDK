package com.pssplvinsdkdemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.pssplvinsdkdemo.ui.theme.VINSDKDemoTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.layout.Box
import com.psspl.vinsdk.VinResult

sealed class ScanStatus {
    object None : ScanStatus()
    data class Success(val result: VinResult) : ScanStatus()
    data class Cancelled(val message: String = "The scan was dismissed by the user.") : ScanStatus()
    data class Error(val message: String) : ScanStatus()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VINSDKDemoTheme {
                var currentScreen by remember { mutableStateOf(AppConstants.SCREEN_HOME) }
                var scanStatus by remember { mutableStateOf<ScanStatus>(ScanStatus.None) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                        when (currentScreen) {
                            AppConstants.SCREEN_HOME -> {
                                HomeScreen(
                                    onScanVIN = {
                                        currentScreen = AppConstants.SCREEN_SCANNER
                                    },
                                    scanStatus = scanStatus
                                )
                            }
                            AppConstants.SCREEN_SCANNER -> {
                                ScannerScreen(
                                    title = AppConstants.TITLE_SCANNER,
                                    boxColor = androidx.compose.ui.graphics.Color(0xFF00AEEF),
                                    onClose = { 
                                        scanStatus = ScanStatus.Cancelled()
                                        currentScreen = AppConstants.SCREEN_HOME 
                                    },
                                    onScanned = { result ->
                                        scanStatus = ScanStatus.Success(result)
                                        currentScreen = AppConstants.SCREEN_HOME
                                    },
                                    onError = { errorMsg ->
                                        scanStatus = ScanStatus.Error(errorMsg)
                                        currentScreen = AppConstants.SCREEN_HOME
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}