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
    implementation("com.github.ravivaghelapsspl-coder.VIN-READER-SDK:vinsdk:1.0.3")
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
            onScanned = { vinString ->
                // Automatically triggers when a valid structured VIN is found inside the frame
                println("Found VIN: $vinString")
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

## Features
- **Clean Compose API:** Purely built in `androidx.compose`.
- **UI Agnostic:** The SDK returns the data; you build the look.
- **High Accuracy & ISO Parsing:** Supports built-in smart parsing, multi-line spanning (scanning multiple broken VIN blocks and verifying if combining them forms a logical checksum), and optional strict ISO 3779 Checksum verification.
