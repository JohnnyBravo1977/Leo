plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // Kotlin 2.x → required for Compose
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}

// Load version info dynamically from VERSION file
val versionProps = file("../gradle.properties")
    .readLines()
    .mapNotNull { line ->
        val parts = line.split("=")
        if (parts.size == 2) parts[0].trim() to parts[1].trim() else null
    }
    .toMap()

android {
    namespace = "com.example.leo"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.leo"
        minSdk = 26
        targetSdk = 34
        versionName = versionProps["VERSION_NAME"]
        versionCode = versionProps["VERSION_CODE"]?.toInt() ?: 1
    }
}

android {
    namespace = "com.example.leo"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.leo"
        minSdk = 26
        targetSdk = 34
        versionCode = 12
        versionName = "12"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // (No BuildConfig API fields right now)
        // Make sure multiline emoji etc. behaves:
        vectorDrawables.useSupportLibrary = true
    }
    lint {
        abortOnError = false
        checkReleaseBuilds = true
        warningsAsErrors = false
        // enable more checks over time; start gentle for v12
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14" // works with Kotlin 2.0.x toolchain
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }



    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug { }
    }

    // Compose + BuildConfig
    buildFeatures {
        compose = true
        buildConfig = true   // keep on; harmless and useful later
    }

    // Kotlin 2.x toolchain
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    // DO NOT set composeOptions.kotlinCompilerExtensionVersion with Kotlin 2.x
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }


    dependencies {
        // --- Compose BOM (keeps Compose libs in sync) ---
        val composeBom = platform("androidx.compose:compose-bom:2024.10.00")
        implementation(composeBom)
        androidTestImplementation(composeBom)

        // --- Core Android ---
        implementation("androidx.core:core-ktx:1.13.1")
        implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
        implementation("androidx.activity:activity-compose:1.9.1")

        // --- Jetpack Compose UI ---
        implementation("androidx.compose.ui:ui")
        implementation("androidx.compose.foundation:foundation")
        implementation("androidx.compose.ui:ui-tooling-preview")
        implementation("androidx.compose.material3:material3") // no explicit version; comes from BOM
        implementation("androidx.compose.material:material-icons-extended")

        // --- Navigation ---
        implementation("androidx.navigation:navigation-compose:2.8.2")

        // --- Serialization ---
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")


        // --- System UI / Splash ---
        implementation("androidx.core:core-splashscreen:1.0.1")

        // --- DataStore / Preferences ---
        implementation("androidx.datastore:datastore-preferences:1.1.1")

        // --- Coroutines ---
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

        // --- (Optional) XML Material (only if you use XML themes anywhere) ---
        implementation("com.google.android.material:material:1.12.0")

        // --- Networking (ready for later API work) ---
        implementation("com.squareup.okhttp3:okhttp:4.12.0")
        implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

        // --- Debug / Tooling ---
        debugImplementation("androidx.compose.ui:ui-tooling")
        debugImplementation("androidx.compose.ui:ui-test-manifest")

        // --- Testing ---
        testImplementation("junit:junit:4.13.2")
        androidTestImplementation("androidx.test.ext:junit:1.2.1")
        androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
        androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    }
}
