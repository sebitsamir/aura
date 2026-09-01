# Android Template

A clean, production-ready **Kotlin + Jetpack Compose** starter you can copy for every new Android app — simple or advanced.

Stack is already wired: **Compose Material 3**, **Navigation**, **Hilt**, **Room**, **DataStore**, **Retrofit + OkHttp**, **Coil**, **Coroutines**, **Timber**, **Splash Screen**, release **R8/ProGuard**, and a version catalog for optional libraries (Paging, WorkManager, Moshi, LeakCanary).

---

## Requirements

- Android Studio **Meerkat / Narwhal** or newer (AGP 9.x)
- JDK **17+** (Android Studio’s bundled JBR is fine)
- Android SDK with **API 36** installed

On Windows, if CLI builds fail without `JAVA_HOME`, use:

```bat
gw.bat assembleDebug
```

Or set `JAVA_HOME` to Android Studio’s JBR, or uncomment `org.gradle.java.home` in `gradle.properties`.

---

## Quick start (new app from this template)

### 1. Copy the template

Copy the whole folder (or clone this repo) and rename it to your project name.

### 2. Rename the project

| What | Where | Change to |
|------|--------|-----------|
| Project name | `settings.gradle.kts` → `rootProject.name` | Your app name |
| Namespace / applicationId | `app/build.gradle.kts` | `com.yourcompany.yourapp` |
| Package folders | `app/src/.../com/example/app` | Match the new package |
| App display name | `res/values/strings.xml` → `app_name` | Your name |
| Theme names (optional) | `themes.xml`, Compose `AppTheme` | Your branding |

**Android Studio:** `Refactor → Rename` on the package, or use **Edit → Find in Files** for `com.aura.app` and replace carefully.

### 3. Sync & run

1. Open the folder in Android Studio  
2. Let Gradle sync finish (first sync downloads dependencies once)  
3. Run on an emulator or device  

```bat
gradlew.bat assembleDebug
```

### 4. Point networking at your API

In `di/AppModule.kt`, change:

```kotlin
val baseUrl = "https://api.example.com/"
```

Then add interfaces under `data/remote/` and inject them with Hilt.

---

## What’s included

### Already enabled in `:app`

| Area | Libraries |
|------|-----------|
| UI | Compose, Material 3, Icons Extended, Navigation (type-safe routes) |
| Architecture | Hilt DI, ViewModel, Coroutines/Flow |
| Local data | Room (+ KSP), DataStore Preferences |
| Network | Retrofit, OkHttp logging, Kotlinx Serialization |
| Images | Coil |
| UX / quality | Splash Screen, Timber, LeakCanary (debug), R8 minify (release) |

### In the version catalog (ready to uncomment)

In `app/build.gradle.kts` (bottom of `dependencies`):

- Paging 3  
- WorkManager + Hilt Worker  
- Moshi (if you prefer it over kotlinx.serialization)  

Versions live in `gradle/libs.versions.toml` — update once, reuse everywhere.

---

## Suggested package layout

```
com.yourapp/
├── App.kt                 # @HiltAndroidApp
├── MainActivity.kt        # @AndroidEntryPoint
├── di/                    # Hilt modules (network, DB, …)
├── ui/
│   ├── navigation/        # NavHost + routes
│   ├── screens/           # Screen composables
│   ├── components/        # Shared UI pieces (add as needed)
│   └── theme/             # Color, Type, AppTheme
├── data/
│   ├── local/             # Room DB, DAOs, entities
│   ├── remote/            # API + DTOs
│   └── repository/        # Repository implementations
└── domain/
    ├── model/             # Domain models
    └── usecase/           # Optional use cases for complex apps
```

**Simple apps:** ViewModel → Repository → Room/API is enough.  
**Advanced apps:** add use cases, feature modules, and stricter domain boundaries.

---

## Tips for any kind of project

### Simple UI-only app
- Keep `HomeScreen` / add a few screens in `ui/screens`
- You can leave Room/Retrofit unused; they don’t hurt until you call them
- Or comment out unused `implementation` lines in `app/build.gradle.kts` to slim the APK

### App with local database
1. Create entities + DAO under `data/local`
2. Create `AppDatabase`
3. Provide them in `DatabaseModule`
4. Expose via a repository + ViewModel

### App with API
1. Define Retrofit interfaces in `data/remote`
2. `@Provides` your API in `AppModule` (`retrofit.create(...)`)
3. Map DTOs → domain models in the repository

### Multi-module (larger / advanced)
1. Keep `:app` as the thin shell (Application, NavHost, DI entry)
2. Add modules in `settings.gradle.kts`, e.g. `:core:network`, `:feature:home`
3. Root `build.gradle.kts` already declares `android.library` — apply it in new modules
4. Share versions via `libs.versions.toml`

### Branding
- Update `ui/theme/Color.kt` and set `dynamicColor = false` in `AppTheme` if you want a fixed brand palette
- Replace launcher icons (Image Asset Studio)
- Change `splash_background` in `colors.xml`

### Release / Play Store
- Release builds already enable minify + shrink resources
- Create a real keystore; never commit `.jks` / `keystore.properties`
- Bump `versionCode` / `versionName` in `app/build.gradle.kts`
- Fill `backup_rules.xml` / `data_extraction_rules.xml` before shipping

### Stability habits
- Prefer catalog aliases (`libs....`) over hard-coded coordinates
- Bump the Compose **BOM** together; don’t pin Compose artifacts individually
- Keep KSP version aligned with Kotlin (`ksp` in the catalog)
- After renaming packages, **Clean Project** then rebuild
- Treat `local.properties` as machine-local (already gitignored)

---

## First-build checklist

- [ ] Renamed `rootProject.name`, `namespace`, `applicationId`
- [ ] Renamed Kotlin package folders
- [ ] Updated `app_name` and launcher icons
- [ ] Set Retrofit `baseUrl` (or remove network wiring if unused)
- [ ] Synced Gradle and ran `assembleDebug`
- [ ] (Optional) Initialized git: `git init` then first commit

---

## Useful Gradle commands

```bat
gradlew.bat assembleDebug
gradlew.bat assembleRelease
gradlew.bat test
gradlew.bat lint
gradlew.bat clean
```

---

## Notes

- First Gradle sync downloads dependencies from Google/Maven Central — that is normal. After that, the cache is local; you do not need to “install” libraries again for this project.
- `minSdk = 26` covers the vast majority of devices; lower it only if you must support older phones.
- Placeholder package is `com.aura.app` — always change it before publishing.
