import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

val localProps = Properties().apply {
    val propFile = rootProject.file("local.properties")
    if (propFile.exists()) {
        FileInputStream(propFile).use { load(it) }
    }
}

fun resolveSecret(key: String): String? {
    return System.getenv(key)?.takeIf { it.isNotBlank() }
        ?: localProps.getProperty(key)?.takeIf { it.isNotBlank() }
}

android {
    namespace = "eth.culturalsports.shah"
    compileSdk = 35

    defaultConfig {
        applicationId = "eth.culturalsports.shah"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        vectorDrawables {
            useSupportLibrary = true
        }

        // Optional future AdMob App ID support (empty by default; no Ad Unit IDs attached)
        val admobAppId = resolveSecret("ADMOB_APP_ID") ?: ""
        manifestPlaceholders["admobAppId"] = admobAppId
        buildConfigField("String", "ADMOB_APP_ID", "\"$admobAppId\"")
    }

    signingConfigs {
        create("release") {
            val ksPath = resolveSecret("KEYSTORE_FILE") ?: "release.keystore"
            val ksFile = rootProject.file(ksPath)
            val ksAlias = resolveSecret("KEY_ALIAS")
            val ksPass = resolveSecret("KEY_PASSWORD")
            val stPass = resolveSecret("STORE_PASSWORD")

            if (ksFile.exists() && !ksAlias.isNullOrBlank() && !ksPass.isNullOrBlank() && !stPass.isNullOrBlank()) {
                storeFile = ksFile
                keyAlias = ksAlias
                keyPassword = ksPass
                storePassword = stPass
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            val releaseSigning = signingConfigs.getByName("release")
            if (releaseSigning.storeFile?.exists() == true) {
                signingConfig = releaseSigning
            }
        }
        debug {
            isMinifyEnabled = false
        }
    }

    bundle {
        language {
            // Keep all 9 languages inside the signed .aab so in-app instant language switching always works offline!
            enableSplit = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    lint { checkReleaseBuilds = false; abortOnError = false }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
}
