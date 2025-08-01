# Event-prototype

This is a demo of a Compose Multiplatform app. The project is created by the [Kotlin Multiplatform Wizard](https://kmp.jetbrains.com/) and targeting Android, iOS, Web and Desktop (Windows, MacOS, Linux).

## Installing the released versions
* Android (app): Download the .apk-file on your device, allow install and trust the developer.
* MacOS (desktop): Download the .dmg-file on your device and drop it into the application folder.
* Linux (desktop): Download the .deb-file on your device and install it through your app manager or running `sudo dpkg -i PACKAGE.NAME_VERSION.deb` in a terminal. For uninstall, run `sudo dpkg -r PACKAGE.NAME`.
* JS or WasmJs (web): Download the .wasmJs.zip-file or the js.zip-file on your device and extract it. Make sure you have Python on your system, otherwise install it from [python.org](https://www.python.org/downloads/). In a terminal, change to the folder where you extracted the zip-file and run `python3 -m http.server 8080`. Open a browser and navigate to `http://localhost:8080`.

## Project structure
* `/composeApp` is for code that will be shared across the Compose Multiplatform applications.
  It contains several subfolders:
  - `commonMain` is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name:
    + `androidMain` is the Android app.
    + `desktopMain` is the desktop app. Compile on a different platform to obtain that platform's executable.
    + `iosMain` is the Kotlin part of the iOS app.
    + `jsMain` is the JavaScript app that runs directly in a web browser.
    + `wasmJsMain` targets WebAssembly, which runs in a sandboxed environment within the browser.
* `/iosApp` contains iOS applications. This is the entry point for the iOS app. This is also where SwiftUI code for the project lives. Only compiles on MacOS.

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html),
[Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform/#compose-multiplatform),
[Kotlin/Wasm](https://kotl.in/wasm/)…

Feedback on Compose/Web and Kotlin/Wasm is appreciated in the public Slack channel [#compose-web](https://slack-chats.kotlinlang.org/c/compose-web).
If you face any issues, please report them on [GitHub](https://github.com/mheerwaarden/CMP-Event-prototype/issues).

## Starting the app in Android Studio
To run the Android App, it is recommended to use the Android Studio run configuration. 
Using the command line, you need to do the following steps:
- To build: `./gradlew :composeApp:assembleDebug`
- To start the emulator:
  + To find the emulator name: `~/Android/Sdk/emulator/emulator -list-avds`
  + To start the emulator: `~/Android/Sdk/emulator/emulator -avd <emulator name>`
- To install:
  + `./gradlew :composeApp:installDebug`, which includes running the build `./gradlew :composeApp:assembleDebug`
  + Alternatively, to install using adb: `adb install composeApp/build/outputs/apk/debug/composeApp-debug.apk`
- Start the app on the emulator:
  + Open the app drawer on the emulator.
  + Find your app "Event Demo".
  + Click the app icon to launch it.

You can start the application on other platforms by running the following Gradle tasks:
* Desktop app: `./gradlew :composeApp:run`
* iOS app: `./gradlew :composeApp:iosX64Run`
* JS app: `./gradlew :composeApp:jsBrowserDevelopmentRun`
* WasmJS app: `./gradlew :composeApp:wasmJsBrowserDevelopmentRun`
* Android app: `./gradlew :composeApp:assembleDebugAndroid*/` or menu item 'Run Build | Generate App Bundles or APKs | Generate APKs'
  APK in composeApp/build/outputs/apk/debug/composeApp-debug.apk

You can start the tests by running the following Gradle tasks:
* Android app: `./gradlew :composeApp:androidUnitTest`
* Desktop app: `./gradlew :composeApp:jvmTest`
* iOS app: `./gradlew :composeApp:iosTest`
* JS app: `./gradlew :composeApp:jsBrowserTest` (or `./gradlew :composeApp:jsNodeTest` to run the NodeJs test or `./gradlew :composeApp:jsTest` to run both)
* WasmJS app: `./gradlew :composeApp:wasmJsBrowserTest`

**Note:** Gradle tasks can be added to the Android Studio run configuration as a configuration of type Gradle with the Run argument set to the Gradle task name. 


