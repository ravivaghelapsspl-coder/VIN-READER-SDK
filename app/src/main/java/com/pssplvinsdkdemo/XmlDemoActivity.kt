package com.pssplvinsdkdemo

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.ComposeView

class XmlDemoActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_xml_demo)

        val btnScan = findViewById<Button>(R.id.btnScan)
        val tvVinResult = findViewById<TextView>(R.id.tvVinResult)
        val composeView = findViewById<ComposeView>(R.id.composeView)

        btnScan.setOnClickListener {
            // Hide XML UI
            btnScan.visibility = View.GONE
            tvVinResult.visibility = View.GONE
            
            // Show ComposeView (our SDK view)
            composeView.visibility = View.VISIBLE
            
            // Inject Compose Screen
            composeView.setContent {
                ScannerScreen(
                    title = "Scan VIN",
                    boxColor = androidx.compose.ui.graphics.Color(0xFF00AEEF),
                    onClose = {
                        // Reshow XML UI on Close
                        composeView.visibility = View.GONE
                        btnScan.visibility = View.VISIBLE
                        tvVinResult.visibility = View.VISIBLE
                    },
                    onScanned = { result ->
                        // Show scanned data in XML UI
                        tvVinResult.text = "Scanned VIN: ${result.vin}\nConfidence: ${result.confidence}"
                        composeView.visibility = View.GONE
                        btnScan.visibility = View.VISIBLE
                        tvVinResult.visibility = View.VISIBLE
                    },
                    onError = { errorMsg ->
                        tvVinResult.text = "Error: $errorMsg"
                        composeView.visibility = View.GONE
                        btnScan.visibility = View.VISIBLE
                        tvVinResult.visibility = View.VISIBLE
                    }
                )
            }
        }
    }
}
