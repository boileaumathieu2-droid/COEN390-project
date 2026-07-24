plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.zone"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.zone"
        minSdk = 24
        targetSdk = 34
        versionCode = 4
        versionName = "1.4"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
    implementation("androidx.activity:activity:1.8.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.github.PhilJay:MPAndroidChart:3.1.0")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.robolectric:robolectric:4.12.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
}

android {
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
    lint {
        // Legacy BLE scanning uses fine location only through Android 11.
        disable += "CoarseFineLocation"
    }
}
