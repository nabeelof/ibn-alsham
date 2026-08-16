# KMM Mobile App — AGENTS.md

## Project Overview

This is a **Kotlin Multiplatform Mobile (KMM)** app. Android uses **Jetpack Compose**, iOS uses **SwiftUI**, and shared business logic lives in the KMM layer.

## Quick Commands

```bash
# Build
./gradlew assembleDebug                        # Android debug APK
./gradlew assembleRelease                      # Android release APK

# Lint & Format
./gradlew spotlessApply                        # Auto-format Kotlin
./gradlew spotlessCheck                        # Check formatting
./gradlew detekt                               # Static analysis
swiftformat iosApp                             # Format Swift code

# Test
./gradlew testDebugUnitTest                    # Android unit tests
./gradlew :kmm:feature:{name}:testDebugUnitTest  # Single KMM module tests
./gradlew koverHtmlReport                      # Coverage report
```

## Architecture

- **MVI pattern** everywhere: `Action` (user events) -> `UiState` (immutable state) -> `SideEffect` (one-shot events)
- **Clean Architecture layers**: Presentation -> Domain -> Data
- **Data mapping**: `DTO -> Domain Model -> ViewState` at each layer boundary
- **Error handling**: `Result<Error, T>` sealed class — no thrown exceptions for business logic
- **Cache-based navigation**: Complex objects passed via typed `CacheKey<T>`, not nav arguments
- **DI**: Koin everywhere — each module has a DI module with `module()` function

## Module Structure

```
kmm/
├── feature/      # Feature modules (per screen/flow)
├── data/         # Cross-feature data (API clients, repositories)
├── library/      # Shared utilities (core, network, database, cache, mvi, di)
├── infra/        # Platform capabilities (auth, deepLinking, featureFlag, analytics, logger, tracker, userSettings)
├── service/      # Cross-cutting service wrappers (authentication, i18n, notification, customer)
└── shared/       # iOS-only XCFramework aggregator

ui/
├── design/       # Android design system components
└── feature/      # Android feature UI modules
```

**Android** depends on individual KMM modules directly. **iOS** imports a single compiled `Shared.xcframework`.

## Coding Conventions

- **Kotlin style**: ktlint, 4-space indent, max line length 120
- **No star imports** (`ij_kotlin_name_count_to_use_star_import = 999`)
- **Trailing commas** allowed and encouraged
- **Composable functions** exempt from standard function naming rules
- **Convention plugins** — use KMP library plugin for KMM modules, Android feature plugin for UI modules
- **Test structure**: Given-When-Then with Turbine for Flow testing, kotest assertions
- **Value objects** for domain safety (Email, PhoneNumber, etc.)
- **Always run** `./gradlew spotlessApply detekt` before committing

## Key Patterns

### MVI ViewModel
```kotlin
class MyViewModel(/* use cases */) : KMMViewModel(), ViewModelContract<UiState, Action, SideEffect>,
    MVI<UiState, Action, SideEffect> by mvi(UiState.defaultInstance()) {

    override fun onAction(action: Action) { /* dispatch */ }
}
```

### iOS ViewModel Bridge
SwiftUI wraps KMM ViewModels with `@Published` properties and `watchState()`/`watchEffect()` observers with `DisposableHandle` cleanup.

### Koin Module
```kotlin
val myModule = module {
    viewModel { MyViewModel(get(), get()) }
    factory<MyRepository> { MyRepositoryImpl(get()) }
}
```

## Important Notes

- **Detekt is strict**: configure max issues = 0, cyclomatic complexity <= 15, method length <= 60 lines
- **Feature flags** — check before assuming a feature is enabled
- Convention plugins reduce `build.gradle.kts` to ~5-15 lines per module

## Skills Reference

For detailed guides, use the slash commands:
- `/kmm-app-from-scratch` — Complete A-to-Z plan to build a KMM app from scratch
- `/kmm-development-guide` — Step-by-step workflows, code templates, checklists
- `/kmm-project-architecture` — Module map, key file paths, architecture deep-dive
- `/deep-linking` — Deep link implementation guide for both platforms
