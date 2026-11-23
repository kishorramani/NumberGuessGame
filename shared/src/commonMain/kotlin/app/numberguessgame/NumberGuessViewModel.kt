package app.numberguessgame

import androidx.appstate.statestore.StateStore
import androidx.appstate.transform.transform
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.random.Random

/**
 * [NumberGuessViewModel] manages the business logic and state for the Number Guessing game.
 *
 * It showcases the latest modern Jetpack AppState and Compose state management architecture:
 * 1. **Transform** (`androidx.appstate.transform:transform`):
 *    Headless `@Composable` block that tracks Compose state dependencies outside the UI and
 *    exposes a unified [State] object directly to the UI without `StateFlow`.
 * 2. **StateStore** (`androidx.appstate.statestore:StateStore`):
 *    A centralized, type-safe container for shared/persistent application-level state ([GameStats]).
 * 3. **Atomic Snapshot Transactions** ([Snapshot.withMutableSnapshot]):
 *    Groups multiple state modifications into a single atomic transaction so observers and
 *    the `transform` engine never observe intermediate inconsistent states.
 *
 * @param coroutineScope Optional [CoroutineScope] used to drive the state transform (defaults to [viewModelScope]).
 * @param dispatcher Optional [CoroutineDispatcher] for transform calculations (defaults to [Dispatchers.Default]).
 * @param stateStore Optional [StateStore] instance for application-scoped stats (defaults to [AppStatePersistence.stateStore]).
 */
class NumberGuessViewModel(
    coroutineScope: CoroutineScope? = null,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val stateStore: StateStore = AppStatePersistence.stateStore
) : ViewModel() {

    // Scope used to manage the lifetime of the AppState transform
    private val scope = coroutineScope ?: viewModelScope

    // Target random number between 1 and 100 that the player must guess
    private var targetNumber by mutableIntStateOf(Random.nextInt(1, 101))

    // Number of attempts the user has made in the current round
    private var attempts by mutableIntStateOf(0)

    // Current text input entered by the user
    private var numberText by mutableStateOf("")

    // Feedback message displayed to the user (e.g. "Too high", "Too low", "You won!")
    private var guessText by mutableStateOf<String?>(null)

    // Flag indicating whether the user guessed the correct number in the current round
    private var isGuessCorrect by mutableStateOf(false)

    /**
     * The unified UI state exposed as a read-only Compose [State].
     *
     * How `transform` works with [StateStore]:
     * 1. It executes a headless `@Composable` block outside of any UI rendering.
     * 2. It reads local snapshot states ([numberText], [guessText], [isGuessCorrect]) AND
     *    the shared application state from [stateStore] via `stateStore.getState(GameStatsKey).value`.
     * 3. Whenever ANY of these values update, this block automatically re-runs ("recomposes")
     *    and produces an updated [NumberGuessState].
     * 4. The Compose UI observes this [State] directly with `val state by viewModel.state`, without
     *    needing `collectAsStateWithLifecycle()`!
     */
    val state: State<NumberGuessState> = transform(
        initialValue = NumberGuessState(),
        scope = scope,
        dispatcher = dispatcher
    ) { _ ->
        // Read the shared stats from StateStore (tracked automatically by Compose snapshot runtime)
        val stats = stateStore.getState(GameStatsKey).value

        NumberGuessState(
            numberText = numberText,
            guessText = guessText,
            isGuessCorrect = isGuessCorrect,
            gamesWon = stats.gamesWon,
            bestScore = stats.bestScore
        )
    }

    /**
     * Handles user actions dispatched from the UI.
     *
     * Notice the use of [Snapshot.withMutableSnapshot]:
     * Wrapping multiple property mutations inside `withMutableSnapshot` ensures they are committed
     * together in a single atomic transaction. Observers (and the [transform] block) only see the
     * final result, avoiding multiple redundant recompositions or intermediate states.
     */
    fun onAction(action: NumberGuessAction) {
        when (action) {
            // User typed or edited the text in the input field
            is NumberGuessAction.OnNumberTextChange -> {
                numberText = action.numberText
            }

            // User tapped the "Make guess" button
            NumberGuessAction.OnGuessClick -> {
                // Execute all updates atomically in a single snapshot transaction
                Snapshot.withMutableSnapshot {
                    val guess = numberText.toIntOrNull()
                    if (guess != null) {
                        attempts++
                    }

                    val isCorrect = (guess != null && guess == targetNumber)

                    // Determine the feedback message based on the guess
                    guessText = when {
                        guess == null -> "Please enter a number."
                        targetNumber > guess -> "Nope, my number is larger."
                        targetNumber < guess -> "Nope, my number is smaller."
                        else -> "That was it! You needed $attempts attempts."
                    }

                    isGuessCorrect = isCorrect
                    numberText = ""

                    // If the user won, update the shared StateStore
                    if (isCorrect) {
                        stateStore.updateState(GameStatsKey) { current ->
                            current.copy(
                                gamesWon = current.gamesWon + 1,
                                bestScore = if (current.bestScore == null) {
                                    attempts
                                } else {
                                    minOf(current.bestScore, attempts)
                                },
                                totalAttempts = current.totalAttempts + attempts
                            )
                        }
                    }
                }
            }

            // User tapped the "Start new game" button after winning
            NumberGuessAction.OnStartNewGameButtonClick -> {
                // Reset round state atomically
                Snapshot.withMutableSnapshot {
                    targetNumber = Random.nextInt(1, 101)
                    attempts = 0
                    numberText = ""
                    guessText = null
                    isGuessCorrect = false
                }
            }
        }
    }
}

