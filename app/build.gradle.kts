import java.util.Properties
import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.dokka") version "1.9.10"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22"
}

tasks.named<DokkaTask>("dokkaHtml").configure {
    outputDirectory.set(file("docs"))
    moduleName.set("PillBox")

    dokkaSourceSets.named("main") {
        perPackageOption {
            matchingRegex.set(".*libs.*")
            suppress.set(true)
        }
        perPackageOption {
            matchingRegex.set("com\\.daniela\\.pillbox\\.ui\\.theme")
            suppress.set(true)
        }
        perPackageOption {
            matchingRegex.set("com\\.daniela\\.pillbox\\.activity")
            suppress.set(true)
        }
    }
}

// Top of your app/build.gradle.kts
val secrets = Properties().apply {
    try {
        load(rootProject.file("secrets.properties").inputStream())
    } catch (e: Exception) {
        logger.warn("secrets.properties not found! Using empty values")
    }
}

android {
    namespace = "com.daniela.pillbox"
    compileSdk = 35

    defaultConfig {
        // Required for BuildConfig access
        buildConfigField(
            "String",
            "ENDPOINT",
            "\"${secrets.getProperty("APPWRITE_ENDPOINT", "")}\""
        )
        buildConfigField(
            "String",
            "PROJECT_ID",
            "\"${secrets.getProperty("APPWRITE_PROJECT_ID", "")}\""
        )
        buildConfigField(
            "String",
            "DATABASE_ID",
            "\"${secrets.getProperty("APPWRITE_DATABASE_ID", "")}\""
        )
        buildConfigField(
            "String",
            "MEDICATIONS_ID",
            "\"${secrets.getProperty("APPWRITE_MEDICATIONS_ID", "")}\""
        )
        buildConfigField(
            "String",
            "SCHEDULES_ID",
            "\"${secrets.getProperty("APPWRITE_SCHEDULES_ID", "")}\""
        )
        buildConfigField(
            "String",
            "INTAKES_ID",
            "\"${secrets.getProperty("APPWRITE_INTAKES_ID", "")}\""
        )

        applicationId = "com.daniela.pillbox"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // AppWrite
    implementation(libs.appwrite.sdk)

    // Voyager
    implementation(libs.voyager.navigator)
    implementation(libs.voyager.screenModel)
    implementation(libs.voyager.transitions)
    implementation(libs.voyager.koin)

    // Google things
    implementation(libs.androidx.ui.text.google.fonts)
    implementation(libs.androidx.material.icons.extended)

    // Splash screen
    implementation(libs.androidx.core.splashscreen)

    // Koin
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.core)

    // Ktor
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // Material3
    implementation(libs.androidx.material3)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}