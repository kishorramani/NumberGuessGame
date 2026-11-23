package app.numberguessgame

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Root Composable for the Number Guess game screen.
 *
 * In the new Jetpack AppState pattern:
 * - The ViewModel exposes `state: State<NumberGuessState>` instead of `StateFlow<NumberGuessState>`.
 * - Because `State<T>` is a first-class Compose primitive, we no longer need `collectAsStateWithLifecycle()`.
 * - We simply delegate using `val state by viewModel.state`. Compose automatically recomposes whenever
 *   the snapshot state updates!
 */
@Composable
fun NumberGuessScreenRoot(modifier: Modifier = Modifier) {
    val viewModel = viewModel { NumberGuessViewModel() }

    // Direct observation of Compose State (no collectAsStateWithLifecycle needed!)
    val state by viewModel.state

    NumberGuessScreen(
        state = state,
        onAction = viewModel::onAction,
        modifier = modifier
    )
}

/**
 * Stateless UI screen for the Number Guess game.
 *
 * It receives:
 * @param state The current UI state ([NumberGuessState]) to display.
 * @param onAction Lambda to handle user actions ([NumberGuessAction]).
 * @param modifier Modifier for styling and layout adjustments.
 */
@Composable
fun NumberGuessScreen(
    state: NumberGuessState,
    onAction: (NumberGuessAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        // Display shared statistics managed by Jetpack AppState's StateStore
        Text(
            text = "Games won: ${state.gamesWon} | Best score: ${state.bestScore ?: "-"}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Text input field for user to enter their guess
        TextField(
            value = state.numberText,
            onValueChange = { newText ->
                onAction(NumberGuessAction.OnNumberTextChange(newText))
            },
            placeholder = { Text("Enter a number (1-100)") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            )
        )

        // Button to submit the guess
        Button(
            onClick = {
                onAction(NumberGuessAction.OnGuessClick)
            }
        ) {
            Text("Make guess")
        }

        // Show feedback message if available
        if (state.guessText != null) {
            Text(
                text = state.guessText
            )
        }

        // Show "Start new game" button when player wins
        if (state.isGuessCorrect) {
            Button(
                onClick = {
                    onAction(NumberGuessAction.OnStartNewGameButtonClick)
                }
            ) {
                Text("Start new game")
            }
        }
    }
}

@Preview(
    showBackground = true
)
@Composable
private fun NumberGuessScreenPreview() {
    NumberGuessScreen(
        state = NumberGuessState(
            numberText = "42",
            guessText = "Nope, my number is larger.",
            isGuessCorrect = false,
            gamesWon = 2,
            bestScore = 4
        ),
        onAction = {}
    )
}
