package me.danikvitek.lab4.screen

import android.widget.Toast
import androidx.annotation.FloatRange
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
            HistoryTable(
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
private fun HistoryTable(
    history: List<HistoryRecord>,
    modifier: Modifier = Modifier,
    @FloatRange(from = 0.0, fromInclusive = false) titleWeight: Float = 8f,
    @FloatRange(from = 0.0, fromInclusive = false) artistWeight: Float = 7f,
    @FloatRange(from = 0.0, fromInclusive = false) timestampWeight: Float = 7f,
) {
    val listState = rememberLazyListState()
    LazyColumn(
        state = listState,
        modifier = modifier,
    ) {
        item(key = -1L) {
            Header(
                titleWeight = titleWeight,
                artistWeight = artistWeight,
                timestampWeight = timestampWeight,
            )
        }
        items(items = history, key = { it.id }) { student ->
            EntryRow(
                history = student,
                titleWeight = titleWeight,
                artistWeight = artistWeight,
                timestampWeight = timestampWeight,
            )
        }
    }
}

@Composable
private fun Header(
    @FloatRange(from = 0.0, fromInclusive = false) titleWeight: Float,
    @FloatRange(from = 0.0, fromInclusive = false) artistWeight: Float,
    @FloatRange(from = 0.0, fromInclusive = false) timestampWeight: Float,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .weight(weight = titleWeight)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                )
                .padding(horizontal = 4.dp),
        ) {
            Text(
                text = stringResource(R.string.history_table_title),
                fontWeight = FontWeight.SemiBold,
            )
        }
        Box(
            modifier = Modifier
                .weight(weight = artistWeight)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                )
                .padding(horizontal = 4.dp),
        ) {
            Text(
                text = stringResource(R.string.history_table_artist),
                fontWeight = FontWeight.SemiBold,
            )
        }
        Box(
            modifier = Modifier
                .weight(weight = timestampWeight)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                )
                .padding(horizontal = 4.dp),
        ) {
            Text(
                text = stringResource(R.string.history_table_timestamp),
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun EntryRow(
    history: HistoryRecord,
    @FloatRange(from = 0.0, fromInclusive = false) titleWeight: Float,
    @FloatRange(from = 0.0, fromInclusive = false) artistWeight: Float,
    @FloatRange(from = 0.0, fromInclusive = false) timestampWeight: Float,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
    ) {
        Box(
            modifier = Modifier
                .weight(weight = titleWeight)
                .fillMaxHeight()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                )
                .padding(horizontal = 4.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                text = history.title,
            )
        }
        Box(
            modifier = Modifier
                .weight(weight = artistWeight)
                .fillMaxHeight()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                )
                .padding(horizontal = 4.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                text = history.artist,
            )
        }
        Box(
            modifier = Modifier
                .weight(weight = timestampWeight)
                .fillMaxHeight()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                )
                .padding(horizontal = 4.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                text = history.timestamp.toString(),
            )
        }
    }
}

@Preview
@Composable
private fun HistoryTablePreview(
    @PreviewParameter(HistoryTablePreviewProvider::class) history: List<HistoryRecord>,
) {
    Lab4Theme {
        Scaffold { innerPadding ->
            HistoryTable(
                history = history,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

private class HistoryTablePreviewProvider : PreviewParameterProvider<List<HistoryRecord>> {
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
            )
        }
    )
}
