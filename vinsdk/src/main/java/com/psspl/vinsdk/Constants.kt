package com.psspl.vinsdk

object Constants {
    const val LOG_TAG_SCANNER = "ScannerScreen"
    const val REGEX_VIN_STRUCTURE = "^[A-HJ-NPR-Z0-9]{17}$"
    
    // Prefixes
    const val PREFIX_VIN = "VIN"
    const val PREFIX_V1N = "V1N"
    const val PREFIX_CHASSIS_N0 = "CHASSISN0"
    const val PREFIX_CHASSIS = "CHASSIS"
    
    // Replacement Chars
    const val CHAR_O = "O"
    const val CHAR_NUM_0 = "0"
    const val CHAR_I = "I"
    const val CHAR_NUM_1 = "1"
    const val CHAR_Q = "Q"
    const val CHAR_STAR = "*"
    const val CHAR_SPACE = " "
    const val CHAR_HYPHEN = "-"
    const val CHAR_UNDERSCORE = "_"
    const val CHAR_COLON = ":"
    
    // UI Messages
    const val MSG_CAMERA_PERMISSION_REQUIRED = "Camera permission is required to use the scanner"
    
    // Log Messages
    const val LOG_BINDING_FAILED = "Use case binding failed"
    const val LOG_TEXT_RECOGNITION_FAILED = "Text recognition failed"
    const val LOG_BLOCK_INSIDE = "Block INSIDE scanner list:"
    const val LOG_BLOCK_OUTSIDE = "Block OUTSIDE or intersecting weakly:"
    const val LOG_BLOCK_FOUND = "Found Block anywhere:"
    const val LOG_VALID_VIN = "✅ Valid matched VIN:"
}
