package com.pssplvinsdkdemo

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import com.psspl.vinsdk.TruvideoSdkVINResponse
import com.psspl.vinsdk.TruvideoSdkVINConfiguration
import com.psspl.vinsdk.VINScannerContract

class XmlDemoActivity : ComponentActivity() {
    private var scannerScreenLauncher: ActivityResultLauncher<TruvideoSdkVINConfiguration>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_xml_demo)

        val btnScan = findViewById<Button>(R.id.btnScan)
        val tvVinResult = findViewById<TextView>(R.id.tvVinResult)

        scannerScreenLauncher = registerForActivityResult(VINScannerContract()) { result: TruvideoSdkVINResponse? ->
            if (result != null) {
                tvVinResult.text = "Scanned VIN: ${result.data}\nConfidence: ${result.confidence}"
            } else {
                tvVinResult.text = "Scan cancelled"
            }
        }

        btnScan.setOnClickListener {
            openScanner(scannerScreenLauncher)
        }
    }

    private fun openScanner(cameraScreen: ActivityResultLauncher<TruvideoSdkVINConfiguration>?) {
        val scannerConfiguration = TruvideoSdkVINConfiguration()
        cameraScreen?.launch(scannerConfiguration)
    }
}
