# Godot Android Yandex Ads

Android-плагин для [Godot 4](https://godotengine.org/), интегрирующий [Yandex Mobile Ads SDK](https://yandex.ru/dev/mobile-ads/).

Поддерживает баннерную, межстраничную и рекламу с вознаграждением.

> Работает только на Android. На других платформах плагин ничего не делает.

## Требования

- Godot 4.x
- Экспорт под Android с включённой сборкой через Gradle

---

## Установка

### 1. Скачивание

Скачайте и распакуйте последний [архив релиза](https://github.com/noctisalamandra/godot-yandex-ads-android/releases/latest).

![Download](screens/download.png)

### 2. Добавление в проект

Скопируйте папку `addons/GodotAndroidYandexAds` в корень вашего проекта Godot.

![Project](screens/project.png)

### 3. Включение плагина

Откройте `Проект → Настройки проекта → Плагины` и включите **GodotAndroidYandexAds**.

![Settings](screens/settings.png)

### 4. Настройка экспорта Android

В настройках экспорта:
- Включите **Use Gradle Build**

![Plugin](screens/plugin.png)

- В разделе **Permissions** включите `Access Network State` и `Internet`

### 5. Настройка Gradle

В файле `android/build/build.gradle` добавьте следующее внутрь блока `android {}`:

```groovy
android {
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }
}
```

### 6. Добавление узла

Создайте Node в вашей сцене и прикрепите скрипт `yandex_ads.gd` из папки плагина.

![Node](screens/node.png)

---

## Настройка

Узел `YandexAds` предоставляет следующие экспортируемые свойства:

| Свойство | Тип | Описание |
|---|---|---|
| `api_key` | `String` | API-ключ AppMetrica (необязательно, для аналитики) |
| `banner_id` | `String` | ID рекламного блока для баннера |
| `banner_on_top` | `bool` | Показывать баннер сверху (`true`) или снизу (`false`, по умолчанию) |
| `banner_width` | `int` | Фиксированная ширина баннера в dp (0 = адаптивный) |
| `banner_height` | `int` | Фиксированная высота баннера в dp (0 = адаптивный) |
| `interstitial_id` | `String` | ID рекламного блока для межстраничной рекламы |
| `rewarded_id` | `String` | ID рекламного блока для рекламы с вознаграждением |

Укажите ID рекламных блоков в Инспекторе:

![Key](screens/key.png)

---

## API

### Методы

#### Баннер

```gdscript
load_banner() -> void              # Загрузить и отобразить баннер
show_banner() -> void              # Показать скрытый баннер
hide_banner() -> void              # Скрыть баннер (остаётся в памяти)
get_banner_dimension() -> Vector2  # Вернуть размер баннера в пикселях
```

#### Межстраничная реклама

```gdscript
load_interstitial() -> void         # Загрузить межстраничную рекламу
show_interstitial() -> void         # Показать загруженную межстраничную рекламу
is_interstitial_loaded() -> bool    # Проверить, готова ли реклама к показу
```

#### Реклама с вознаграждением

```gdscript
load_rewarded_video() -> void         # Загрузить рекламу с вознаграждением
show_rewarded_video() -> void         # Показать загруженную рекламу
is_rewarded_video_loaded() -> bool    # Проверить, готова ли реклама к показу
```

### Сигналы

![Signals](screens/signals.png)

#### Баннер

| Сигнал | Аргументы | Описание |
|---|---|---|
| `banner_loaded` | — | Баннер успешно загружен |
| `banner_failed_to_load` | `error_code: int` | Ошибка загрузки баннера |

#### Межстраничная реклама

| Сигнал | Аргументы | Описание |
|---|---|---|
| `interstitial_loaded` | — | Межстраничная реклама загружена |
| `interstitial_failed_to_load` | `error_code: int` | Ошибка загрузки межстраничной рекламы |
| `interstitial_closed` | — | Пользователь закрыл межстраничную рекламу |

#### Реклама с вознаграждением

| Сигнал | Аргументы | Описание |
|---|---|---|
| `rewarded_video_loaded` | — | Реклама с вознаграждением загружена |
| `rewarded_video_failed_to_load` | `error_code: int` | Ошибка загрузки рекламы |
| `rewarded_video_closed` | — | Пользователь закрыл рекламу |
| `rewarded` | `currency: String, amount: int` | Пользователь получил вознаграждение |

---

## Пример использования

```gdscript
extends Node

func _ready():
    # Подключаем сигналы
    $YandexAds.banner_loaded.connect(_on_banner_loaded)
    $YandexAds.banner_failed_to_load.connect(_on_banner_failed_to_load)
    $YandexAds.interstitial_loaded.connect(_on_interstitial_loaded)
    $YandexAds.rewarded_video_loaded.connect(_on_rewarded_video_loaded)
    $YandexAds.rewarded.connect(_on_rewarded)

    # Загружаем баннер сразу
    $YandexAds.load_banner()

func _on_banner_loaded():
    $YandexAds.show_banner()

func _on_banner_failed_to_load(error_code):
    print("Ошибка загрузки баннера: ", error_code)

# Вызывать, когда нужно показать межстраничную рекламу
func show_interstitial_ad():
    if $YandexAds.is_interstitial_loaded():
        $YandexAds.show_interstitial()
    else:
        $YandexAds.load_interstitial()

func _on_interstitial_loaded():
    $YandexAds.show_interstitial()

# Вызывать, когда нужно показать рекламу с вознаграждением
func show_rewarded_ad():
    if $YandexAds.is_rewarded_video_loaded():
        $YandexAds.show_rewarded_video()
    else:
        $YandexAds.load_rewarded_video()

func _on_rewarded_video_loaded():
    $YandexAds.show_rewarded_video()

func _on_rewarded(currency: String, amount: int):
    print("Получено вознаграждение: %d %s" % [amount, currency])
    # Здесь начислите награду игроку
```

---

## Демо-проект

Полный пример проекта доступен [здесь](https://github.com/noctisalamandra/godot-yandex-ads-android-demo).

## Лицензия

[MIT](LICENSE)
