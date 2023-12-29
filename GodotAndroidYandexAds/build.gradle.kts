plugins {
    id("com.android.library")
}

android {
    namespace = "com.darkmoonight.godotandroidyandexads"
    compileSdk = 34

    defaultConfig {
        minSdk = 23

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    compileOnly(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar"))))
    implementation("com.yandex.android:mobileads:6.3.0")        // Yandex mobile ads
    implementation("io.appmetrica.analytics:analytics:6.0.0")   // Yandex metrics
    implementation("org.godotengine:godot:4.2.1.stable")        // Godot
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}