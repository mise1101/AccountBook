# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
./gradlew assembleDebug       # build debug APK
./gradlew installDebug        # build and install on connected device
./gradlew test                # run unit tests
./gradlew connectedAndroidTest # run instrumented tests
```

## Architecture

Single-Activity MVVM app built with Kotlin + Jetpack Compose + Room.

- **`MainActivity.kt`** — sole Activity with `launchMode="singleTop"`. Uses a counter-based approach (`addRequestCount: MutableIntState`) with `onNewIntent` to handle repeated widget taps — each increment triggers `LaunchedEffect` to re-show the add-transaction screen.
- **`MainViewModel`** — single `AndroidViewModel` holding all app state: filter, month offset, form state, categories (as `StateFlow`), transactions (via `flatMapLatest` on filter), and monthly totals (via `combine` + `flatMapLatest` on month offset). All DB mutations go through `viewModelScope.launch`.
- **Navigation** — no Navigation Component. Screens are full-screen composables managed by boolean state in `AccountBookApp` (`isAdding`, `isManagingCategories`). The two main tabs (Home / Stats) use a `Scaffold` + `NavigationBar`.

## Data Layer

- **Room** with two tables: `categories` (predefined + user-added) and `transactions` (FK to `categories` with `RESTRICT` on delete). Uses KSP for annotation processing.
- **`AppDatabase`** — singleton with `onCreate` callback that seeds 9 expense + 6 income default categories.
- **DAOs** return `Flow<List<...>>` for all queries. `TransactionDao.getByTypeWithCategory` uses embedded `TransactionWithCategory` for the join.
- **`AppRepository`** — thin wrapper over DAOs, exists to avoid ViewModel coupling to Room directly.

## Widget

- `AccountBookWidgetProvider` sends `ACTION_ADD_TRANSACTION` intent to `MainActivity`. The Activity's `launchMode="singleTop"` ensures `onNewIntent` handles subsequent taps while the Activity is already open.

## UI Theme

Material 3 with Android 12+ dynamic color. `AccountBookTheme` wraps content; colors defined in `Color.kt`, typography in `Type.kt`.
