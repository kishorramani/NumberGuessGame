# 🎯 Number Guess Game (Compose Multiplatform)

A modern, cross-platform Number Guessing Game built with **Kotlin Multiplatform (KMP)** and **Compose Multiplatform**, targeting **Android**, **iOS**, and **Desktop (JVM)** with 100% shared UI and business logic.

---

## 🏗️ Architecture: MVI (Model-View-Intent)

This project strictly adheres to the **MVI (Model-View-Intent)** architectural pattern powered by **Unidirectional Data Flow (UDF)**.

```
       +-------------------------------------------------------+
       |                 NumberGuessViewModel                  |
       |                                                       |
       |   • Holds private mutable state (_state)              |
       |   • Exposes immutable StateFlow<NumberGuessState>     |
       |   • Processes business logic in onAction()            |
       +-------------------------------------------------------+
                      |                                   ▲
       (State Down)   |                                   |  (Events Up)
  StateFlow<State>    |                                   |  NumberGuessAction
                      ▼                                   |
       +-------------------------------------------------------+
       |                NumberGuessScreenRoot                  |
       |  (Stateful Composable: Collects state & wires actions)|
       +-------------------------------------------------------+
                      |                                   ▲
       (State Down)   |                                   |  (Events Up)
     NumberGuessState |                                   |  onAction lambda
                      ▼                                   |
       +-------------------------------------------------------+
       |                  NumberGuessScreen                    |
       |     (Stateless Composable: Pure rendering UI)         |
       |                                                       |
       |   • TextField (Input number)                          |
       |   • Button ("Make guess")                             |
       |   • Text (Feedback / attempts result)                 |
       |   • Button ("Start new game" when solved)             |
       +-------------------------------------------------------+
```

---

## 🔄 State Management: State Down, Events Up

### 1. State Pass Down (Unidirectional Data Flow)
- **Immutable State**: The entire state of the screen is encapsulated in a single, read-only data class:
  ```kotlin
  data class NumberGuessState(
      val numberText: String = "",
      val guessText: String? = null,
      val isGuessCorrect: Boolean = false
  )
  ```
- **Single Source of Truth**: The `NumberGuessViewModel` maintains this state using a private `MutableStateFlow` and exposes an immutable `StateFlow<NumberGuessState>`:
  ```kotlin
  private val _state = MutableStateFlow(NumberGuessState())
  val state = _state.asStateFlow()
  ```
- **State Passing**: The state is collected with lifecycle awareness (`collectAsStateWithLifecycle()`) in `NumberGuessScreenRoot` and passed down directly into the stateless `NumberGuessScreen`:
  ```kotlin
  @Composable
  fun NumberGuessScreenRoot(modifier: Modifier = Modifier) {
      val viewModel = viewModel { NumberGuessViewModel() }
      val state by viewModel.state.collectAsStateWithLifecycle()

      NumberGuessScreen(
          state = state,
          onAction = viewModel::onAction,
          modifier = modifier
      )
  }
  ```

### 2. Events Bubble Up (User Intents)
- **Typed Actions**: User interactions are modeled as explicit, sealed actions rather than arbitrary function calls:
  ```kotlin
  sealed interface NumberGuessAction {
      data class OnNumberTextChange(val numberText: String): NumberGuessAction
      data object OnGuessClick: NumberGuessAction
      data object OnStartNewGameButtonClick: NumberGuessAction
  }
  ```
- **Decoupled UI**: The stateless `NumberGuessScreen` has **zero** knowledge of the ViewModel. It receives an `onAction: (NumberGuessAction) -> Unit` lambda and invokes it whenever a user interacts with the UI:
  - User types in the text box $\rightarrow$ emits `OnNumberTextChange`
  - User taps "Make guess" $\rightarrow$ emits `OnGuessClick`
  - User taps "Start new game" $\rightarrow$ emits `OnStartNewGameButtonClick`
- **Centralized Logic**: The ViewModel's `onAction(action: NumberGuessAction)` handles every event in a single `when` statement, calculates attempts and comparison results, and publishes a new immutable state with `_state.update { ... }`.

### 3. Benefits of this Pattern
- **Predictability**: The UI is a pure function of its State: `UI = f(State)`.
- **Zero Race Conditions**: Because state can only be modified via actions dispatched to the ViewModel, state mutations are sequential and synchronized.
- **Previewability & Testability**: The stateless composable `NumberGuessScreen` can be easily rendered in Compose Previews or verified in unit/screenshot tests by simply feeding mock `NumberGuessState` objects.
- **Cross-Platform Compatibility**: Uses explicit ViewModel factories (`viewModel { NumberGuessViewModel() }`) to ensure flawless execution across Android, iOS (Kotlin/Native without reflection), and Desktop.

---

## 📂 Project Structure

```
.
├── androidApp/                 # Android application entry point
│   └── src/main/
│       ├── AndroidManifest.xml
│       └── kotlin/app/numberguessgame/
│           └── MainActivity.kt
├── iosApp/                     # iOS Xcode project & SwiftUI entry point
│   ├── Configuration/
│   │   └── Config.xcconfig
│   ├── iosApp/
│   │   ├── iOSApp.swift        # SwiftUI App lifecycle
│   │   └── ContentView.swift  # Embeds Compose MainViewController
│   └── iosApp.xcodeproj
├── desktopApp/                 # Desktop JVM entry point
│   └── src/main/kotlin/app/numberguessgame/
│       └── main.kt             # Compose Desktop window launcher
└── shared/                     # 100% shared multiplatform codebase
    └── src/
        ├── commonMain/kotlin/app/numberguessgame/
        │   ├── App.kt                     # Root app layout & theme
        │   ├── NumberGuessState.kt        # MVI State definition
        │   ├── NumberGuessAction.kt       # MVI Actions / Intents
        │   ├── NumberGuessViewModel.kt    # State holder & game logic
        │   └── NumberGuessScreenRoot.kt   # Stateful Root & Stateless Screen UI
        └── iosMain/kotlin/app/numberguessgame/
            └── MainViewController.kt      # ComposeUIViewController bridge for iOS
```

---

## 🚀 Running the Application

### 📱 iOS (Simulator)
1. Ensure an iOS simulator is booted:
   ```bash
   xcrun simctl list devices | grep -i booted
   ```
2. Build and run via Xcode:
   Open `iosApp/iosApp.xcodeproj` in Xcode, select your simulator, and click **Run**.
3. Or build and launch from the command line:
   ```bash
   xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug -destination 'platform=iOS Simulator,name=iPhone 16' build
   xcrun simctl install booted build/DerivedData/Build/Products/Debug-iphonesimulator/NumberGuessGame.app
   xcrun simctl launch booted app.numberguessgame.NumberGuessGame
   ```

### 🤖 Android
Run on an Android device or emulator:
```bash
./gradlew :androidApp:installDebug
```

### 💻 Desktop (macOS / Linux / Windows)
Run the desktop application:
```bash
./gradlew :desktopApp:run
```
Or run with hot-reload during development:
```bash
./gradlew :desktopApp:hotRun --auto
```

---

## 🛠️ Tech Stack & Dependencies

- **Kotlin**: 2.4.21
- **Compose Multiplatform**: 1.12.2
- **Material 3**: Compose Material3 multiplatform components
- **AndroidX Lifecycle / ViewModel Compose**: 2.11.0 (multiplatform state management)
- **Kotlinx Coroutines**: 1.11.0
