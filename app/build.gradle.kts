import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { stream ->
        localProperties.load(stream)
    }
}

val rawServerUrl: String = localProperties.getProperty("nux.server.url") ?: ""

android {
    namespace = "com.israadev.nuxlauncher"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.israadev.nuxlauncher"
        minSdk = 26
        targetSdk = 34
        versionCode = 10
        versionName = "1.0.9"

        buildConfigField("String", "SERVER_BASE_URL", "\"$rawServerUrl\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        ndk {
            abiFilters.addAll(listOf("arm64-v8a", "armeabi-v7a", "x86", "x86_64"))
        }
    }

    val releaseSigningConfigured =
        !System.getenv("ANDROID_KEYSTORE_PATH").isNullOrBlank() &&
            !System.getenv("ANDROID_KEYSTORE_PASSWORD").isNullOrBlank() &&
            !System.getenv("ANDROID_KEY_ALIAS").isNullOrBlank() &&
            !System.getenv("ANDROID_KEY_PASSWORD").isNullOrBlank()

    val releaseTaskRequested = gradle.startParameter.taskNames.any {
        it.contains("Release", ignoreCase = true)
    }

    if (!releaseSigningConfigured && releaseTaskRequested) {
        throw GradleException(
            "Release signing is not configured. Set ANDROID_KEYSTORE_PATH, " +
                "ANDROID_KEYSTORE_PASSWORD, ANDROID_KEY_ALIAS and ANDROID_KEY_PASSWORD."
        )
    }

    signingConfigs {
        if (releaseSigningConfigured) {
            create("release") {
                storeFile = file(System.getenv("ANDROID_KEYSTORE_PATH"))
                storePassword = System.getenv("ANDROID_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("ANDROID_KEY_ALIAS")
                keyPassword = System.getenv("ANDROID_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            if (releaseSigningConfigured) {
                signingConfig = signingConfigs.getByName("release")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
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

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        jniLibs {
            useLegacyPackaging = true
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.gson)
    implementation(libs.okhttp)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.commons.compress)
    implementation(libs.xz)
    implementation(libs.bytehook)
    implementation("androidx.webkit:webkit:1.12.1")

    debugImplementation(libs.androidx.ui.tooling)
}
