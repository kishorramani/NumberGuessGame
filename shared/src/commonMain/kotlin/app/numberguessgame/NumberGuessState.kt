package app.numberguessgame

/**
 * Represents the immutable UI state of the Number Guess Game screen.
 *
 * In Modern Android & Compose architecture (MVI / UDF):
 * - State flows DOWN from the ViewModel to the UI.
 * - The UI is a pure function of this state.
 * - Any user interaction produces an Action (event), which flows UP to the ViewModel.
 *
 * @property numberText The current text value entered by the user in the input field.
 * @property guessText The message/feedback shown to the player (e.g. "Nope, my number is larger.").
 *                     Null when no guess has been made yet.
 * @property isGuessCorrect True when the player successfully guessed the target number; controls
 *                          the visibility of the "Start new game" button.
 * @property gamesWon Total number of completed games won, shared via [androidx.appstate.statestore.StateStore].
 * @property bestScore Best (lowest) attempt count achieved so far, shared via [androidx.appstate.statestore.StateStore].
 */
data class NumberGuessState(
    val numberText: String = "",
    val guessText: String? = null,
    val isGuessCorrect: Boolean = false,
    val gamesWon: Int = 0,
    val bestScore: Int? = null
)
