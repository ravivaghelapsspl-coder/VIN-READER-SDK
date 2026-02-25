package com.pssplvinsdkdemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pssplvinsdkdemo.ui.theme.VINSDKDemoTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.layout.Box

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VINSDKDemoTheme {
                var currentScreen by remember { mutableStateOf("home") }
                var vinNumber by remember { mutableStateOf("") }
                var scannerType by remember { mutableStateOf("") }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                        when (currentScreen) {
                            "home" -> {
                                HomeScreen(
                                    onScanVIN = {
                                        currentScreen = "scanner"
                                    },
                                    vinNumber = vinNumber,
                                    onVinNumberChange = { vinNumber = it.uppercase() }
                                )
                            }
                            "scanner" -> {
                                ScannerScreen(
                                    title = "Place the entire VIN inside the box",
                                    boxColor = androidx.compose.ui.graphics.Color(0xFF00AEEF),
                                    onClose = { currentScreen = "home" },
                                    onScanned = { result ->
                                        vinNumber = result
                                        currentScreen = "home"
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