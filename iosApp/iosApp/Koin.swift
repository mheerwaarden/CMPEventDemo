//
//  KoinApplication.swift
//  KaMPStarteriOS
//
//  Created by Russell Wolf on 6/18/20.
//  Copyright © 2020 Touchlab. All rights reserved.
//

import Foundation
import ComposeApp

func startKoin() {
    // You could just as easily define all these dependencies in Kotlin,
    // but this helps demonstrate how you might pass platform-specific
    // dependencies in a larger scale project where declaring them in
    // Kotlin is more difficult, or where they're also used in
    // iOS-specific code.

    let userDefaults = UserDefaults(suiteName: "EVENT_DEMO_SETTINGS")!
    let iosAppInfo = IosAppInfo()
    let doOnStartup = { NSLog("Startup iOS/Swift") }

    let koinApplication = KoinIOSKt.doInitKoinIos(
        userDefaults: userDefaults,
        appInfo: iosAppInfo,
        doOnStartup: doOnStartup
    )
    _koin = koinApplication.koin
}

private var _koin: Koin_coreKoin?
var koin: Koin_coreKoin {
    return _koin!
}

class IosAppInfo: AppInfo {
    var versionCode: String
    var versionName: String
    let appId: String

    init() {
        // Read Info.plist
        guard let infoDictionary = Bundle.main.infoDictionary else {
            fatalError("Info.plist not found or invalid.")
        }

        // Get Bundle Identifier
        guard let bundleIdentifier = Bundle.main.bundleIdentifier else {
            fatalError("Bundle identifier not found in Info.plist.")
        }

        // Get Version Name
        guard let version = infoDictionary["CFBundleShortVersionString"] as? String else {
            fatalError("Version name (CFBundleShortVersionString) not found or invalid in Info.plist.")
        }

        // Get Version Code (Build Number)
        guard let buildNumber = infoDictionary["CFBundleVersion"] as? String else {
            fatalError("Version code (CFBundleVersion) not found or invalid in Info.plist.")
        }

        self.appId = bundleIdentifier
        self.versionName = version
        self.versionCode = buildNumber
    }
}
