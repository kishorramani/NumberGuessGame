package app.numberguessgame

/**
 * Represents all possible user actions (intents/events) that can occur on the Number Guess screen.
 *
 * Following Unidirectional Data Flow (UDF):
 * - UI elements (buttons, text fields) do not modify state directly.
 * - Instead, they emit a [NumberGuessAction] to the ViewModel via `onAction(action)`.
 * - The ViewModel processes the action and updates the state.
 */
sealed interface NumberGuessAction {

    /**
     * Triggered when the user clicks the "Make guess" button.
     */
    data object OnGuessClick : NumberGuessAction

    /**
     * Triggered when the user types or alters characters in the number input field.
     *
     * @param numberText The updated string value from the text field.
     */
    data class OnNumberTextChange(val numberText: String) : NumberGuessAction

    /**
     * Triggered when the user clicks the "Start new game" button to reset the round.
     */
    data object OnStartNewGameButtonClick : NumberGuessAction
}
