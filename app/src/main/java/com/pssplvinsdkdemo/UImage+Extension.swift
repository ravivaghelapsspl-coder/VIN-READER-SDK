//
//  UImage+Extension.swift
//  Truline
//
//  Created by PinalKumar on 13/03/25.
//

import UIKit

//extension UIImage {
//    func fixOrientation() -> UIImage {
//        guard imageOrientation != .up else { return self }
//        
//        UIGraphicsBeginImageContextWithOptions(size, false, scale)
//        self.draw(in: CGRect(origin: .zero, size: size))
//        let correctedImage = UIGraphicsGetImageFromCurrentImageContext()
//        UIGraphicsEndImageContext()
//        
//        return correctedImage ?? self
//    }
//}

extension UIImage {
    func fixOrientation() -> UIImage {
        // If already correct orientation, return self
        guard imageOrientation != .up else { return self }
        
        let format = UIGraphicsImageRendererFormat()
        format.scale = self.scale
        format.opaque = true
        
        let renderer = UIGraphicsImageRenderer(size: self.size, format: format)
        
        // Capture self weakly to avoid retention
        return autoreleasepool { [weak self] in
            guard let self = self else {
                // Return empty image if self is nil (rare case)
                return UIImage()
            }
            
            return renderer.image { _ in
                self.draw(in: CGRect(origin: .zero, size: self.size))
            }
        }
    }
}
