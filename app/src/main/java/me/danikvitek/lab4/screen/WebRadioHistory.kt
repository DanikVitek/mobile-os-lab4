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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CardColors
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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

@Composable
fun WebRadioHistory(
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = hiltViewModel(),
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
                    ErrorVariant.NO_INTERNET_CONNECTION -> R.string.toast_error_no_internet_connection
                    ErrorVariant.RESPONSE_ERROR -> R.string.toast_error_response_error
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
        )
    }
}

@Composable
private fun History(
    history: List<HistoryRecord>,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    val visibleState = remember { MutableTransitionState(false) }
    visibleState.targetState = true

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        history.getOrNull(0)?.let { firstRecord ->
            item(key = firstRecord.id, contentType = firstRecord.javaClass) {
                AnimatedVisibility(
                    visibleState = visibleState,
                    enter = slideInVertically { -it } + expandVertically(),
                ) {
                    HistoryRecord(
                        record = firstRecord,
                    )
                }
            }
            items(
                items = history.subList(1, history.size),
                key = { it.id },
                contentType = { it.javaClass }
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
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth(0.95f),
        colors = CardColors(
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            disabledContentColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f),
            disabledContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
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
                    .widthIn(max = 235.dp),
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
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f),
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
        WebRadioHistory(
            history = history,
            modifier = Modifier.padding()
        )
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
        List(15) {
            val id = it.toLong() + 1
            HistoryRecord(
                id = id,
                title = "Title $id",
                artist = "Artist $id",
                timestamp = Date.from(ZonedDateTime.now().minusDays(it.toLong()).toInstant()),
            )
        }
    )
}
