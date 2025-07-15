import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.ksp)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.androidApplication)
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
        outputModuleName = "composeApp"
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

    applyDefaultHierarchyTemplate()

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
        val iosMain by getting {
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
        val iosTest by getting {
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
    // to *find* the KSP-generated code generated by `kspCommonMainKotlinMetadata`.
    sourceSets.named("commonMain").configure {
        kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
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
    // For Koin, you generally want the KSP processor to run for each platform that needs to use the
    // generated Koin modules. However, the universal "ksp()" configuration has performance issues
    // and is deprecated on multiplatform since 1.0.1.

    // This makes KSP process commonMain for metadata, useful if Koin generates common code/modules.
    add("kspCommonMainMetadata", libs.koin.ksp.compiler)

    // Add KSP for each platform if Koin generates platform-specific initializers
    // or if the commonMainMetadata isn't sufficient for platform consumption.
    // The configuration name is usually ksp<TargetName> or ksp<CapitalizedSourceSetName>
    add("kspAndroid", libs.koin.ksp.compiler)
    add("kspDesktop", libs.koin.ksp.compiler)
    add("kspJs", libs.koin.ksp.compiler)
    add("kspWasmJs", libs.koin.ksp.compiler)

    // For Apple targets (iOS, macOS, etc.), KSP configurations can be per-target
    add("kspIosX64", libs.koin.ksp.compiler)
    add("kspIosArm64", libs.koin.ksp.compiler)
    add("kspIosSimulatorArm64", libs.koin.ksp.compiler)
}

// Configure KSP tasks to depend on kspCommonMainKotlinMetadata. This is not done implicitly.
// The specific KSP task names for platforms would be:
//     val platformCompilationKspTaskNames = listOf(
//         "kspDebugKotlinAndroid",
//         "kspReleaseKotlinAndroid",
//         "kspKotlinDesktop",
//         "kspKotlinIosX64",
//         "kspKotlinIosArm64",
//         "kspKotlinIosSimulatorArm64",
//         "kspKotlinJs",
//         "kspKotlinWasmJs"
//     )
// For less maintenance when adding platforms, we examine if the task name matches a pattern.
// This is done by name matching because accurately identifying these tasks by a common supertype
// (e.g., with tasks.withType<SomeKspTask>()) proved unreliable due to Gradle's task decoration and
// the specific class hierarchy of KSP tasks.
tasks.matching { it.name.startsWith("ksp") }.configureEach {
    // Now we know the name starts with "ksp".
    // Log for clarity, showing the actual type.
    project.logger.debug("Found potential KSP task (by name): ${this.name} of type ${this.javaClass.name}")

    // 1. Exclude tasks that should NOT have this 'dependsOn' relationship.
    //    'kspCommonMainKotlinMetadata' should not depend on itself.
    //    '*ProcessorClasspath' tasks are for setting up class paths, not direct code generation consumed by other KSP tasks.
    if (this.name == "kspCommonMainKotlinMetadata" || this.name.contains("ProcessorClasspath", ignoreCase = true)) {
        project.logger.debug("Skipping task '${this.name}' (self or classpath task).")
        return@configureEach // Exit this configuration block for the current task.
    }

    // 2. Define the patterns for the platform KSP tasks we want to target.
    // Regex for typical Kotlin platform KSP tasks (JVM, Android, Native)
    // e.g., kspKotlinDesktop, kspDebugKotlinAndroid, kspReleaseKotlinIosX64
    val isKotlinPlatformKspTask =
        this.name.matches(Regex("ksp([A-Z][a-zA-Z0-9]*)?Kotlin([A-Z][a-zA-Z0-9_]+)"))
    // Regex for JS-based platform KSP tasks (JS, WasmJs)
    // e.g., kspJs, kspSomeVariantJs, kspWasmJs, kspSomeVariantWasmJs
    val isJsBasedPlatformKspTask = this.name.matches(Regex("ksp([A-Z][a-zA-Z0-9]*)?(Wasm)?Js"))

    // 3. If the current KSP task matches one of our platform patterns, add the dependency.
    if (isKotlinPlatformKspTask || isJsBasedPlatformKspTask) {
        project.logger.info("Configuring task '${this.name}' (name match) to depend on 'kspCommonMainKotlinMetadata'")
        this.dependsOn("kspCommonMainKotlinMetadata")
    } else {
        // This task started with "ksp" but didn't match the more specific platform patterns.
        // This might include tasks like 'kspAARMetadataExtractor', etc., which are fine to ignore here.
        project.logger.debug("Task '${this.name}' (name starts with ksp) did not match specific platform patterns for dependsOn.")
    }
}

// Task kspCommonMainKotlinMetadata is not automatically triggered, it needs a manual instruction to do so
tasks.withType<KotlinCompile>().all {
    if (name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    }
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

// For debugging: `.gradlew printKotlinDetails` shows the details of the configuration defined here.
tasks.register("printKotlinDetails") {
    doLast {
        println(">>> Kotlin Source Sets <<<")
        kotlin.sourceSets.forEach {
            println("Name: ${it.name}")
            it.dependsOn.forEach { dep ->
                println("  DependsOn: ${dep.name}")
            }
        }
        println("\n>>> Kotlin Compilations <<<")
        kotlin.targets.forEach { target ->
            target.compilations.forEach { compilation ->
                println("Compilation: ${compilation.name} on Target: ${target.name}")
                println("  Task Name: ${compilation.compileTaskProvider.name}")
                println("  Default Source Set: ${compilation.defaultSourceSet.name}")
                compilation.allKotlinSourceSets.forEach { ss ->
                    println("    AllKotlinSourceSet: ${ss.name}")
                }
            }
        }
        println("\n>>> KSP Related Configurations (if any exist explicitly) <<<")
        project.configurations.filter { it.name.startsWith("ksp") }.forEach {
            println("Configuration: ${it.name}")
            // if (it.isCanBeResolved) {
            //     it.resolvedConfiguration.lenientConfiguration.allModuleDependencies.forEach { dep ->
            //         println("  Dependency: ${dep.moduleGroup}:${dep.moduleName}:${dep.moduleVersion}")
            //     }
            // }
        }
    }
}
