# Godot YandexAdsAndroid
This plugin is designed to connect Yandex advertising to godot.

## Instruction

### 1. Downloading
Download and unpack this archive.

![Download](screens/download.png)

### 2. Distribution by files
Move the contents of the 'yandex-plugin' folder to /android/plugins.

![Download](screens/file1.png)

Move the 'yandex-ads-lib' folder to your project.

![Download](screens/file2.png)

### 3. Project Setup
In the project export, specify a custom build and plugins.

![Download](screens/plugins.png)

Next, create a node for the advertising module where you need it.

![Download](screens/node.png)

And insert the ID of your ad.

![Download](screens/key.png)

In build.gradle the module file of your application add the following code:
```
dependencies {
    ...
    implementation 'com.yandex.android:mobileads:5.5.0'
}
```

```
android {

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
}
```

Then you can use the signals and functions written in the script to display ads.

![Download](screens/signals.png)

```
func _on_CoinPlus_pressed():
	$Yandex.load_rewarded_video()
  
func _on_Yandex_rewarded_video_loaded():
	$Yandex.show_rewarded_video()
```
