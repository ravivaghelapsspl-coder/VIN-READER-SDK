# VIN-READER-SDK

An isolated, lightweight Android SDK for optically scanning and validating Vehicle Identification Numbers (VINs) via ML Kit Text Recognition with support for ISO 3779 checksum mathematics.

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
    implementation("com.github.ravivaghelapsspl-coder:VIN-READER-SDK:1.0.9")
}
```

## API Documentation

### [#](#vin-scanner-contract)VINScannerContract
[#](#vin-scanner-contract)
Represents the standard Android Activity Result Contract used to launch the scanner Activity and return the scanned result safely to your component.

- `registerForActivityResult(VINScannerContract())`: this function registers the contract in your Activity and returns an ActivityResultLauncher.

```kotlin
registerForActivityResult(VINScannerContract()) { result: VINScannerCode? ->
    // implementation
}
```

### [#](#vin-scanner-configuration)VINScannerConfiguration
[#](#vin-scanner-configuration)
Represents the configuration class parameter used to customize the behavior and UI constraints of the SDK.

- `iso3779Enabled`: Determines if strictly validating mathematical ISO 3779 Checksums is enabled by default. Default: `false`.
- `flashMode`: Determines the default starting mode of the camera torch capability using `VINScannerFlashMode.ON` or `OFF`. Default: `VINScannerFlashMode.OFF`.

```kotlin
VINScannerConfiguration(
    iso3779Enabled = false,
    flashMode = VINScannerFlashMode.OFF
)
```

### [#](#vin-scanner-code)VINScannerCode
[#](#vin-scanner-code)
Represents the result dataset emitted by the ML Kit text analyzer containing exactly 17 corrected characters.

- `data`: retrieving the parsed 17-digit string representing the VIN.
- `confidence`: retrieving the Float tracking the optical accuracy/checksum validity.

## Usage Example

Implementation of the complete pipeline within a ComponentActivity. 

```kotlin
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import com.psspl.vinsdk.*

class YourActivity: ComponentActivity() {
    
    private var scannerScreenLauncher: ActivityResultLauncher<VINScannerConfiguration>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        scannerScreenLauncher = registerForActivityResult(VINScannerContract()) { result: VINScannerCode? ->
            if (result != null) {
                println("Scanned VIN: ${result.data}")
            } else {
                println("Scan was dismissed")
            }
        }
        
        // When a button is clicked... 
        openScanner(scannerScreenLauncher)        
    }

    private fun openScanner(cameraScreen: ActivityResultLauncher<VINScannerConfiguration>?) {
        val scannerConfiguration = VINScannerConfiguration(
            iso3779Enabled = false,
            flashMode = VINScannerFlashMode.OFF
        )
        cameraScreen?.launch(scannerConfiguration)
    }
}
```
