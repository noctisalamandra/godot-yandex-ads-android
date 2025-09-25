extends Node

class_name YandexAds

# Signals
# Banner
signal banner_loaded
signal banner_failed_to_load(error_code)

# Interstitial
signal interstitial_loaded
signal interstitial_failed_to_load(error_code)
signal interstitial_closed

# Rewarded
signal rewarded(currency, amount)
signal rewarded_video_loaded
signal rewarded_video_failed_to_load(error_code)
signal rewarded_video_closed

# Properties
@export var api_key: String
@export var banner_id: String
@export var banner_on_top: bool = false
@export var banner_width: int = 0
@export var banner_height: int = 0
@export var interstitial_id: String
@export var rewarded_id: String

# "Private" properties
var _yandex_singleton = null
var _is_interstitial_loaded: bool = false
var _is_rewarded_video_loaded: bool = false

func _enter_tree():
	if not init():
		print("Yandex Java Singleton not found. This plugin will only work on Android")

# Initialization
func init() -> bool:
	if Engine.has_singleton("GodotAndroidYandexAds"):
		_yandex_singleton = Engine.get_singleton("GodotAndroidYandexAds")
		var banner_callable = Callable(self, "_on_banner_loaded")
		if not _yandex_singleton.is_connected("_on_banner_loaded", banner_callable):
			connect_signals()
		_yandex_singleton.init(api_key)
		return true
	return false

# Connect the YandexAds Java signals
func connect_signals() -> void:
	connect_banner_signals()
	connect_interstitial_signals()
	connect_rewarded_signals()

func connect_banner_signals() -> void:
	_yandex_singleton._on_banner_loaded.connect(_on_banner_loaded)
	_yandex_singleton._on_banner_failed_to_load.connect(_on_banner_failed_to_load)

func connect_interstitial_signals() -> void:
	_yandex_singleton._on_interstitial_loaded.connect(_on_interstitial_loaded)
	_yandex_singleton._on_interstitial_failed_to_load.connect(_on_interstitial_failed_to_load)
	_yandex_singleton._on_interstitial_ad_dismissed.connect(_on_interstitial_ad_dismissed)

func connect_rewarded_signals() -> void:
	_yandex_singleton._on_rewarded.connect(_on_rewarded)
	_yandex_singleton._on_rewarded_video_ad_loaded.connect(_on_rewarded_video_ad_loaded)
	_yandex_singleton._on_rewarded_video_ad_failed_to_load.connect(_on_rewarded_video_ad_failed_to_load)
	_yandex_singleton._on_rewarded_video_ad_dismissed.connect(_on_rewarded_video_ad_dismissed)

# Load
func load_banner() -> void:
	if _yandex_singleton:
		_yandex_singleton.loadBanner(banner_id, banner_on_top, banner_width, banner_height)

func load_interstitial() -> void:
	if _yandex_singleton:
		_yandex_singleton.loadInterstitial(interstitial_id)

func is_interstitial_loaded() -> bool:
	return _is_interstitial_loaded

func load_rewarded_video() -> void:
	if _yandex_singleton:
		_yandex_singleton.loadRewardedVideo(rewarded_id)

func is_rewarded_video_loaded() -> bool:
	return _is_rewarded_video_loaded

# Show / hide
func show_banner() -> void:
	if _yandex_singleton:
		_yandex_singleton.showBanner()

func hide_banner() -> void:
	if _yandex_singleton:
		_yandex_singleton.hideBanner()

func show_interstitial() -> void:
	if _yandex_singleton:
		_yandex_singleton.showInterstitial()
		_is_interstitial_loaded = false

func show_rewarded_video() -> void:
	if _yandex_singleton:
		_yandex_singleton.showRewardedVideo()
		_is_rewarded_video_loaded = false

# Dimension
func get_banner_dimension() -> Vector2:
	if _yandex_singleton:
		return Vector2(_yandex_singleton.getBannerWidth(), _yandex_singleton.getBannerHeight())
	return Vector2()

# Callbacks
# Banner
func _on_banner_loaded() -> void:
	emit_signal("banner_loaded")

func _on_banner_failed_to_load(error_code: int) -> void:
	emit_signal("banner_failed_to_load", error_code)

# Interstitial
func _on_interstitial_loaded() -> void:
	_is_interstitial_loaded = true
	emit_signal("interstitial_loaded")

func _on_interstitial_failed_to_load(error_code: int) -> void:
	_is_interstitial_loaded = false
	emit_signal("interstitial_failed_to_load", error_code)

func _on_interstitial_ad_dismissed() -> void:
	emit_signal("interstitial_closed")

# Rewarded
func _on_rewarded(currency: String, amount: int) -> void:
	emit_signal("rewarded", currency, amount)

func _on_rewarded_video_ad_loaded() -> void:
	_is_rewarded_video_loaded = true
	emit_signal("rewarded_video_loaded")

func _on_rewarded_video_ad_failed_to_load(error_code: int) -> void:
	_is_rewarded_video_loaded = false
	emit_signal("rewarded_video_failed_to_load", error_code)

func _on_rewarded_video_ad_dismissed() -> void:
	emit_signal("rewarded_video_closed")
