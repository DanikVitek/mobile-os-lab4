package me.danikvitek.lab4.screen

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CardColors
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import me.danikvitek.lab4.R
import me.danikvitek.lab4.data.entity.HistoryRecord
import me.danikvitek.lab4.ui.theme.Lab4Theme
import me.danikvitek.lab4.viewmodel.ErrorVariant
import me.danikvitek.lab4.viewmodel.HistoryViewModel
import me.danikvitek.lab4.viewmodel.Status
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Date
import kotlin.random.Random

@Composable
fun WebRadioHistory(
    viewModel: HistoryViewModel,
    modifier: Modifier = Modifier,
) {
    val status by viewModel.status.collectAsState()
    WebRadioHistory(
        status = status,
        modifier = modifier,
    )
}

@Composable
private fun WebRadioHistory(status: Status, modifier: Modifier = Modifier) {
    when (status) {
        Status.Initializing -> {}
        is Status.WithHistory -> {
            val history by status.history.collectAsState(emptyList())
            WebRadioHistory(
                history = history,
                modifier = modifier,
            )
            if (status is Status.Error) Toast.makeText(
                LocalContext.current,
                when (status.error) {
                    ErrorVariant.NO_INTERNET_CONNECTION -> R.string.toast_no_internet_connection
                    ErrorVariant.RESPONSE_ERROR -> R.string.toast_response_error
                },
                Toast.LENGTH_SHORT,
            ).show()
        }
    }
}

@Composable
private fun WebRadioHistory(
    history: List<HistoryRecord>,
    modifier: Modifier = Modifier,
    animateHistory: Boolean = true,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            Surface(
                shadowElevation = 2.dp,
                color = MaterialTheme.colorScheme.secondaryContainer,
            ) {
                Text(
                    text = stringResource(R.string.history_top_bar_title),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp, bottom = 16.dp),
                )
            }
        },
    ) { innerPadding ->
        History(
            history = history,
            modifier = Modifier.padding(innerPadding),
            animateHistory = animateHistory,
        )
    }
}

@Composable
private fun History(
    history: List<HistoryRecord>,
    animateHistory: Boolean,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    val visibleState = MutableTransitionState(initialState = !animateHistory)
    visibleState.targetState = true

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        history.getOrNull(0)?.let { firstRecord ->
            item(key = firstRecord.id) {
                AnimatedVisibility(
                    visibleState = visibleState,
                    enter = slideInVertically { -it } + expandVertically(),
                ) {
                    HistoryRecord(
                        record = firstRecord,
                        containerColor = MaterialTheme.colorScheme.primary,
                    )
                    LaunchedEffect(firstRecord.id) {
                        if (listState.firstVisibleItemIndex == 1) {
                            listState.animateScrollToItem(0)
                        }
                    }
                }
            }
            items(
                items = history.subList(1, history.size),
                key = { it.id },
            ) { record ->
                HistoryRecord(
                    record = record,
                )
            }
        }
    }
}

@Composable
private fun HistoryRecord(
    record: HistoryRecord,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = contentColorFor(containerColor),
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth(0.95f),
        colors = CardColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.7f),
            disabledContentColor = contentColor.copy(alpha = 0.7f),
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .padding(vertical = 8.dp, horizontal = 16.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = record.title,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "${stringResource(R.string.artist)}: ${record.artist}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Text(
                text = record.timestamp.toShortString(),
                textAlign = TextAlign.Right,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp),
                color = contentColor.copy(alpha = 0.7f),
            )
        }
    }
}

/**
 * - when the date is today, display only time in local format
 * - when the date is yesterday, display "Yesterday"
 * - otherwise, display the date in the local format
 */
@Composable
private fun Date.toShortString(): String {
    val now: LocalDateTime = LocalDateTime.now()
    val today: LocalDate = now.toLocalDate()
    val date: ZonedDateTime = this.toInstant().atZone(ZoneId.systemDefault())
    return when {
        date.toLocalDate() == today ->
            date.toLocalTime().format(DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM))

        date.toLocalDate() == today.minusDays(1) -> stringResource(R.string.yesterday)
        else -> date.toLocalDate().toString()
    }
}

@Preview
@Composable
private fun WebRadioHistoryPreview(
    @PreviewParameter(WebRadioHistoryPreviewProvider::class) history: List<HistoryRecord>,
) {
    Lab4Theme {
        WebRadioHistory(history = history, animateHistory = false)
    }
}

private class WebRadioHistoryPreviewProvider : PreviewParameterProvider<List<HistoryRecord>> {
    override val values = sequenceOf(
        emptyList(),
        listOf(
            HistoryRecord(
                id = 1,
                title = "Title 1",
                artist = "Artist 1",
            ),
            HistoryRecord(
                id = 2,
                title = "Title 2",
                artist = "Artist 2",
            ),
        ),
        List(15) { idx ->
            val id = idx.toLong() + 1
            HistoryRecord(
                id = id,
                title = randomString(Random.nextInt(10, 100)),
                artist = randomString(Random.nextInt(10, 50)),
                timestamp = Date.from(ZonedDateTime.now().minusDays(idx.toLong()).toInstant()),
            )
        }
    )
    override val count: Int = 3

    companion object {
        private val chars = buildSet(capacity = 26 + 26 + 10 + 1) {
            addAll('a'..'z')
            addAll('A'..'Z')
            addAll('0'..'9')
            add(' ')
        }

        private fun randomString(length: Int): String =
            generateSequence { chars.random() }.take(length).joinToString("")
    }
}
