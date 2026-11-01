plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.ferdian"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.ferdian"
        minSdk = 23
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
}

dependencies {
    implementation(libs.androidx.ui)
    // Define versions here
    val tensorflowVersion = "2.10.0"
    val tensorflowSupportVersion = "0.4.0"
    val tensorflowGpuVersion = "2.13.0"
    val tensorflowTaskVisionVersion = "0.4.4"
    val tensorflowGpuDelegateVersion = "0.4.0"
    val navVersion = "2.8.2"
    val cameraxVersion = "1.2.0-alpha02"
    val androidxCoreKtxVersion = "1.8.0"
    val androidxAppCompatVersion = "1.6.1"
    val materialVersion = "1.9.0"
    val constraintLayoutVersion = "2.1.4"
    val windowManagerVersion = "1.1.0-alpha02"

    // TensorFlow Lite dependencies
    implementation("org.tensorflow:tensorflow-lite:$tensorflowVersion")
    implementation("org.tensorflow:tensorflow-lite-support:$tensorflowSupportVersion")
    implementation("org.tensorflow:tensorflow-lite-metadata:$tensorflowSupportVersion")
    implementation("org.tensorflow:tensorflow-lite-gpu:$tensorflowGpuVersion")
    implementation("org.tensorflow:tensorflow-lite-task-vision:$tensorflowTaskVisionVersion")
    implementation("org.tensorflow:tensorflow-lite-gpu-delegate-plugin:$tensorflowGpuDelegateVersion")

    // Navigation library
    implementation("androidx.navigation:navigation-runtime-ktx:$navVersion")
    implementation("androidx.navigation:navigation-fragment-ktx:$navVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navVersion")

    // CameraX dependencies
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")

    // Core AndroidX libraries
    implementation("androidx.core:core-ktx:$androidxCoreKtxVersion")
    implementation("androidx.appcompat:appcompat:$androidxAppCompatVersion")
    implementation("com.google.android.material:material:$materialVersion")
    implementation("androidx.constraintlayout:constraintlayout:$constraintLayoutVersion")

    // Jetpack Compose UI
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // WindowManager for multi-window support
    implementation("androidx.window:window:$windowManagerVersion")

    // Unit testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
