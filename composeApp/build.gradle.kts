import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.ksp)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.skie)
}

ksp {
    arg("KOIN_CONFIG_CHECK", "true")
}

kotlin {
    jvmToolchain(17)

    androidTarget {
        compilerOptions {
            freeCompilerArgs.addAll(
                listOf(
                    // Generate metadata classes for enabling certain recomposition optimizations
                    // in Compose.
                    "-P",
                    "plugin:org.jetbrains.compose.compiler:generateFunctionKeyMetaClass=true",
                )
            )
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
        iosTarget.compilations.all {
            compileTaskProvider.configure {
                compilerOptions.freeCompilerArgs.addAll(
                    listOf(
                        "-P",
                        "plugin:org.jetbrains.compose.compiler:generateFunctionKeyMetaClass=true",
                    )
                )
            }
        }
    }

    jvm("desktop") {
        compilerOptions {
            freeCompilerArgs.addAll(
                listOf(
                    // Generate metadata classes for enabling certain recomposition optimizations
                    // in Compose.
                    "-P",
                    "plugin:org.jetbrains.compose.compiler:generateFunctionKeyMetaClass=true",
                )
            )
        }
    }

    js(IR) { // Regular JS target (for Safari)
        // Add support for the ES2015 features
        useEsModules()
        browser {
            commonWebpackConfig {
                outputFileName = "composeApp.js" // Replace with your desired output file name
                output?.library = "composeApp" // Replace with your desired library name
            }
            webpackTask {
                // We don't need to do anything special here.
                // The default webpack task is sufficient.
            }
        }
        binaries.executable()
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    freeCompilerArgs.addAll(
                        listOf(
                            "-Xir-per-module",
                            "-P",
                            "plugin:org.jetbrains.compose.compiler:generateFunctionKeyMetaClass=true",
                            "-opt-in=kotlin.ExperimentalStdlibApi"
                        )
                    )
                }
            }
        }
        // Define nodejs to get tests working
        nodejs {
            testTask {
                // No special configuration needed here.
            }
        }
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs { // WebAssembly target
        moduleName = "composeApp" // TODO: replace with moduleOutputName
        browser {
            val rootDirPath = project.rootDir.path
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                outputFileName = "composeApp.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(rootDirPath)
                        add(projectDirPath)
                    }
                }
            }
        }
        binaries.executable()
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions.freeCompilerArgs.addAll(
                    listOf(
                        "-Xir-per-module",
                        "-P",
                        "plugin:org.jetbrains.compose.compiler:generateFunctionKeyMetaClass=true",
                        "-Xwasm-attach-js-exception",
                    )
                )
            }

        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                implementation(libs.androidx.lifecycle.viewmodel)
                implementation(libs.androidx.lifecycle.runtime.compose)

                // Extended icons set
                implementation(compose.materialIconsExtended)

                // Dependency injection
                implementation(libs.koin.core)
                api(libs.koin.annotations)
                implementation(libs.koin.compose)
                implementation(libs.koin.compose.viewmodel)
                implementation(libs.koin.compose.viewmodel.navigation)
                // Coroutines
                implementation(libs.kotlinx.coroutines.core)
                // Navigation
                implementation(libs.navigation.compose)
                // Preferences
                implementation(libs.multiplatform.settings)
                // DateTime
                implementation(libs.kotlinx.dateTime)
                // Logging
                api(libs.touchlab.kermit)
            }
        }
        val commonTest by getting {
            dependencies {
                // Bring all the platform dependencies automatically
                implementation(kotlin("test"))
                implementation(libs.bundles.shared.commonTest)
                implementation(libs.touchlab.kermit)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(compose.preview)
                implementation(libs.androidx.activity.compose)

                // Dependency injection
                implementation(libs.koin.android)
                // Preferences
                implementation(libs.androidx.datastore.preferences)
                implementation(libs.androidx.preference.ktx)
                // ViewModel
                implementation(libs.androidx.lifecycle.viewmodel)
                // Window size calculation
                implementation(libs.androidx.material3.window.size.clazz)
            }
        }
        val androidUnitTest by getting {
            dependencies {
                implementation(libs.bundles.shared.commonTest)
                implementation(libs.bundles.shared.androidTest)

                // Robolectric - a simulated Android environment for unit testing
                implementation(libs.robolectric)

                // Preferences
                implementation(libs.androidx.datastore.preferences)
                implementation(libs.androidx.preference.ktx)
                // ViewModel
                implementation(libs.androidx.lifecycle.viewmodel)
                // Window size calculation
                implementation(libs.androidx.material3.window.size.clazz)
            }
        }
        val androidInstrumentedTest by getting {
            dependencies {
                implementation(libs.bundles.shared.commonTest)
                implementation(libs.bundles.shared.androidTest)

                // Actual Android execution
                implementation(libs.androidx.espresso.core)
                implementation(libs.androidx.rules)
                implementation(libs.androidx.runner)

                // Preferences
                implementation(libs.androidx.datastore.preferences)
                implementation(libs.androidx.preference.ktx)
                // ViewModel
                implementation(libs.androidx.lifecycle.viewmodel)
                // Window size calculation
                implementation(libs.androidx.material3.window.size.clazz)
            }
        }
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)

            dependencies {
                // Logging
                api(libs.touchlab.kermit.simple)
                // Conversion of enum, sealed classes and coroutines to native iOS
                // Skie only works for iOS targets and may error for other platforms, so define
                // where it does work
                implementation(libs.touchlab.skie.annotations)
            }
        }
        val iosX64Test by getting
        val iosArm64Test by getting
        val iosSimulatorArm64Test by getting
        val iosTest by creating {
            dependsOn(commonTest)
            iosX64Test.dependsOn(this)
            iosArm64Test.dependsOn(this)
            iosSimulatorArm64Test.dependsOn(this)

            dependencies {
                // Logging
                api(libs.touchlab.kermit.simple)
                // Conversion of enum, sealed classes and coroutines to native iOS
                implementation(libs.touchlab.skie.annotations)
            }
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinx.coroutines.swing)
            }
        }
        val desktopTest by getting

        val wasmJsMain by getting {
            dependencies {
                implementation(libs.kotlinx.browser)
            }
        }

        val wasmJsTest by getting

        val jsMain by getting {
            dependencies {
                implementation(compose.html.core)
                implementation(libs.kotlin.browser)
            }
        }

        val jsTest by getting
    }

    applyDefaultHierarchyTemplate()

    @OptIn(ExperimentalWasmDsl::class)
    configure(listOf(js(), wasmJs(), jvm("desktop"))) {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions.freeCompilerArgs.addAll(
                    listOf(
                        "-P",
                        "plugin:org.jetbrains.compose.compiler:generateFunctionKeyMetaClass=true",
                    )
                )
            }
        }
    }

    // KSP Common sourceSet, see https://insert-koin.io/docs/setup/annotations
    // It tells the `commonMain` source set (and by extension, dependent platform source sets) where
    // to *find* the code generated by `kspCommonMainKotlinMetadata`.
    sourceSets.named("commonMain").configure {
        kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
    }

    // Configure KSP task dependencies on kspCommonMainKotlinMetadata
    afterEvaluate {
        project.logger.info("KSP: Configuring task dependencies after evaluation.")
        try {
            val kspCommonMainMetadataTaskProvider = tasks.named("kspCommonMainKotlinMetadata")

            // From `./gradlew :composeApp:tasks --all | grep ksp`
            // The list matches the ksp task list in the dependencies section, with explicit
            // entries for Release and Debug for the Android platform.
            val platformKspTaskNames = listOf(
                "kspDebugKotlinAndroid",
                "kspReleaseKotlinAndroid",
                "kspKotlinDesktop",
                "kspKotlinIosArm64",
                "kspKotlinIosSimulatorArm64",
                "kspKotlinIosX64",
                "kspKotlinJs",
                "kspKotlinWasmJs"
            )

            platformKspTaskNames.forEach { kspTaskName ->
                try {
                    tasks.named(kspTaskName) {
                        dependsOn(kspCommonMainMetadataTaskProvider)
                        project.logger.info("KSP: Added dependency from '$kspTaskName' to 'kspCommonMainKotlinMetadata'")
                    }
                } catch (e: UnknownTaskException) {
                    project.logger.warn("KSP: Task '$kspTaskName' not found during dependency setup. This might be okay if the target/variant is not active or KSP is not configured for it.")
                }
            }
        } catch (e: UnknownTaskException) {
            project.logger.error(
                "KSP: Critical - Task 'kspCommonMainKotlinMetadata' not found. Ensure KSP is configured for commonMainMetadata.",
                e
            )
        }
    }
}


// Accessing properties from gradle.properties
val appId: String by project
val versionName: String by project
val versionCode: String by project

android {
    namespace = "com.github.mheerwaarden.eventdemo"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = appId
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = versionCode
        versionName = versionName
    }
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        release {
            // Enable ProGuard/R8:
            isMinifyEnabled = true

            // ProGuard rules:
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), // Default Android rules
                "proguard-rules.pro" // Project rules
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

}

dependencies {
    testImplementation(libs.junit)
    debugImplementation(compose.uiTooling)
    // KSP Configuration
    // The universal "ksp()" configuration has performance issues and is deprecated on multiplatform since 1.0.1
    add("kspCommonMainMetadata", libs.koin.ksp.compiler)
    add("kspAndroid", libs.koin.ksp.compiler)
    add("kspDesktop", libs.koin.ksp.compiler)
    add("kspJs", libs.koin.ksp.compiler)
    add("kspIosX64", libs.koin.ksp.compiler)
    add("kspIosArm64", libs.koin.ksp.compiler)
    add("kspIosSimulatorArm64", libs.koin.ksp.compiler)
    add("kspWasmJs", libs.koin.ksp.compiler)
}

compose.desktop {
    application {
        mainClass = "com.github.mheerwaarden.eventdemo.MainKt"

        buildTypes.release.proguard {
            version.set("7.5.0")
            configurationFiles.from("proguard-rules.pro")
        }

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.github.mheerwaarden.eventdemo"
            packageVersion = "1.0.0"
            description = "Compose Multiplatform Demo with an Event Calendar"
            linux {
                iconFile.set(project.file("src/desktopMain/resources/desktopicon.png"))
            }
            macOS {
                iconFile.set(project.file("src/desktopMain/resources/desktopicon.icns"))
            }
            windows {
                iconFile.set(project.file("src/desktopMain/resources/desktopicon.ico"))
            }

        }
    }
}

compose.resources {
    publicResClass = false
    // Use a more friendly import name than cmpeventdemo.composeapp.generated.resources
    packageOfResClass = "com.github.mheerwaarden.eventdemo.resources"
    generateResClass = auto
}
