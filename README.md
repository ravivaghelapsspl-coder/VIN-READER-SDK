# TruvideoSdkVIN

An isolated, lightweight iOS SDK for optically scanning and validating Vehicle Identification Numbers (VINs) via Apple Vision Text Recognition, with optional ISO 3779 checksum validation.

---

## Requirements

| Requirement | Version |
| :--- | :--- |
| Android Studio | Hedgehog (2023.1.1) or later |
| Kotlin | 2.0+ |
| Java | 11 |
| Min SDK | 24 (Android 7.0) |
| Compile SDK | 36 |

---

## Installation

### Gradle (JitPack)

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

Add the dependency in your module `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.ravivaghelapsspl-coder:VIN-READER-SDK:1.0.9")
}
```

---

## Quick Start

### Step 1 — Camera Permission

Declare the permission in your `AndroidManifest.xml`:

```xml
<uses-feature
    android:name="android.hardware.camera"
    android:required="false" />

<uses-permission android:name="android.permission.CAMERA" />
```

> [!IMPORTANT]
> The SDK handles camera permission requests internally. On first launch it shows a waiting screen, automatically re-asks if soft-denied, and shows an **"Open App Settings"** button if permanently denied — no extra code needed on your side.

---

### Step 2 — Configuration

Use `TruvideoSdkVINConfiguration` to customize the scanner before presenting it.

| Property | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `iso3779Enabled` | `Boolean` | `false` | Enforce ISO 3779 mathematical checksum before accepting a VIN. The user can toggle this from the scanner UI at runtime. |
| `flashMode` | `TruvideoSdkVINFlashMode` | `OFF` | Initial torch state — ON or OFF. The user can toggle this from the scanner UI at runtime. |

```kotlin
TruvideoSdkVINConfiguration(
    iso3779Enabled = false,
    flashMode = TruvideoSdkVINFlashMode.OFF
)
```

---

### Step 3 — Integration

#### Activity / Fragment

```kotlin
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import com.psspl.vinsdk.*

class YourActivity : ComponentActivity() {

    private var scannerLauncher: ActivityResultLauncher<TruvideoSdkVINConfiguration>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        scannerLauncher = registerForActivityResult(VINScannerContract()) { result: TruvideoSdkVINResponse? ->
            if (result != null) {
                println("VIN: ${result.data}")
                println("Confidence: ${result.confidence}")
            } else {
                println("Scan cancelled")
            }
        }

        // Trigger on a button click or any event
        openScanner()
    }

    private fun openScanner() {
        scannerLauncher?.launch(
            TruvideoSdkVINConfiguration(
                iso3779Enabled = false,
                flashMode = TruvideoSdkVINFlashMode.OFF
            )
        )
    }
}
```

#### Jetpack Compose

```kotlin
@Composable
fun ScanButton() {
    val launcher = rememberLauncherForActivityResult(VINScannerContract()) { result ->
        result?.let {
            println("VIN: ${it.data}")
            println("Confidence: ${it.confidence}")
        }
    }

    Button(onClick = {
        launcher.launch(TruvideoSdkVINConfiguration())
    }) {
        Text("Scan VIN")
    }
}
```

---

## Entities

### `TruvideoSdkVINConfiguration`

Configuration object passed at launch to initialize the scanner's behaviour.

| Property | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `iso3779Enabled` | `Boolean` | `false` | Enforce ISO 3779 checksum validation |
| `flashMode` | `TruvideoSdkVINFlashMode` | `OFF` | Initial torch state on launch |

---

### `TruvideoSdkVINFlashMode`

Controls the initial state of the device torch.

| Case | Description |
| :--- | :--- |
| `ON` | Torch turns on automatically when the scanner opens |
| `OFF` | Torch remains off (default) |

```kotlin
enum class TruvideoSdkVINFlashMode { ON, OFF }
```

---

### `TruvideoSdkVINResponse`

The result object returned via the Activity Result API on a successful scan.

| Property | Type | Description |
| :--- | :--- | :--- |
| `data` | `String` | The validated 17-character VIN string |
| `confidence` | `Float` | Scan confidence score (0.0 – 1.0) |
| `timestamp` | `Date` | The date and time when the VIN was captured |

---

### `VINScannerContract`

Standard Android Activity Result Contract used to launch the scanner and parse the result.

```kotlin
registerForActivityResult(VINScannerContract()) { result: TruvideoSdkVINResponse? ->
    // result is null if the user cancelled
}
```

---

### Confidence Score Reference

| Score | Meaning |
| :--- | :--- |
| `0.99` | Valid structure **and** ISO 3779 checksum passed |
| `0.90` | Two-line merged VIN with checksum passed |
| `0.85` | Valid structure only (checksum not enforced) |
| `0.75` | Two-line merged VIN, structure only |

---

## Privacy

TruvideoSdkVIN strictly adheres to privacy guidelines:

- **Zero tracking** — Does not collect, track, store, or transmit any user data or PII
- **Minimal permissions** — Only requires Camera access for scanning
- **Local processing** — All VIN recognition is performed entirely on-device; nothing is persisted or uploaded

---

© TruVideo. All rights reserved.
