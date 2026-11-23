package app.numberguessgame

import androidx.appstate.statestore.StateStore
import androidx.appstate.transform.testing.transformTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Unit tests for [NumberGuessViewModel] using Jetpack AppState's [transformTest].
 *
 * Why [transformTest]?
 * - AppState transforms run asynchronously on a background dispatcher via the Compose runtime.
 * - [transformTest] provides a deterministic testing environment with `backgroundScope`, `testDispatcher`,
 *   and `advance()`, which forces pending Compose snapshot updates to apply before assertions run.
 */
class NumberGuessViewModelTest {

    @Test
    fun initialState_hasEmptyInputAndNoGuessFeedback() = transformTest {
        val viewModel = NumberGuessViewModel(
            coroutineScope = backgroundScope,
            dispatcher = testDispatcher
        )

        val state = viewModel.state.value

        assertEquals("", state.numberText)
        assertNull(state.guessText)
        assertFalse(state.isGuessCorrect)
        assertEquals(0, state.gamesWon)
        assertNull(state.bestScore)
    }

    @Test
    fun onNumberTextChange_updatesNumberText() = transformTest {
        val viewModel = NumberGuessViewModel(
            coroutineScope = backgroundScope,
            dispatcher = testDispatcher
        )

        viewModel.onAction(NumberGuessAction.OnNumberTextChange("42"))
        advance()

        assertEquals("42", viewModel.state.value.numberText)
    }

    @Test
    fun onGuessClick_withInvalidInput_showsErrorMessage() = transformTest {
        val viewModel = NumberGuessViewModel(
            coroutineScope = backgroundScope,
            dispatcher = testDispatcher
        )

        viewModel.onAction(NumberGuessAction.OnNumberTextChange("abc"))
        viewModel.onAction(NumberGuessAction.OnGuessClick)
        advance()

        val state = viewModel.state.value
        assertEquals("Please enter a number.", state.guessText)
        assertFalse(state.isGuessCorrect)
        assertEquals("", state.numberText)
    }

    @Test
    fun onGuessClick_withValidGuess_clearsInputAndGivesFeedback() = transformTest {
        val viewModel = NumberGuessViewModel(
            coroutineScope = backgroundScope,
            dispatcher = testDispatcher
        )

        viewModel.onAction(NumberGuessAction.OnNumberTextChange("50"))
        viewModel.onAction(NumberGuessAction.OnGuessClick)
        advance()

        val state = viewModel.state.value
        assertNotNull(state.guessText)
        assertEquals("", state.numberText)
    }

    @Test
    fun onStartNewGameButtonClick_resetsState() = transformTest {
        val viewModel = NumberGuessViewModel(
            coroutineScope = backgroundScope,
            dispatcher = testDispatcher
        )

        viewModel.onAction(NumberGuessAction.OnNumberTextChange("50"))
        viewModel.onAction(NumberGuessAction.OnGuessClick)
        advance()

        viewModel.onAction(NumberGuessAction.OnStartNewGameButtonClick)
        advance()

        val state = viewModel.state.value
        assertEquals("", state.numberText)
        assertNull(state.guessText)
        assertFalse(state.isGuessCorrect)
    }

    @Test
    fun stateStoreUpdates_recomposeTransformState() = transformTest {
        val stateStore = StateStore()
        val viewModel = NumberGuessViewModel(
            coroutineScope = backgroundScope,
            dispatcher = testDispatcher,
            stateStore = stateStore
        )

        // Initial stats check
        assertEquals(0, viewModel.state.value.gamesWon)
        assertNull(viewModel.state.value.bestScore)

        // Simulate StateStore update from application/service
        stateStore.updateState(GameStatsKey) { current ->
            current.copy(gamesWon = 3, bestScore = 4)
        }
        advance()

        // Assert that transform automatically picked up the StateStore change
        val updatedState = viewModel.state.value
        assertEquals(3, updatedState.gamesWon)
        assertEquals(4, updatedState.bestScore)
    }
}
