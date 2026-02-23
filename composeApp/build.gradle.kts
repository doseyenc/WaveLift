import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    
    jvm()
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
        }
    }
}

android {
    namespace = "com.doseyenc.wavelift"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.doseyenc.wavelift"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(libs.compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "com.doseyenc.wavelift.MainKt"

        jvmArgs += listOf(
            "-Xmx512m"
        )

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)

            packageName = "WaveLift"
            packageVersion = "1.0.0"
            description = "YouTube to MP3 converter with modern UI"
            vendor = "doseyenc"
            copyright = "© 2026 doseyenc"

            // Bundle yt-dlp and ffmpeg binaries
            appResourcesRootDir = project.layout.projectDirectory.dir("appResources")

            macOS {
                bundleID = "com.doseyenc.wavelift"
                dmgPackageVersion = "1.0.0"
                dmgPackageBuildVersion = "1"

                // Signing — activate via gradle.properties:
                //   compose.desktop.mac.sign=true
                //   compose.desktop.mac.signing.identity=Developer ID Application: ...
                signing {
                    sign.set(
                        project.findProperty("compose.desktop.mac.sign")?.toString() == "true"
                    )
                    identity.set(
                        project.findProperty("compose.desktop.mac.signing.identity")?.toString()
                    )
                }

                // Notarization — activate via gradle.properties:
                //   compose.desktop.mac.notarization.appleID=your@email.com
                //   compose.desktop.mac.notarization.password=app-specific-password
                //   compose.desktop.mac.notarization.teamID=XXXXXXXXXX
                notarization {
                    appleID.set(
                        project.findProperty("compose.desktop.mac.notarization.appleID")?.toString()
                    )
                    password.set(
                        project.findProperty("compose.desktop.mac.notarization.password")?.toString()
                    )
                    teamID.set(
                        project.findProperty("compose.desktop.mac.notarization.teamID")?.toString()
                    )
                }
            }

            windows {
                menuGroup = "WaveLift"
                upgradeUuid = "e8c4a1b7-2d3f-4e5a-b6c7-d8e9f0a1b2c3"
            }

            linux {
                debMaintainer = "doseyenc"
            }
        }
    }
}
