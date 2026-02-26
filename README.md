# VIN-READER-SDK

An isolated, lightweight Android Jetpack Compose SDK for optically scanning and validating Vehicle Identification Numbers (VINs) via ML Kit Text Recognition.

## Installation 

Add the JitPack repository in your root `settings.gradle.kts`:
```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

Add the dependency to your module `build.gradle.kts`:
```kotlin
dependencies {
    implementation("com.github.ravivaghelapsspl-coder.VIN-READER-SDK:vinsdk:1.0.6")
}
```

## Usage Example (Jetpack Compose)

The SDK exposes `VINScannerCamera`, which strictly handles the camera rendering and ML Kit text recognition natively without injecting unexpected UI overlays. You simply layer your UI buttons over it using Compose!

```kotlin
import com.psspl.vinsdk.VINScannerCamera

@Composable
fun ScannerDemoScreen() {
    var isFlashlightOn by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        
        // The core, invisible scanning engine implementation
        VINScannerCamera(
            modifier = Modifier.fillMaxSize(),
            isFlashlightOn = isFlashlightOn,
            shouldVerifyChecksum = false, // Set to true to strictly enforce ISO 3779 checksum math!
            onScanned = { result ->
                // Automatically triggers when a valid structured VIN is found inside the frame
                println("Found VIN: ${result.vin} with confidence: ${result.confidence}")
            }
        )

        // Draw your own custom UI directly over it here!
        Button(
            onClick = { isFlashlightOn = !isFlashlightOn },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Text(if (isFlashlightOn) "Turn Flash Off" else "Turn Flash On")
        }
    }
}
```

## Usage Example (XML Activity)

If your project doesn't fully use Compose yet, you can still easily integrate the SDK by utilizing a `ComposeView` inside your XML layouts.

**1. Create a placeholder in your XML file (e.g., `activity_scanner.xml`):**
```xml
<androidx.compose.ui.platform.ComposeView
    android:id="@+id/composeView"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

**2. Attach the scanner in your Kotlin Activity (`ScannerActivity.kt`):**
```kotlin
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import com.psspl.vinsdk.VINScannerCamera

class ScannerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)

        val composeView = findViewById<ComposeView>(R.id.composeView)
        
        composeView.setContent {
            VINScannerCamera(
                isFlashlightOn = false,
                shouldVerifyChecksum = false,
                onScanned = { result ->
                    println("Scanned VIN: ${result.vin} | Accuracy: ${result.confidence}")
                    // Handle the result or close the activity!
                }
            )
        }
    }
}
```

## Features
- **Clean Compose API:** Purely built in `androidx.compose`.
- **UI Agnostic:** The SDK returns the data; you build the look.
- **High Accuracy & ISO Parsing:** Supports built-in smart parsing, multi-line spanning (scanning multiple broken VIN blocks and verifying if combining them forms a logical checksum), and optional strict ISO 3779 Checksum verification.
