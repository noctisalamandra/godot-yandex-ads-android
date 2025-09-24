@tool
extends EditorPlugin

var export_plugin : AndroidExportPlugin

func _enter_tree():
	export_plugin = AndroidExportPlugin.new()
	add_export_plugin(export_plugin)


func _exit_tree():
	remove_export_plugin(export_plugin)
	export_plugin = null


class AndroidExportPlugin extends EditorExportPlugin:
	var _plugin_name = "GodotAndroidYandexAds"

	func _supports_platform(platform):
		if platform is EditorExportPlatformAndroid:
			return true
		return false

	func _get_android_libraries(platform, debug):
		if debug:
			return PackedStringArray([_plugin_name + "/bin/debug/" + _plugin_name + "-debug.aar"])
		else:
			return PackedStringArray([_plugin_name + "/bin/release/" + _plugin_name + "-release.aar"])
	

	func _get_android_dependencies(platform, debug):
		if debug:
			return PackedStringArray(["com.yandex.android:mobileads:7.16.0"])
		else:
			return PackedStringArray(["com.yandex.android:mobileads:7.16.0"])


	func _get_name():
		return _plugin_name
