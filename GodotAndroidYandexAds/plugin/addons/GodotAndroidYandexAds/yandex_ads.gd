extends Node

class_name YandexAds

# signals
signal banner_loaded
signal banner_failed_to_load(error_code)

signal interstitial_failed_to_load(error_code)
signal interstitial_loaded
signal interstitial_closed

signal rewarded_video_loaded
signal rewarded_video_closed
signal rewarded(currency, amount)
signal rewarded_video_failed_to_load(error_code)

# properties
@export var banner_on_top:bool = false
@export var api_key:String
@export var banner_id:String
@export var interstitial_id:String
@export var rewarded_id:String

# "private" properties
var _yandex_singleton = null
var _is_interstitial_loaded:bool = false
var _is_rewarded_video_loaded:bool = false

func _enter_tree():
	if not init():
		print("Yandex Java Singleton not found. This plugin will only work on Android")

# initialization
func init() -> bool:
	if(Engine.has_singleton("GodotAndroidYandexAds")):
		_yandex_singleton = Engine.get_singleton("GodotAndroidYandexAds")
		if not _yandex_singleton.is_connected("_on_banner_loaded", _on_banner_loaded):
			connect_signals()
			_yandex_singleton.init(api_key) #
			return true
	return false

# connect the YandexAds Java signals
func connect_signals() -> void:
	# Banner
	_yandex_singleton._on_banner_loaded.connect(_on_banner_loaded)
	_yandex_singleton._on_banner_failed_to_load.connect(_on_banner_failed_to_load)

	# Interstitial
	_yandex_singleton._on_interstitial_loaded.connect(_on_interstitial_loaded)
	_yandex_singleton._on_interstitial_failed_to_load.connect(_on_interstitial_failed_to_load)
	_yandex_singleton._on_interstitial_ad_dismissed.connect(_on_interstitial_ad_dismissed)

	# Rewarded
	_yandex_singleton._on_rewarded.connect(_on_rewarded)
	_yandex_singleton._on_rewarded_video_ad_loaded.connect(_on_rewarded_video_ad_loaded)
	_yandex_singleton._on_rewarded_video_ad_failed_to_load.connect(_on_rewarded_video_ad_failed_to_load)
	_yandex_singleton._on_rewarded_video_ad_dismissed.connect(_on_rewarded_video_ad_closed)
	_yandex_singleton._on_returned_to_application_after_rewarded_video.connect(_on_rewarded_video_ad_closed)

# load
func load_banner() -> void:
	if _yandex_singleton != null:
		_yandex_singleton.loadBanner(banner_id, banner_on_top)

func load_interstitial() -> void:
	if _yandex_singleton != null:
		_yandex_singleton.loadInterstitial(interstitial_id)

func is_interstitial_loaded() -> bool:
	if _yandex_singleton != null:
		return _is_interstitial_loaded
	return false

func load_rewarded_video() -> void:
	if _yandex_singleton != null:
		_yandex_singleton.loadRewardedVideo(rewarded_id)

func is_rewarded_video_loaded() -> bool:
	if _yandex_singleton != null:
		return _is_rewarded_video_loaded
	return false

# show / hide
func show_banner() -> void:
	if _yandex_singleton != null:
		_yandex_singleton.showBanner()

func hide_banner() -> void:
	if _yandex_singleton != null:
		_yandex_singleton.hideBanner()

func show_interstitial() -> void:
	if _yandex_singleton != null:
		_yandex_singleton.showInterstitial()
		_is_interstitial_loaded = false

func show_rewarded_video() -> void:
	if _yandex_singleton != null:
		_yandex_singleton.showRewardedVideo()
		_is_rewarded_video_loaded = false


# dimension
func get_banner_dimension() -> Vector2:
	if _yandex_singleton != null:
		return Vector2(_yandex_singleton.getBannerWidth(), _yandex_singleton.getBannerHeight())
	return Vector2()

# callbacks
# banner
func _on_banner_loaded() -> void:
	emit_signal("banner_loaded")

func _on_banner_failed_to_load(error_code:int) -> void:
	emit_signal("banner_failed_to_load", error_code)

#interstitial
func _on_interstitial_failed_to_load(error_code:int) -> void:
	_is_interstitial_loaded = false
	emit_signal("interstitial_failed_to_load", error_code)

func _on_interstitial_loaded() -> void:
	_is_interstitial_loaded = true
	emit_signal("interstitial_loaded")

func _on_returned_to_application_after_interstitial() -> void:
	emit_signal("interstitial_closed")

func _on_interstitial_ad_dismissed() -> void:
	emit_signal("interstitial_closed")

#rewarded
func _on_rewarded_video_ad_loaded() -> void:
	_is_rewarded_video_loaded = true
	emit_signal("rewarded_video_loaded")

func _on_rewarded(currency:String, amount:int) -> void:
	emit_signal("rewarded", currency, amount)

func _on_rewarded_video_ad_failed_to_load(error_code:int) -> void:
	_is_rewarded_video_loaded = false
	emit_signal("rewarded_video_failed_to_load", error_code)

func _on_rewarded_video_ad_closed() -> void:
	emit_signal("rewarded_video_closed")