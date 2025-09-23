import org.gradle.api.tasks.bundling.AbstractArchiveTask
import com.android.build.gradle.internal.tasks.factory.dependsOn

plugins {
    id("com.android.library")
}

val pluginName = "GodotAndroidYandexAds"
val pluginPackageName = "com.darkmoonight.godotandroidyandexads"

android {
    namespace = pluginPackageName
    compileSdk = 36

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        minSdk = 24

        manifestPlaceholders["godotPluginName"] = pluginName
        manifestPlaceholders["godotPluginPackageName"] = pluginPackageName
        buildConfigField("String", "GODOT_PLUGIN_NAME", "\"${pluginName}\"")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation("com.yandex.android:mobileads:7.16.0")
    implementation("io.appmetrica.analytics:analytics:7.12.0")
    implementation("org.godotengine:godot:4.5.0.stable")
    implementation("androidx.test.ext:junit:1.3.0")
}

tasks.withType<AbstractArchiveTask>().configureEach {
    archiveBaseName.set(pluginName)
}

val copyDebugAARToPluginAddons by tasks.registering(Copy::class) {
    from("build/outputs/aar")
    include("$pluginName-debug.aar")
    into("plugin/addons/$pluginName/bin/debug")
}

val copyReleaseAARToPluginAddons by tasks.registering(Copy::class) {
    from("build/outputs/aar")
    include("$pluginName-release.aar")
    into("plugin/addons/$pluginName/bin/release")
}

val cleanPluginAddons by tasks.registering(Delete::class) {
    delete("plugin/addons/$pluginName")
}

val copyAddonsToPlugin by tasks.registering(Copy::class) {
    dependsOn(cleanPluginAddons)
    finalizedBy(copyDebugAARToPluginAddons)
    finalizedBy(copyReleaseAARToPluginAddons)

    from("export_scripts")
    into("plugin/addons/$pluginName")
}

tasks.named("assemble").configure {
    finalizedBy(copyAddonsToPlugin)
}

tasks.named<Delete>("clean").apply {
    dependsOn(cleanPluginAddons)
}
