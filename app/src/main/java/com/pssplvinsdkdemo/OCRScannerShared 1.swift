//
//  OCRScannerShared.swift
//  Truline
//
//  Created by PinalKumar on 22/12/25.
//

import SwiftUI
import AVFoundation

enum OCRScanType {
    case licensePlate
    case vin
    
    var apiKey: String {
        switch self {
        case .licensePlate:
            "license_plate"
        case .vin:
            "vin"
        }
    }
}

// MARK: - Camera Preview
struct OCRCameraPreview: UIViewRepresentable {
    
    let session: AVCaptureSession
    
    func makeUIView(context: Context) -> UIView {
        let view = UIView()
        let layer = AVCaptureVideoPreviewLayer(session: session)
        layer.videoGravity = .resizeAspectFill
        layer.frame = UIScreen.main.bounds
        view.layer.addSublayer(layer)
        return view
    }
    
    func updateUIView(_ uiView: UIView, context: Context) {}
}
