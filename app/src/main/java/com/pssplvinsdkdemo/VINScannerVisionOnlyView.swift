//
//  VINScannerVisionOnlyView.swift
//  Truline
//
//  Created by PinalKumar on 22/12/25.
//

import SwiftUI
import AVFoundation
import Vision

enum VINScanResult: Equatable {
    case success(String)
    case invalidChecksum
    case failure
    
    var id: Int {
        switch self {
        case .success: return 0
        case .invalidChecksum: return 1
        case .failure: return 2
        }
    }
}

final class VINValidator {
    
    // MARK: - Singleton
    static let shared = VINValidator()
    private init() {}
    
    // MARK: - Public API
    
    func validate(vin: String, shouldVerifyChecksum: Bool) -> VINScanResult {
        let vinUppercased = vin
            .trimmingCharacters(in: .whitespacesAndNewlines)
            .uppercased().replacingOccurrences(of: "*", with: "")
        
        guard isValidStructure(vinUppercased) else {
            return .failure
        }
        
        if !shouldVerifyChecksum {
            return .success(vinUppercased)
        }
        
        return validateVINChecksum(vinUppercased)
        ? .success(vinUppercased)
        : .invalidChecksum
    }
    
    // MARK: - Structure Validation
    
    private func isValidStructure(_ vin: String) -> Bool {
        // Strict North American VIN regex (excludes I, O, Q)
        let pattern = "^[A-HJ-NPR-Z0-9]{17}$"
        
        guard let regex = try? NSRegularExpression(pattern: pattern) else {
            return false
        }
        
        let range = NSRange(location: 0, length: vin.count)
        return regex.firstMatch(in: vin, options: [], range: range) != nil
    }
    
    // MARK: - Checksum Validation (ISO 3779)
    
    private func validateVINChecksum(_ vin: String) -> Bool {
        let map: [Character: Int] = [
            "A":1,"B":2,"C":3,"D":4,"E":5,"F":6,"G":7,"H":8,
            "J":1,"K":2,"L":3,"M":4,"N":5,"P":7,"R":9,
            "S":2,"T":3,"U":4,"V":5,"W":6,"X":7,"Y":8,"Z":9,
            "0":0,"1":1,"2":2,"3":3,"4":4,"5":5,"6":6,"7":7,"8":8,"9":9
        ]
        
        let weights = [8,7,6,5,4,3,2,10,0,9,8,7,6,5,4,3,2]
        let vinArray = Array(vin)
        
        var sum = 0
        for i in 0..<17 {
            guard let value = map[vinArray[i]] else { return false }
            sum += value * weights[i]
        }
        
        let remainder = sum % 11
        let expectedCheckChar: Character = remainder == 10 ? "X" : Character(String(remainder))
        
        // Check digit is the 9th character (index 8)
        return vinArray[8] == expectedCheckChar
    }
}


/// Production-ready VIN Scanner using Vision Framework.
/// Handles 2-line VINs, OCR noise, and ISO 3779 Checksum validation.
class VINScanner {
    
    // MARK: - API
    
    /// Scans a given image for a valid VIN.
    /// - Parameters:
    ///   - image: The input UIImage (preferably cropped to the VIN area).
    ///   - completion: Returns the result of the scan.
    static func scan(image: UIImage, shouldVerifyChecksum: Bool, completion: @escaping (VINScanResult) -> Void) {
        // Ensure we have a valid CGImage
        guard let cgImage = image.cgImage else {
            DispatchQueue.main.async { completion(.failure) }
            return
        }
        recognizeVIN(from: cgImage, shouldVerifyChecksum: shouldVerifyChecksum, completion: completion)
    }
    
    // MARK: - Vision Logic
    
    private static func recognizeVIN(from image: CGImage, shouldVerifyChecksum: Bool, completion: @escaping (VINScanResult) -> Void) {
        let request = VNRecognizeTextRequest { request, error in
            if let error = error {
                print("VIN OCR Error: \(error)")
                DispatchQueue.main.async { completion(.failure) }
                return
            }
            
            guard let observations = request.results as? [VNRecognizedTextObservation] else {
                DispatchQueue.main.async { completion(.failure) }
                return
            }
            
            let result = self.extractVIN(from: observations, shouldVerifyChecksum: shouldVerifyChecksum)
            DispatchQueue.main.async { completion(result) }
        }
        
        // Configuration for maximum accuracy on codes
        request.recognitionLevel = .accurate
        request.usesLanguageCorrection = false // VINs are not dictionary words
        request.minimumTextHeight = 0.015 // Filters out extremely small noise
        
        let handler = VNImageRequestHandler(cgImage: image, orientation: .up, options: [:])
        
        DispatchQueue.global(qos: .userInitiated).async {
            do {
                try handler.perform([request])
            } catch {
                print("Failed to perform OCR: \(error)")
                DispatchQueue.main.async { completion(.failure) }
            }
        }
    }
    
    // MARK: - Extraction & Merge Logic
    
    private static func extractVIN(from observations: [VNRecognizedTextObservation], shouldVerifyChecksum: Bool) -> VINScanResult {
        var lines: [(String, CGRect)] = []
        
        for obs in observations {
            // Get best candidate
            guard let text = obs.topCandidates(1).first?.string else { continue }
            
            // Clean common OCR noise and normalize
            let cleaned = text.uppercased()
                .replacingOccurrences(of: " ", with: "")
                .replacingOccurrences(of: "-", with: "")
                .replacingOccurrences(of: "_", with: "")
            
            // Heuristic: Partial VINs acceptable for merging, but single noise chars (<6) are useless
            guard cleaned.count >= 6 else { continue }
            
            lines.append((cleaned, obs.boundingBox))
        }
        
        // Sort top-to-bottom (Vision Y=0 is bottom, so Higher Y is top)
        lines.sort { $0.1.midY > $1.1.midY }
        
        // 1. Single-line Perfect Match
        for line in lines {
            let result = VINValidator.shared.validate(vin: line.0, shouldVerifyChecksum: shouldVerifyChecksum)
            if case .failure = result { continue }
            return result
        }
        
        // 2. Try merging adjacent lines (Two-line VINs)
        // Since we sorted Top->Bottom, we try concatenating i + j
        for i in 0..<lines.count {
            for j in (i+1)..<lines.count {
                let merged = lines[i].0 + lines[j].0
                
                // Only check if length is plausible (17 chars)
                if merged.count == 17 {
                    let result = VINValidator.shared.validate(vin: merged, shouldVerifyChecksum: shouldVerifyChecksum)
                    if case .failure = result { continue }
                    return result
                }
            }
        }
        
        return .failure
    }
    
}


struct VINScannerVisionOnlyView: View {
    
    @StateObject private var viewModel: VINScannerVisionOnlyViewModel
    @Environment(\.presentationMode) var presentationMode
    
    init(onDetected: @escaping (String, UIImage) -> Void, onSettingNavigation: @escaping () -> Void) {
        self._viewModel = StateObject(wrappedValue: VINScannerVisionOnlyViewModel(onDetected: onDetected, onSettingNavigation: onSettingNavigation))
    }
    
    var body: some View {
        ZStack {
            OCRCameraPreview(session: viewModel.session)
                .ignoresSafeArea()
            VStack {
                Text("")
                    .alert(isPresented: $viewModel.showPermissionAlert) {
                        Alert(
                            title: Text(AppConstants.AppInfo.appName),
                            message: Text(AppConstants.cameraPermissionString),
                            primaryButton: .default(Text("Settings"), action: {
                                viewModel.openSettings()
                            }),
                            secondaryButton: .cancel(Text("Cancel"), action: {
                                presentationMode.wrappedValue.dismiss()
                            })
                        )
                    }
                Text("")
                    .alert(isPresented: $viewModel.showCameraErrorAlert) {
                        Alert(
                            title: Text(AppConstants.AppInfo.appName),
                            message: Text(AppConstants.cameraNotSupportedMessage),
                            dismissButton: .default(Text("OK"), action: {
                                presentationMode.wrappedValue.dismiss()
                            })
                        )
                    }
            }
            // Dark Overlay with Cutout
            Color.black.opacity(0.6)
                .mask(
                    ZStack {
                        Rectangle()
                            .fill(Color.white)
                        
                        RoundedRectangle(cornerRadius: 10)
                            .frame(width: viewModel.rectWidth, height: viewModel.rectHeight)
                            .blendMode(.destinationOut)
                    }
                    .compositingGroup()
                )
                .ignoresSafeArea()
            
            // Guide Frame (Optional visual border)
            RoundedRectangle(cornerRadius: 10)
                .stroke(viewModel.guideColor, lineWidth: 2)
                .frame(width: viewModel.rectWidth, height: viewModel.rectHeight)
            
            // Text and UI Controls
            VStack {
                
                Spacer()
                
                Text(viewModel.statusText)
                    .foregroundColor(.white)
                    .font(.system(size: 16, weight: .regular))
                    .padding(.bottom, viewModel.rectHeight / 2 + 30) // Push up from center
                
                Spacer()
                
                HStack {
                    Toggle(isOn: $viewModel.shouldVerifyChecksum) {
                        Text("ISO 3779")
                            .font(.system(size: 16, weight: .semibold))
                            .foregroundColor(.white)
                    }
                    .toggleStyle(SwitchToggleStyle(tint: .green))
                    .padding(.vertical, 8)
                    .padding(.horizontal, 16)
                    .background(Color.black.opacity(0.5))
                    .cornerRadius(20)
                    .overlay(
                        RoundedRectangle(cornerRadius: 20)
                            .stroke(Color.white.opacity(0.3), lineWidth: 1)
                    )
                    .fixedSize()
                    .padding(.leading, 24)
                    .padding(.bottom, 36)
                    
                    Spacer()
                    
                    Button(action: {
                        viewModel.toggleFlashlight()
                    }) {
                        HStack(spacing: 8) {
                            Image(systemName: viewModel.isFlashlightOn ? "bolt.fill" : "bolt.slash.fill")
                                .font(.system(size: 16))
                            Text(viewModel.isFlashlightOn ? "On" : "Off")
                                .font(.system(size: 16, weight: .semibold))
                        }
                        .foregroundColor(.white)
                        .padding(.vertical, 8)
                        .padding(.horizontal, 16)
                        .background(Color.black.opacity(0.5))
                        .cornerRadius(20)
                        .overlay(
                            RoundedRectangle(cornerRadius: 20)
                                .stroke(Color.white.opacity(0.3), lineWidth: 1)
                        )
                    }
                    .padding(.trailing, 24)
                    .padding(.bottom, 36)
                }
            }
            
            VStack {
                HStack {
                    Spacer()
                    
                    Button(action: {
                        presentationMode.wrappedValue.dismiss()
                    }) {
                        Image(systemName: "xmark")
                            .font(.system(size: 18, weight: .bold))
                            .foregroundColor(.white)
                            .padding(10)
                            .background(Color.black.opacity(0.5))
                            .clipShape(Circle())
                    }
                    .padding(.top, 60)
                    .padding(.trailing, 20)
                }
                Spacer()
            }
        }
        .onAppear {
            viewModel.onTimeout = {
//                presentationMode.wrappedValue.dismiss()
            }
            viewModel.start()
        }
        .onDisappear {
            viewModel.stop()
        }
    }
}

// MARK: - ViewModel
final class VINScannerVisionOnlyViewModel: NSObject, ObservableObject, AVCaptureVideoDataOutputSampleBufferDelegate {
    
    // MARK: Public
    let session = AVCaptureSession()
    var onDetected: ((String, UIImage) -> Void)?
    var onSettingNavigation: (() -> Void)?
    var onTimeout: (() -> Void)?
    private var currentPixelBuffer: CVPixelBuffer?
    
    // VIN Specific Properties
    var rectWidth: CGFloat { UIScreen.main.bounds.width * 0.85 }
    var rectHeight: CGFloat { 60 } // Thinner box for VIN
    
    @Published var guideColor: Color = .blue
    @Published var statusText: String = "Place the entire VIN inside the box"
    @Published var isFlashlightOn: Bool = false
    @Published var shouldVerifyChecksum: Bool = false
    @Published var showPermissionAlert = false
    @Published var showCameraErrorAlert = false
    
    // MARK: Timing
    private let alignmentDelay: TimeInterval = 2.5
    private var detectionEnabled = false
    
    // MARK: Camera
    private var captureDevice: AVCaptureDevice?
    private let videoOutput = AVCaptureVideoDataOutput()
    private let cameraQueue = DispatchQueue(label: "camera.queue.vin")
    private let sessionQueue = DispatchQueue(label: "session.queue.vin")
    private var timeoutTimer: Timer?
    
    // MARK: Vision
    private var frameCounter = 0
    private let frameStride = 6
    
    init(onDetected: @escaping ((String, UIImage) -> Void), onSettingNavigation: @escaping () -> Void) {
        self.onDetected = onDetected
        self.onSettingNavigation = onSettingNavigation
    }
    
    // MARK: Lifecycle
    func start() {
        checkCameraPermission()
    }
    
    private func checkCameraPermission() {
        switch AVCaptureDevice.authorizationStatus(for: .video) {
        case .authorized:
            self.startSession()
        case .notDetermined:
            AVCaptureDevice.requestAccess(for: .video) { granted in
                if granted {
                    self.startSession()
                } else {
                    DispatchQueue.main.async {
                        self.showPermissionAlert = true
                    }
                }
            }
        case .denied, .restricted:
            DispatchQueue.main.async {
                self.showPermissionAlert = true
            }
        @unknown default:
            DispatchQueue.main.async {
                self.showPermissionAlert = true
            }
        }
    }
    
    func openSettings() {
        if let url = URL(string: UIApplication.openSettingsURLString) {
            onSettingNavigation?()
            UIApplication.shared.open(url)
        }
    }
    
    private func startSession() {
        configureCamera()
        
        sessionQueue.async {
            self.session.startRunning()
        }
        
        startTimeoutTimer()
        
        DispatchQueue.main.asyncAfter(deadline: .now() + alignmentDelay) {
            self.detectionEnabled = true
            self.statusText = "Scanning…"
            self.guideColor = .yellow
        }
    }
    
    func stop() {
        cleanup()
    }
    
    // MARK: - Cleanup
    private func cleanup() {
        detectionEnabled = false
        timeoutTimer?.invalidate()
        
        sessionQueue.async { [weak self] in
            guard let self = self else { return }
            if self.session.isRunning {
                self.session.stopRunning()
            }
            self.session.inputs.forEach { self.session.removeInput($0) }
            self.session.outputs.forEach { self.session.removeOutput($0) }
        }
    }
    
    // MARK: Camera Setup
    private func configureCamera() {
        session.beginConfiguration()
        session.sessionPreset = .high
        
        let discoverySession = AVCaptureDevice.DiscoverySession(
            deviceTypes: [.builtInTripleCamera, .builtInDualCamera, .builtInWideAngleCamera],
            mediaType: .video,
            position: .back
        )
        
        guard let device = discoverySession.devices.first else {
            session.commitConfiguration()
            DispatchQueue.main.async {
                self.showCameraErrorAlert = true
            }
            return
        }
        
        self.captureDevice = device
        
        guard let input = try? AVCaptureDeviceInput(device: device) else {
            session.commitConfiguration()
            DispatchQueue.main.async {
                self.showCameraErrorAlert = true
            }
            return
        }
        
        if session.canAddInput(input) {
            session.addInput(input)
        }
        
        // Optimize for close-range scanning
        do {
            try device.lockForConfiguration()
            if device.isFocusModeSupported(.continuousAutoFocus) {
                device.focusMode = .continuousAutoFocus
            }
            if device.isSmoothAutoFocusEnabled {
                device.isSmoothAutoFocusEnabled = true
            }
            device.unlockForConfiguration()
        } catch {
            print("Error configuring focus mode: \(error)")
        }
        
        videoOutput.videoSettings = [
            kCVPixelBufferPixelFormatTypeKey as String:
                kCVPixelFormatType_420YpCbCr8BiPlanarFullRange
        ]
        
        videoOutput.setSampleBufferDelegate(self, queue: cameraQueue)
        
        if session.canAddOutput(videoOutput) {
            session.addOutput(videoOutput)
        }
        
        session.commitConfiguration()
    }
    

    
    // MARK: SampleBuffer Delegate
    func captureOutput(_ output: AVCaptureOutput,
                       didOutput sampleBuffer: CMSampleBuffer,
                       from connection: AVCaptureConnection) {
        
        guard detectionEnabled else { return }
        
        frameCounter += 1
        if frameCounter % frameStride != 0 { return }
        
        guard let pixelBuffer = CMSampleBufferGetImageBuffer(sampleBuffer) else { return }
        // Store for potential needs, though mostly used for cropping now
        currentPixelBuffer = pixelBuffer
        
        guard let image = cropToGuide(pixelBuffer: pixelBuffer) else { return }
        
        VINScanner.scan(image: image, shouldVerifyChecksum: shouldVerifyChecksum) { [weak self] result in
            guard let self = self else { return }
            
            DispatchQueue.main.async {
                switch result {
                case .success(let vin):
                    self.guideColor = .green
                    self.statusText = "VIN detected: \(vin)"
                    self.cleanup()
                    self.onDetected?(vin, image)
                case .invalidChecksum:
                    self.statusText = "VIN could not be verified"
                    self.guideColor = .red
                    // Do not cleanup or call onDetected, keep scanning
                case .failure:
                    // Keep scanning, maybe reset status if it was invalid before?
                    // For now, let's just leave it or set back to Scanning if needed,
                    // but usually we don't want to flicker "Scanning..." too fast.
                    if self.statusText.contains("Invalid Checksum") {
                         // Optional: Reset after some time or just leave it until a new one is found
                    } else if self.statusText.starts(with: "VIN detected") {
                         // Should not happen if we cleaned up
                    } else {
                        self.statusText = "Scanning…"
                        self.guideColor = .yellow
                    }
                }
            }
        }
    }

    

    // MARK: - Flashlight
    func toggleFlashlight() {
        guard let device = captureDevice, device.hasTorch else { return }
        
        do {
            try device.lockForConfiguration()
            if device.torchMode == .on {
                device.torchMode = .off
                isFlashlightOn = false
            } else {
                try device.setTorchModeOn(level: AVCaptureDevice.maxAvailableTorchLevel)
                isFlashlightOn = true
            }
            device.unlockForConfiguration()
        } catch {
            print("Error toggling flashlight: \(error)")
        }
    }

    private func startTimeoutTimer() {
        timeoutTimer?.invalidate()
        timeoutTimer = Timer.scheduledTimer(withTimeInterval: 60.0, repeats: false) { [weak self] _ in
            DispatchQueue.main.async {
                self?.cleanup()
                self?.onTimeout?()
            }
        }
    }

    // MARK: - Orientation Logic
    private func currentCaptureOrientation() -> UIImage.Orientation {
        switch UIDevice.current.orientation {
        case .portrait: return .right
        case .portraitUpsideDown: return .left
        case .landscapeLeft: return .up
        case .landscapeRight: return .down
        default: return .right
        }
    }

    // MARK: - Image Cropping
    private func cropToGuide(pixelBuffer: CVPixelBuffer) -> UIImage? {
        let ciImage = CIImage(cvPixelBuffer: pixelBuffer)
        let context = CIContext()
        
        guard let cgImage = context.createCGImage(ciImage, from: ciImage.extent) else { return nil }
        
        // Create initial UIImage
        let uiImage = UIImage(cgImage: cgImage, scale: 1.0, orientation: currentCaptureOrientation())
        
        // Calculate crop rect
        let screenSize = UIScreen.main.bounds.size
        let rotatedImage = uiImage.fixOrientation()
        
        let imgW = rotatedImage.size.width
        let imgH = rotatedImage.size.height
        
        // Calculate scale factor for AspectFill
        let scaleX = imgW / screenSize.width
        let scaleY = imgH / screenSize.height
        let imageScale = max(scaleX, scaleY)
        
        // Calculate the visible frame of the image
        let visibleWidth = screenSize.width * imageScale
        let visibleHeight = screenSize.height * imageScale
        
        let visibleX = (imgW - visibleWidth) / 2
        let visibleY = (imgH - visibleHeight) / 2
        
        // Define the guide rect in screen coordinates
        let guideWidth = rectWidth
        let guideHeight = rectHeight
        
        let guideFrame = CGRect(
            x: (screenSize.width - guideWidth) / 2,
            y: (screenSize.height - guideHeight) / 2,
            width: guideWidth,
            height: guideHeight
        )
        
        // Map guide frame to image coordinates
        let cropRect = CGRect(
            x: visibleX + (guideFrame.origin.x * imageScale),
            y: visibleY + (guideFrame.origin.y * imageScale),
            width: guideFrame.width * imageScale,
            height: guideFrame.height * imageScale
        )
        
        guard let croppedCG = rotatedImage.cgImage?.cropping(to: cropRect) else { return nil }
        return UIImage(cgImage: croppedCG)
    }
}
