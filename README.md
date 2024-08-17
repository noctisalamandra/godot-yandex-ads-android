# Godot YandexAdsAndroid

This plugin is designed to connect Yandex advertising to godot.

## Instruction

### 1. Downloading

Download and unpack this [archive](https://github.com/noctisalamandra/godot-yandex-ads-android/releases/latest).

![Download](screens/download.png)

### 2. Distribution by files

Adding a folder to the root section of the project.

![Project](screens/project.png)

### 3. Project Setup

In the project settings, enable the plugin.

![Settings](screens/settings.png)

When exporting a project, enable "Use Grade Build".

![Plugin](screens/plugin.png)

In the "Permissions" section, enable "Access Network State" and "Internet".

Then create a node for the advertising module where you need it and connect the "yandex_ads.gd" script, which can be found in the folder that we added.

![Node](screens/node.png)

And insert the ID of your ad.

![Key](screens/key.png)

In build.gradle along the way "android/build/build.gradle" add the following code:

```
android {

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
}
```

Then you can use the signals and functions written in the script to display ads.

![Signals](screens/signals.png)

```
func _ready():
	$YandexAds.load_banner() 

func _on_rewarded_pressed():
	$YandexAds.load_rewarded_video()

func _on_interstitial_pressed():
	$YandexAds.load_interstitial()

func _on_yandex_ads_rewarded_video_loaded():
	$YandexAds.show_rewarded_video() 

func _on_yandex_ads_interstitial_loaded():
	$YandexAds.show_interstitial() 

func _on_yandex_ads_banner_loaded():
	$YandexAds.show_banner() 

func _on_yandex_ads_rewarded(currency, amount):
	pass
```

An example of plug-in connection can be found [here](https://github.com/noctisalamandra/godot-yandex-ads-android-demo).